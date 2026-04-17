package Contents;

import adminsacas.DBConnection;
import adminsacas.Session;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * viewlogs – Instructor panel: see consultation history from DB.
 * Supports search by student name and CSV export.
 */
public class viewlogs extends javax.swing.JPanel {

    private static class Log {
        String studentName, day, time, reason, status, loggedAt;
    }

    private final List<Log> allLogs = new ArrayList<>();
    private JPanel    listArea;
    private JTextField searchField;

    public viewlogs() {
        initComponents();
        initUI();
        refreshData();
    }

    public void refreshData() {
        loadFromDB();
        renderList();
    }

    private void loadFromDB() {
        allLogs.clear();
        int instructorId = Session.getProfileId();
        if (instructorId < 0) return;
        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT cl.student_name, cl.day_of_week, cl.time_slot, "
                       + "cl.status, DATE_FORMAT(cl.logged_at,'%b %d, %Y') AS logged_at, "
                       + "a.reason "
                       + "FROM consultation_logs cl "
                       + "LEFT JOIN appointments a ON cl.appointment_id=a.id "
                       + "WHERE cl.instructor_name=? ORDER BY cl.logged_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, Session.getFullName());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Log l = new Log();
                    l.studentName = rs.getString("student_name");
                    l.day         = rs.getString("day_of_week");
                    l.time        = rs.getString("time_slot");
                    l.status      = rs.getString("status");
                    l.loggedAt    = rs.getString("logged_at");
                    l.reason      = rs.getString("reason");
                    allLogs.add(l);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        setLayout(new BorderLayout()); setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE); header.setBorder(BorderFactory.createEmptyBorder(18,24,8,24));
        JLabel title = new JLabel("CONSULTATION LOGS");
        title.setFont(new Font("Tahoma",Font.BOLD,22)); header.add(title, BorderLayout.WEST);
        JSeparator sep = new JSeparator(); sep.setForeground(Color.LIGHT_GRAY);
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE);
        top.add(header, BorderLayout.CENTER); top.add(sep, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        toolbar.setBackground(Color.WHITE); toolbar.setBorder(BorderFactory.createEmptyBorder(0,18,0,18));

        searchField = new JTextField(18);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(4,6,4,6)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { renderList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { renderList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { renderList(); }
        });

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.setFont(new Font("Tahoma",Font.PLAIN,12)); exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> exportCSV());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Tahoma",Font.PLAIN,12)); refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshData());

        toolbar.add(new JLabel("🔍")); toolbar.add(searchField);
        toolbar.add(Box.createHorizontalStrut(8)); toolbar.add(exportBtn); toolbar.add(refreshBtn);

        listArea = new JPanel(); listArea.setLayout(new BoxLayout(listArea,BoxLayout.Y_AXIS));
        listArea.setBackground(Color.WHITE); listArea.setBorder(BorderFactory.createEmptyBorder(10,24,10,24));
        JScrollPane scroll = new JScrollPane(listArea);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout()); center.setBackground(Color.WHITE);
        center.add(toolbar, BorderLayout.NORTH); center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void renderList() {
        String search = searchField != null ? searchField.getText().toLowerCase() : "";
        listArea.removeAll();
        int shown = 0;
        for (Log log : allLogs) {
            if (search.isEmpty() || log.studentName.toLowerCase().contains(search)) {
                listArea.add(buildCard(log)); listArea.add(Box.createVerticalStrut(8)); shown++;
            }
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No consultation logs found.");
            empty.setFont(new Font("Tahoma",Font.PLAIN,14)); empty.setForeground(Color.GRAY);
            empty.setBorder(BorderFactory.createEmptyBorder(30,0,0,0));
            listArea.add(empty);
        }
        listArea.revalidate(); listArea.repaint();
    }

    private JPanel buildCard(Log log) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(242,242,242));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180)),
            BorderFactory.createEmptyBorder(10,14,10,14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS)); info.setBackground(new Color(242,242,242));
        info.add(lbl(log.studentName, 14, Font.BOLD));
        info.add(lbl(log.day + "  –  " + log.time, 13, Font.PLAIN));
        if (log.reason != null && !log.reason.isEmpty()) info.add(lbl("Reason: " + log.reason, 12, Font.ITALIC));

        JPanel right = new JPanel(); right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS)); right.setBackground(new Color(242,242,242));

        JLabel badge = new JLabel(log.status != null ? log.status : "");
        badge.setFont(new Font("Tahoma",Font.BOLD,11)); badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(3,8,3,8)); badge.setForeground(Color.WHITE);
        if ("CONFIRMED".equals(log.status)) badge.setBackground(new Color(30,120,30));
        else if ("DECLINED".equals(log.status)) badge.setBackground(new Color(160,30,30));
        else badge.setBackground(Color.GRAY);

        JLabel dateLbl = lbl(log.loggedAt != null ? log.loggedAt : "", 11, Font.PLAIN);
        dateLbl.setForeground(Color.GRAY);
        right.add(badge); right.add(Box.createVerticalStrut(4)); right.add(dateLbl);

        card.add(info, BorderLayout.CENTER); card.add(right, BorderLayout.EAST);
        return card;
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("consultation_logs.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(fc.getSelectedFile())) {
            pw.println("Student Name,Day,Time Slot,Reason,Status,Logged At");
            for (Log l : allLogs) {
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    l.studentName, l.day, l.time,
                    l.reason != null ? l.reason : "",
                    l.status, l.loggedAt);
            }
            JOptionPane.showMessageDialog(this, "Exported to " + fc.getSelectedFile().getAbsolutePath(), "Export Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel lbl(String t, int sz, int style) { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI",style,sz)); return l; }
    private void initComponents() { /* built in initUI */ }
}
