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
 * viewfaculty – Student panel: see all instructors and their availability,
 * then book an appointment directly from here.
 */
public class viewfaculty extends javax.swing.JPanel {

    // ── data model ────────────────────────────────────────────────────────────
    private static class Faculty {
        int    id;
        String name, department, subjects;
        List<String[]> slots = new ArrayList<>();  // [day, timeSlot]
    }

    private final List<Faculty> facultyList = new ArrayList<>();
    private JPanel listArea;

    public viewfaculty() {
        initComponents();
        initUI();
        refreshData();
    }

    public void refreshData() {
        loadFromDB();
        buildCards();
    }

    // ── load instructors + their availability from DB ────────────────────────
    private void loadFromDB() {
        facultyList.clear();
        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT i.id, CONCAT(i.firstname,' ',i.lastname) AS name, "
                       + "i.course AS dept, "
                       + "CONCAT_WS(', ',NULLIF(i.subject1,''),NULLIF(i.subject2,''),"
                       + "NULLIF(i.subject3,''),NULLIF(i.subject4,'')) AS subjects "
                       + "FROM instructors i ORDER BY i.lastname";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Faculty f = new Faculty();
                    f.id = rs.getInt("id");
                    f.name = rs.getString("name");
                    f.department = rs.getString("dept");
                    f.subjects = rs.getString("subjects");
                    facultyList.add(f);
                }
            }
            // load availability for each instructor
            for (Faculty f : facultyList) {
                String avSql = "SELECT day_of_week, time_slot FROM availability WHERE instructor_id=? ORDER BY day_of_week, time_slot";
                try (PreparedStatement ps = conn.prepareStatement(avSql)) {
                    ps.setInt(1, f.id);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) f.slots.add(new String[]{rs.getString(1), rs.getString(2)});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── build UI ─────────────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout()); setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE); header.setBorder(BorderFactory.createEmptyBorder(18,24,8,24));
        JLabel title = new JLabel("VIEW FACULTY AVAILABILITY");
        title.setFont(new Font("Segoe UI",Font.BOLD,22)); header.add(title, BorderLayout.WEST);
        JSeparator sep = new JSeparator(); sep.setForeground(Color.LIGHT_GRAY);
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE);
        top.add(header, BorderLayout.CENTER); top.add(sep, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        listArea = new JPanel(); listArea.setLayout(new BoxLayout(listArea, BoxLayout.Y_AXIS));
        listArea.setBackground(Color.WHITE); listArea.setBorder(BorderFactory.createEmptyBorder(14,34,14,34));
        JScrollPane scroll = new JScrollPane(listArea);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void buildCards() {
        listArea.removeAll();
        if (facultyList.isEmpty()) {
            JLabel empty = new JLabel("No instructors found. Admin must add instructors first.");
            empty.setFont(new Font("Segoe UI",Font.PLAIN,14)); empty.setForeground(Color.GRAY);
            empty.setBorder(BorderFactory.createEmptyBorder(40,0,0,0));
            listArea.add(empty);
        } else {
            for (Faculty f : facultyList) {
                listArea.add(buildCard(f));
                listArea.add(Box.createVerticalStrut(14));
            }
        }
        listArea.revalidate(); listArea.repaint();
    }

    private JPanel buildCard(Faculty f) {
        JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(210,210,210));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180)),
            BorderFactory.createEmptyBorder(14,16,14,16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel nameLbl = new JLabel(f.name);
        nameLbl.setFont(new Font("Segoe UI",Font.BOLD,18));

        JLabel deptLbl = new JLabel(f.department != null ? f.department : "");
        deptLbl.setFont(new Font("Segoe UI",Font.PLAIN,14));
        deptLbl.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));

        JLabel subLbl = new JLabel(f.subjects != null && !f.subjects.isEmpty() ? "Subjects: "+f.subjects : "");
        subLbl.setFont(new Font("Segoe UI",Font.ITALIC,13)); subLbl.setForeground(new Color(60,60,60));

        String slotSummary = f.slots.isEmpty() ? "No availability set yet"
            : f.slots.size() + " slot(s) available";
        JLabel slotLbl = new JLabel(slotSummary);
        slotLbl.setFont(new Font("Segoe UI",Font.BOLD,13)); slotLbl.setForeground(new Color(0,100,0));
        slotLbl.setBorder(BorderFactory.createEmptyBorder(6,0,10,0));

        JButton bookBtn = new JButton("View Schedule / Book Appointment");
        bookBtn.setFont(new Font("Segoe UI",Font.BOLD,14));
        bookBtn.setBackground(new Color(0,51,153)); bookBtn.setForeground(Color.WHITE);
        bookBtn.setFocusPainted(false);
        bookBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK,2), BorderFactory.createEmptyBorder(8,20,8,20)));
        bookBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bookBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { bookBtn.setBackground(new Color(0,70,200)); }
            public void mouseExited(MouseEvent e)  { bookBtn.setBackground(new Color(0,51,153)); }
        });
        bookBtn.addActionListener(e -> openBookingDialog(f));

        card.add(nameLbl); card.add(deptLbl); card.add(subLbl); card.add(slotLbl); card.add(bookBtn);
        return card;
    }

    // ── booking dialog ────────────────────────────────────────────────────────
    private void openBookingDialog(Faculty f) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Book Appointment – " + f.name, true);
        dlg.setSize(520, 480); dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout()); dlg.getContentPane().setBackground(Color.WHITE);

        // header
        JPanel dh = new JPanel(new BorderLayout()); dh.setBackground(new Color(0,51,153)); dh.setBorder(BorderFactory.createEmptyBorder(14,18,14,18));
        JLabel dTitle = new JLabel(f.name); dTitle.setFont(new Font("Segoe UI",Font.BOLD,18)); dTitle.setForeground(Color.WHITE);
        JLabel dSub = new JLabel(f.department != null ? f.department : ""); dSub.setForeground(new Color(200,220,255)); dSub.setFont(new Font("Segoe UI",Font.PLAIN,13));
        JPanel dTitlePanel = new JPanel(); dTitlePanel.setLayout(new BoxLayout(dTitlePanel,BoxLayout.Y_AXIS)); dTitlePanel.setBackground(new Color(0,51,153));
        dTitlePanel.add(dTitle); dTitlePanel.add(dSub);
        dh.add(dTitlePanel, BorderLayout.WEST); dlg.add(dh, BorderLayout.NORTH);

        // slot list
        JPanel slotsPanel = new JPanel(); slotsPanel.setLayout(new BoxLayout(slotsPanel,BoxLayout.Y_AXIS));
        slotsPanel.setBackground(Color.WHITE); slotsPanel.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));

        ButtonGroup bg = new ButtonGroup();
        Map<JRadioButton, String[]> radioMap = new LinkedHashMap<>();

        if (f.slots.isEmpty()) {
            slotsPanel.add(new JLabel("This instructor has not set availability yet."));
        } else {
            for (String[] slot : f.slots) {
                JRadioButton rb = new JRadioButton(slot[0] + "  –  " + slot[1]);
                rb.setFont(new Font("Segoe UI",Font.PLAIN,14)); rb.setBackground(Color.WHITE);
                bg.add(rb); slotsPanel.add(rb); slotsPanel.add(Box.createVerticalStrut(4));
                radioMap.put(rb, slot);
            }
        }

        JLabel reasonLbl = new JLabel("Reason for consultation:");
        reasonLbl.setFont(new Font("Segoe UI",Font.BOLD,13));
        reasonLbl.setBorder(BorderFactory.createEmptyBorder(14,0,4,0));
        JTextArea reasonTA = new JTextArea(3,30);
        reasonTA.setLineWrap(true); reasonTA.setWrapStyleWord(true);
        reasonTA.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        slotsPanel.add(reasonLbl); slotsPanel.add(new JScrollPane(reasonTA));

        JScrollPane spSlots = new JScrollPane(slotsPanel); spSlots.setBorder(BorderFactory.createEmptyBorder());
        dlg.add(spSlots, BorderLayout.CENTER);

        // footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE); footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.LIGHT_GRAY));
        JButton cancelBtn = new JButton("Cancel"); cancelBtn.addActionListener(e -> dlg.dispose());
        JButton confirmBtn = new JButton("SUBMIT REQUEST");
        confirmBtn.setBackground(new Color(0,51,153)); confirmBtn.setForeground(Color.WHITE); confirmBtn.setFocusPainted(false);
        confirmBtn.addActionListener(e -> {
            // find selected slot
            String[] chosenSlot = null;
            for (Map.Entry<JRadioButton,String[]> entry : radioMap.entrySet()) {
                if (entry.getKey().isSelected()) { chosenSlot = entry.getValue(); break; }
            }
            if (chosenSlot == null) {
                JOptionPane.showMessageDialog(dlg, "Please select a time slot.", "Selection Required", JOptionPane.WARNING_MESSAGE); return;
            }
            String reason = reasonTA.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please enter a reason for consultation.", "Required", JOptionPane.WARNING_MESSAGE); return;
            }
            submitBooking(f, chosenSlot[0], chosenSlot[1], reason);
            dlg.dispose();
        });
        footer.add(cancelBtn); footer.add(confirmBtn);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── save appointment to DB ────────────────────────────────────────────────
    private void submitBooking(Faculty f, String day, String time, String reason) {
        int studentId = Session.getProfileId();
        if (studentId < 0) {
            JOptionPane.showMessageDialog(this, "Session error. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        try (Connection conn = DBConnection.get()) {
            String sql = "INSERT INTO appointments (student_id,instructor_id,day_of_week,time_slot,reason,status) VALUES (?,?,?,?,?,'PENDING')";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1,studentId); ps.setInt(2,f.id);
                ps.setString(3,day); ps.setString(4,time); ps.setString(5,reason);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys(); keys.next();
                int appId = keys.getInt(1);

                // notify the instructor
                String notifMsg = "New appointment request from " + Session.getFullName()
                    + " on " + day + " at " + time + ". Reason: " + reason;
                sendNotification(conn, f.id, "INSTRUCTOR", notifMsg);
            }
            JOptionPane.showMessageDialog(this,
                "✅ Appointment request submitted!\n\nInstructor: " + f.name
                + "\nDay: " + day + "\nTime: " + time
                + "\nStatus: PENDING\n\nYou will be notified once the instructor responds.",
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Sends a notification row to the user identified by profileId + role. */
    private void sendNotification(Connection conn, int profileId, String role, String message) throws SQLException {
        // look up the user_id from the profile table
        String col = "INSTRUCTOR".equals(role) ? "instructors" : "students";
        String sql = "SELECT user_id FROM " + col + " WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt(1);
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO notifications (user_id,message) VALUES (?,?)")) {
                    ins.setInt(1, userId); ins.setString(2, message); ins.executeUpdate();
                }
            }
        }
    }

    private void initComponents() { /* built entirely in initUI */ }
}
