package Contents;

import adminsacas.DBConnection;
import adminsacas.Session;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * manageapp – Student panel: view and cancel own appointments.
 * Filter by PENDING / CONFIRMED / HISTORY.
 */
public class manageapp extends javax.swing.JPanel {

    private static class Appt {
        int    id;
        String instructorName, day, time, reason, status, createdAt;
    }

    private final List<Appt> allAppts = new ArrayList<>();
    private String currentFilter = "ALL";
    private JPanel listArea;
    private JButton btnAll, btnPending, btnConfirmed, btnHistory;
    private JTextField searchField;

    public manageapp() {
        initComponents();
        initUI();
        refreshData();
    }

    public void refreshData() {
        loadFromDB();
        renderList();
    }

    // ── load from DB ─────────────────────────────────────────────────────────
    private void loadFromDB() {
        allAppts.clear();
        int studentId = Session.getProfileId();
        if (studentId < 0) return;
        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT a.id, a.day_of_week, a.time_slot, a.reason, a.status, "
                       + "DATE_FORMAT(a.created_at,'%b %d, %Y') AS created_at, "
                       + "CONCAT(i.firstname,' ',i.lastname) AS iname "
                       + "FROM appointments a "
                       + "JOIN instructors i ON a.instructor_id=i.id "
                       + "WHERE a.student_id=? ORDER BY a.created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, studentId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Appt ap = new Appt();
                    ap.id             = rs.getInt("id");
                    ap.day            = rs.getString("day_of_week");
                    ap.time           = rs.getString("time_slot");
                    ap.reason         = rs.getString("reason");
                    ap.status         = rs.getString("status");
                    ap.createdAt      = rs.getString("created_at");
                    ap.instructorName = rs.getString("iname");
                    allAppts.add(ap);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── build UI ─────────────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout()); setBackground(Color.WHITE);

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE); header.setBorder(BorderFactory.createEmptyBorder(18,24,8,24));
        JLabel title = new JLabel("MANAGE APPOINTMENT");
        title.setFont(new Font("Tahoma",Font.BOLD,22)); header.add(title, BorderLayout.WEST);
        JSeparator sep = new JSeparator(); sep.setForeground(Color.LIGHT_GRAY);
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE);
        top.add(header, BorderLayout.CENTER); top.add(sep, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        toolbar.setBackground(Color.WHITE); toolbar.setBorder(BorderFactory.createEmptyBorder(0,18,0,18));

        btnAll       = makeFilterBtn("ALL");
        btnPending   = makeFilterBtn("PENDING");
        btnConfirmed = makeFilterBtn("CONFIRMED");
        btnHistory   = makeFilterBtn("HISTORY");
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Tahoma",Font.PLAIN,14)); refreshBtn.setFocusPainted(false);

        btnAll.addActionListener(e       -> { currentFilter="ALL";       renderList(); updateFilterBtns(); });
        btnPending.addActionListener(e   -> { currentFilter="PENDING";   renderList(); updateFilterBtns(); });
        btnConfirmed.addActionListener(e -> { currentFilter="CONFIRMED"; renderList(); updateFilterBtns(); });
        btnHistory.addActionListener(e   -> { currentFilter="HISTORY";   renderList(); updateFilterBtns(); });
        refreshBtn.addActionListener(e   -> refreshData());

        searchField = new JTextField(16);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(4,6,4,6)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { renderList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { renderList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { renderList(); }
        });

        toolbar.add(btnAll); toolbar.add(btnPending); toolbar.add(btnConfirmed); toolbar.add(btnHistory);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(new JLabel("Search:")); toolbar.add(searchField); toolbar.add(refreshBtn);

        listArea = new JPanel(); listArea.setLayout(new BoxLayout(listArea, BoxLayout.Y_AXIS));
        listArea.setBackground(Color.WHITE); listArea.setBorder(BorderFactory.createEmptyBorder(10,24,10,24));
        JScrollPane scroll = new JScrollPane(listArea);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout()); center.setBackground(Color.WHITE);
        center.add(toolbar, BorderLayout.NORTH); center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        updateFilterBtns();
    }

    private void renderList() {
        String search = searchField != null ? searchField.getText().toLowerCase() : "";
        listArea.removeAll();
        int shown = 0;
        for (Appt ap : allAppts) {
            boolean matchFilter = "ALL".equals(currentFilter)
                || currentFilter.equals(ap.status)
                || ("HISTORY".equals(currentFilter) && ("DECLINED".equals(ap.status) || "CANCELLED".equals(ap.status) || "COMPLETED".equals(ap.status)));
            boolean matchSearch = search.isEmpty()
                || ap.instructorName.toLowerCase().contains(search)
                || ap.day.toLowerCase().contains(search);
            if (matchFilter && matchSearch) {
                listArea.add(buildCard(ap)); listArea.add(Box.createVerticalStrut(10)); shown++;
            }
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No appointments found.");
            empty.setFont(new Font("Tahoma",Font.PLAIN,14)); empty.setForeground(Color.GRAY);
            empty.setBorder(BorderFactory.createEmptyBorder(30,0,0,0));
            listArea.add(empty);
        }
        listArea.revalidate(); listArea.repaint();
    }

    private JPanel buildCard(Appt ap) {
        JPanel card = new JPanel(new BorderLayout(10,0));
        card.setBackground(new Color(240,240,240));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180)),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS)); info.setBackground(new Color(240,240,240));
        info.add(lbl(ap.instructorName, 15, Font.BOLD));
        info.add(lbl(ap.day + "  –  " + ap.time, 13, Font.PLAIN));
        info.add(lbl("Reason: " + ap.reason, 12, Font.ITALIC));
        info.add(lbl("Submitted: " + ap.createdAt, 11, Font.PLAIN));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actions.setBackground(new Color(240,240,240));

        JLabel badge = new JLabel(ap.status);
        badge.setFont(new Font("Tahoma",Font.BOLD,12)); badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(4,10,4,10)); badge.setForeground(Color.WHITE);
        switch (ap.status) {
            case "PENDING"   -> badge.setBackground(new Color(200,130,0));
            case "CONFIRMED" -> badge.setBackground(new Color(30,120,30));
            case "DECLINED"  -> badge.setBackground(new Color(160,30,30));
            case "CANCELLED" -> badge.setBackground(Color.DARK_GRAY);
            default          -> badge.setBackground(Color.GRAY);
        }
        actions.add(badge);

        // Cancel button only for PENDING / CONFIRMED
        if ("PENDING".equals(ap.status) || "CONFIRMED".equals(ap.status)) {
            JButton cancelBtn = new JButton("CANCEL");
            cancelBtn.setBackground(new Color(160,30,30)); cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFont(new Font("Tahoma",Font.BOLD,12)); cancelBtn.setFocusPainted(false);
            cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(5,10,5,10)));
            cancelBtn.addActionListener(e -> cancelAppointment(ap));
            actions.add(cancelBtn);
        }

        card.add(info, BorderLayout.CENTER); card.add(actions, BorderLayout.EAST);
        return card;
    }

    private void cancelAppointment(Appt ap) {
        int c = JOptionPane.showConfirmDialog(this,
            "Cancel your appointment with " + ap.instructorName + " on " + ap.day + " at " + ap.time + "?",
            "Cancel Appointment", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try (Connection conn = DBConnection.get()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE appointments SET status='CANCELLED' WHERE id=?")) {
                ps.setInt(1, ap.id); ps.executeUpdate();
            }
            ap.status = "CANCELLED";
            renderList();
            JOptionPane.showMessageDialog(this, "Appointment cancelled.", "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton makeFilterBtn(String text) {
        JButton b = new JButton(text); b.setFont(new Font("Tahoma",Font.BOLD,12)); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY), BorderFactory.createEmptyBorder(5,12,5,12)));
        return b;
    }
    private void updateFilterBtns() {
        for (JButton b : new JButton[]{btnAll,btnPending,btnConfirmed,btnHistory}) {
            if (b == null) continue;
            boolean active = b.getText().equals(currentFilter);
            b.setBackground(active ? new Color(60,60,60) : new Color(210,210,210));
            b.setForeground(active ? Color.WHITE : Color.BLACK);
        }
    }
    private JLabel lbl(String t, int sz, int style) { JLabel l=new JLabel(t); l.setFont(new Font("Tahoma",style,sz)); return l; }
    private void initComponents() { /* built in initUI */ }
}
