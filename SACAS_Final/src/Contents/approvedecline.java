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
 * approvedecline – Instructor panel to approve or decline student requests.
 * Reads from `appointments` and writes status + notifications back to DB.
 */
public class approvedecline extends javax.swing.JPanel {

    private enum Tab { PENDING, APPROVED, DECLINED }

    private static class Request {
        int    id, studentUserId;
        String studentName, studentCourse, day, time, reason, status;
    }

    private final List<Request> allRequests = new ArrayList<>();
    private Tab    currentTab = Tab.PENDING;
    private JPanel listArea;
    private JButton btnPending, btnApproved, btnDeclined;

    public approvedecline() {
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
        allRequests.clear();
        int instructorId = Session.getProfileId();
        if (instructorId < 0) return;

        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT a.id, a.day_of_week, a.time_slot, a.reason, a.status, "
                       + "CONCAT(s.firstname,' ',s.lastname) AS sname, s.course AS scourse, "
                       + "s.user_id AS suid "
                       + "FROM appointments a "
                       + "JOIN students s ON a.student_id=s.id "
                       + "WHERE a.instructor_id=? ORDER BY a.created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, instructorId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Request req = new Request();
                    req.id            = rs.getInt("id");
                    req.day           = rs.getString("day_of_week");
                    req.time          = rs.getString("time_slot");
                    req.reason        = rs.getString("reason");
                    req.status        = rs.getString("status");
                    req.studentName   = rs.getString("sname");
                    req.studentCourse = rs.getString("scourse");
                    req.studentUserId = rs.getInt("suid");
                    allRequests.add(req);
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
        JLabel title = new JLabel("APPROVE / DECLINE REQUEST");
        title.setFont(new Font("Tahoma",Font.BOLD,22)); header.add(title, BorderLayout.WEST);
        JSeparator sep = new JSeparator(); sep.setForeground(Color.LIGHT_GRAY);
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE);
        top.add(header, BorderLayout.CENTER); top.add(sep, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // tabs + refresh
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabs.setBackground(Color.WHITE); tabs.setBorder(BorderFactory.createEmptyBorder(12,24,0,24));
        btnPending  = makeTabBtn("PENDING");
        btnApproved = makeTabBtn("APPROVED");
        btnDeclined = makeTabBtn("DECLINED");
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Tahoma",Font.PLAIN,12)); refreshBtn.setFocusPainted(false);

        btnPending.addActionListener(e  -> switchTab(Tab.PENDING));
        btnApproved.addActionListener(e -> switchTab(Tab.APPROVED));
        btnDeclined.addActionListener(e -> switchTab(Tab.DECLINED));
        refreshBtn.addActionListener(e  -> refreshData());

        tabs.add(btnPending); tabs.add(btnApproved); tabs.add(btnDeclined); tabs.add(Box.createHorizontalStrut(20)); tabs.add(refreshBtn);

        listArea = new JPanel(); listArea.setLayout(new BoxLayout(listArea, BoxLayout.Y_AXIS));
        listArea.setBackground(Color.WHITE); listArea.setBorder(BorderFactory.createEmptyBorder(10,24,10,24));
        JScrollPane scroll = new JScrollPane(listArea);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout()); center.setBackground(Color.WHITE);
        center.add(tabs, BorderLayout.NORTH); center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void switchTab(Tab tab) {
        currentTab = tab;
        setTabActive(btnPending,  tab == Tab.PENDING);
        setTabActive(btnApproved, tab == Tab.APPROVED);
        setTabActive(btnDeclined, tab == Tab.DECLINED);
        renderList();
    }

    private void renderList() {
        listArea.removeAll();
        List<Request> filtered = new ArrayList<>();
        for (Request r : allRequests) {
            if (currentTab == Tab.PENDING  && "PENDING".equals(r.status))   filtered.add(r);
            if (currentTab == Tab.APPROVED && "CONFIRMED".equals(r.status)) filtered.add(r);
            if (currentTab == Tab.DECLINED && "DECLINED".equals(r.status))  filtered.add(r);
        }

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("No " + currentTab.name().toLowerCase() + " requests.");
            empty.setFont(new Font("Tahoma",Font.PLAIN,14)); empty.setForeground(Color.GRAY);
            empty.setBorder(BorderFactory.createEmptyBorder(30,0,0,0));
            listArea.add(empty);
        } else {
            for (Request req : filtered) { listArea.add(buildCard(req)); listArea.add(Box.createVerticalStrut(10)); }
        }
        listArea.revalidate(); listArea.repaint();
    }

    private JPanel buildCard(Request req) {
        JPanel card = new JPanel(new BorderLayout(10,0));
        card.setBackground(new Color(240,240,240));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180)),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // info
        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); info.setBackground(new Color(240,240,240));
        info.add(bold(req.studentName, 15));
        info.add(plain(req.studentCourse, 12));
        info.add(plain(req.day + "  –  " + req.time, 13));
        info.add(plain("Reason: " + req.reason, 12));

        // status badge
        JLabel badge = new JLabel(req.status);
        badge.setFont(new Font("Tahoma",Font.BOLD,12));
        badge.setOpaque(true); badge.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
        badge.setForeground(Color.WHITE);
        if ("PENDING".equals(req.status))   badge.setBackground(new Color(200,130,0));
        else if ("CONFIRMED".equals(req.status)) badge.setBackground(new Color(30,120,30));
        else badge.setBackground(new Color(160,30,30));

        // action buttons (only for PENDING)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actions.setBackground(new Color(240,240,240));
        actions.add(badge);
        if ("PENDING".equals(req.status)) {
            JButton approve = new JButton("APPROVE"); styleActionBtn(approve, new Color(30,120,30));
            JButton decline = new JButton("DECLINE"); styleActionBtn(decline, new Color(160,30,30));
            approve.addActionListener(e -> updateStatus(req, "CONFIRMED"));
            decline.addActionListener(e -> updateStatus(req, "DECLINED"));
            actions.add(approve); actions.add(decline);
        }

        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    // ── update status in DB + notify student ─────────────────────────────────
    private void updateStatus(Request req, String newStatus) {
        String label = "CONFIRMED".equals(newStatus) ? "APPROVE" : "DECLINE";
        int c = JOptionPane.showConfirmDialog(this,
            label + " appointment for " + req.studentName + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.get()) {
            // update appointment
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE appointments SET status=? WHERE id=?")) {
                ps.setString(1, newStatus); ps.setInt(2, req.id); ps.executeUpdate();
            }
            // log it
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO consultation_logs (appointment_id,student_name,instructor_name,"
                  + "day_of_week,time_slot,status) VALUES (?,?,?,?,?,?)")) {
                ps.setInt(1, req.id); ps.setString(2, req.studentName);
                ps.setString(3, Session.getFullName()); ps.setString(4, req.day);
                ps.setString(5, req.time); ps.setString(6, newStatus);
                ps.executeUpdate();
            }
            // notify student
            String emoji  = "CONFIRMED".equals(newStatus) ? "✅" : "❌";
            String notif  = emoji + " Your appointment request on " + req.day + " at " + req.time
                          + " with " + Session.getFullName() + " has been " + newStatus + ".";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO notifications (user_id,message) VALUES (?,?)")) {
                ps.setInt(1, req.studentUserId); ps.setString(2, notif); ps.executeUpdate();
            }

            req.status = newStatus;
            renderList();
            JOptionPane.showMessageDialog(this, "Request " + newStatus.toLowerCase() + " successfully.",
                "Done", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    private JButton makeTabBtn(String text) {
        JButton b = new JButton(text); b.setFont(new Font("Tahoma",Font.BOLD,12));
        b.setFocusPainted(false); b.setBackground(new Color(200,200,200));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY),
            BorderFactory.createEmptyBorder(6,14,6,14)));
        return b;
    }
    private void setTabActive(JButton b, boolean active) {
        b.setBackground(active ? new Color(60,60,60) : new Color(200,200,200));
        b.setForeground(active ? Color.WHITE : Color.BLACK);
    }
    private void styleActionBtn(JButton b, Color bg) {
        b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false);
        b.setFont(new Font("Tahoma",Font.BOLD,12));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(5,10,5,10)));
    }
    private JLabel bold(String text, int size)  { JLabel l=new JLabel(text); l.setFont(new Font("Segoe UI",Font.BOLD,size));  l.setBackground(new Color(240,240,240)); return l; }
    private JLabel plain(String text, int size) { JLabel l=new JLabel(text); l.setFont(new Font("Segoe UI",Font.PLAIN,size)); l.setBackground(new Color(240,240,240)); return l; }

    private void initComponents() { /* built in initUI */ }
}
