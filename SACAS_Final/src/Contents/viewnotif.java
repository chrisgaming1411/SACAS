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
 * viewnotif – shows real notifications for the logged-in user (student or instructor).
 * Unread notifications are highlighted; clicking marks them read.
 */
public class viewnotif extends javax.swing.JPanel {

    private static class Notif {
        int     id, userId;
        String  message, createdAt;
        boolean isRead;
    }

    private final List<Notif> allNotifs = new ArrayList<>();
    private JPanel  listArea;
    private JButton btnAll, btnUnread;
    private boolean showUnreadOnly = false;

    public viewnotif() {
        initComponents();
        initUI();
        refreshData();
    }

    public void refreshData() {
        loadFromDB();
        renderList();
    }

    // ── load notifications from DB ────────────────────────────────────────────
    private void loadFromDB() {
        allNotifs.clear();
        int userId = Session.getUserId();
        if (userId < 0) return;
        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT id, message, is_read, "
                       + "DATE_FORMAT(created_at,'%b %d, %Y %h:%i %p') AS ts "
                       + "FROM notifications WHERE user_id=? ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Notif n = new Notif();
                    n.id        = rs.getInt("id");
                    n.userId    = userId;
                    n.message   = rs.getString("message");
                    n.createdAt = rs.getString("ts");
                    n.isRead    = rs.getBoolean("is_read");
                    allNotifs.add(n);
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
        JLabel title = new JLabel("NOTIFICATIONS");
        title.setFont(new Font("Tahoma",Font.BOLD,22)); header.add(title, BorderLayout.WEST);
        JSeparator sep = new JSeparator(); sep.setForeground(Color.LIGHT_GRAY);
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE);
        top.add(header, BorderLayout.CENTER); top.add(sep, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        toolbar.setBackground(Color.WHITE); toolbar.setBorder(BorderFactory.createEmptyBorder(0,18,0,18));
        btnAll    = makeBtn("ALL");
        btnUnread = makeBtn("UNREAD");
        JButton markAllBtn = new JButton("Mark All as Read");
        markAllBtn.setFont(new Font("Tahoma",Font.PLAIN,12)); markAllBtn.setFocusPainted(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Tahoma",Font.PLAIN,12)); refreshBtn.setFocusPainted(false);

        btnAll.addActionListener(e    -> { showUnreadOnly=false; updateTabBtns(); renderList(); });
        btnUnread.addActionListener(e -> { showUnreadOnly=true;  updateTabBtns(); renderList(); });
        markAllBtn.addActionListener(e -> markAllRead());
        refreshBtn.addActionListener(e -> refreshData());

        toolbar.add(btnAll); toolbar.add(btnUnread);
        toolbar.add(Box.createHorizontalStrut(12)); toolbar.add(markAllBtn); toolbar.add(refreshBtn);

        listArea = new JPanel(); listArea.setLayout(new BoxLayout(listArea,BoxLayout.Y_AXIS));
        listArea.setBackground(Color.WHITE); listArea.setBorder(BorderFactory.createEmptyBorder(10,24,10,24));
        JScrollPane scroll = new JScrollPane(listArea);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout()); center.setBackground(Color.WHITE);
        center.add(toolbar, BorderLayout.NORTH); center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        updateTabBtns();
    }

    private void renderList() {
        listArea.removeAll();
        long unreadCount = allNotifs.stream().filter(n -> !n.isRead).count();

        // unread count badge in header (optional, nice UX)
        if (unreadCount > 0) {
            JLabel badge = new JLabel("  " + unreadCount + " unread  ");
            badge.setFont(new Font("Tahoma",Font.BOLD,12)); badge.setOpaque(true);
            badge.setBackground(new Color(200,50,50)); badge.setForeground(Color.WHITE);
            badge.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
            JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); badgeWrap.setBackground(Color.WHITE);
            badgeWrap.add(badge); listArea.add(badgeWrap); listArea.add(Box.createVerticalStrut(8));
        }

        int shown = 0;
        for (Notif n : allNotifs) {
            if (showUnreadOnly && n.isRead) continue;
            listArea.add(buildCard(n)); listArea.add(Box.createVerticalStrut(8)); shown++;
        }
        if (shown == 0) {
            JLabel empty = new JLabel(showUnreadOnly ? "No unread notifications." : "No notifications yet.");
            empty.setFont(new Font("Tahoma",Font.PLAIN,14)); empty.setForeground(Color.GRAY);
            empty.setBorder(BorderFactory.createEmptyBorder(30,0,0,0));
            listArea.add(empty);
        }
        listArea.revalidate(); listArea.repaint();
    }

    private JPanel buildCard(Notif n) {
        JPanel card = new JPanel(new BorderLayout(8,0));
        card.setBackground(n.isRead ? new Color(240,240,240) : new Color(255,250,220));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,n.isRead?0:4,0,0, n.isRead?Color.LIGHT_GRAY:new Color(230,150,0)),
            BorderFactory.createEmptyBorder(12,14,12,14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,90));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel msgLbl = new JLabel("<html><body style='width:400px'>" + n.message + "</body></html>");
        msgLbl.setFont(new Font("Segoe UI", n.isRead ? Font.PLAIN : Font.BOLD, 13));

        JLabel timeLbl = new JLabel(n.createdAt != null ? n.createdAt : "");
        timeLbl.setFont(new Font("Segoe UI",Font.PLAIN,11)); timeLbl.setForeground(Color.GRAY);

        JPanel text = new JPanel(); text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));
        text.setBackground(card.getBackground()); text.add(msgLbl); text.add(Box.createVerticalStrut(4)); text.add(timeLbl);

        if (!n.isRead) {
            JLabel dot = new JLabel("●"); dot.setFont(new Font("Segoe UI",Font.PLAIN,18)); dot.setForeground(new Color(230,150,0));
            card.add(dot, BorderLayout.WEST);
        }
        card.add(text, BorderLayout.CENTER);

        // click to mark read
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e)  { markRead(n); }
            public void mouseEntered(MouseEvent e)  { card.setBackground(new Color(220,220,240)); }
            public void mouseExited(MouseEvent e)   { card.setBackground(n.isRead ? new Color(240,240,240) : new Color(255,250,220)); }
        });
        return card;
    }

    private void markRead(Notif n) {
        if (n.isRead) return;
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement("UPDATE notifications SET is_read=1 WHERE id=?")) {
            ps.setInt(1, n.id); ps.executeUpdate(); n.isRead = true; renderList();
        } catch (SQLException e) { /* silently ignore */ }
    }

    private void markAllRead() {
        int userId = Session.getUserId(); if (userId < 0) return;
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement("UPDATE notifications SET is_read=1 WHERE user_id=?")) {
            ps.setInt(1, userId); ps.executeUpdate();
            allNotifs.forEach(n -> n.isRead = true); renderList();
            JOptionPane.showMessageDialog(this, "All notifications marked as read.", "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton makeBtn(String text) {
        JButton b = new JButton(text); b.setFont(new Font("Tahoma",Font.BOLD,12)); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY), BorderFactory.createEmptyBorder(5,12,5,12)));
        return b;
    }
    private void updateTabBtns() {
        btnAll.setBackground(!showUnreadOnly ? new Color(60,60,60) : new Color(210,210,210));
        btnAll.setForeground(!showUnreadOnly ? Color.WHITE : Color.BLACK);
        btnUnread.setBackground(showUnreadOnly ? new Color(60,60,60) : new Color(210,210,210));
        btnUnread.setForeground(showUnreadOnly ? Color.WHITE : Color.BLACK);
    }
    private void initComponents() { /* built in initUI */ }
}
