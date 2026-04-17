package Contents;

import adminsacas.DBConnection;
import adminsacas.Session;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Date;


/**
 * calendar – Instructor panel: SYNC CALENDAR + UPCOMING APPOINTMENTS (real-time from DB).
 *
 * The SYNC NOW button queries the `appointments` table for all CONFIRMED and PENDING
 * appointments belonging to the logged-in instructor and displays them as notification cards.
 * The panel also auto-loads on every visit via refreshData().
 */
public class calendar extends javax.swing.JPanel {

    // ── data model ─────────────────────────────────────────────────────────────
    static class ConsultEvent {
        int    appointmentId;
        String studentName;
        String studentCourse;
        String dayOfWeek;
        String timeSlot;
        String reason;
        String status;        // PENDING or CONFIRMED
        String submittedAt;

        ConsultEvent(int id, String studentName, String studentCourse,
                     String dayOfWeek, String timeSlot,
                     String reason, String status, String submittedAt) {
            this.appointmentId = id;
            this.studentName   = studentName;
            this.studentCourse = studentCourse;
            this.dayOfWeek     = dayOfWeek;
            this.timeSlot      = timeSlot;
            this.reason        = reason;
            this.status        = status;
            this.submittedAt   = submittedAt;
        }
    }

    // ── state ──────────────────────────────────────────────────────────────────
    private boolean isSyncing       = false;
    private boolean lastSyncSuccess = true;
    private String  lastSyncTime    = "Not synced yet";

    private final List<ConsultEvent> upcomingEvents = new ArrayList<>();

    // ── UI refs ────────────────────────────────────────────────────────────────
    private JLabel  checkIcon;
    private JLabel  syncStatusLabel;
    private JLabel  syncTimeLabel;
    private JLabel  countBadge;
    private JButton syncBtn;
    private JPanel  notifPanel;
    private JComboBox<String> filterBox;

    // ──────────────────────────────────────────────────────────────────────────
    public calendar() {
        initComponents();   // NetBeans GEN stub
        buildUI();
        refreshData();      // load real data immediately
    }

    /** Called by InstructorDashboard every time the tab is opened. */
    public void refreshData() {
        loadFromDB();
        buildNotifications();
        updateSyncLabel(true);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LOAD FROM DATABASE
    // ═══════════════════════════════════════════════════════════════════════════
    private void loadFromDB() {
        upcomingEvents.clear();
        int instructorId = Session.getProfileId();
        if (instructorId < 0) return;   // not logged in yet

        try (Connection conn = DBConnection.get()) {
            /*
             * Pull PENDING and CONFIRMED appointments for this instructor.
             * Order: CONFIRMED first, then PENDING; within each group newest first.
             */
            String sql =
                "SELECT a.id, " +
                "       CONCAT(s.firstname, ' ', s.lastname) AS student_name, " +
                "       s.course AS student_course, " +
                "       a.day_of_week, " +
                "       a.time_slot, " +
                "       a.reason, " +
                "       a.status, " +
                "       DATE_FORMAT(a.created_at, '%b %d, %Y  %h:%i %p') AS submitted_at " +
                "FROM appointments a " +
                "JOIN students s ON a.student_id = s.id " +
                "WHERE a.instructor_id = ? " +
                "  AND a.status IN ('PENDING', 'CONFIRMED') " +
                "ORDER BY FIELD(a.status,'CONFIRMED','PENDING'), a.created_at DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, instructorId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    upcomingEvents.add(new ConsultEvent(
                        rs.getInt("id"),
                        rs.getString("student_name"),
                        rs.getString("student_course"),
                        rs.getString("day_of_week"),
                        rs.getString("time_slot"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getString("submitted_at")
                    ));
                }
            }
            lastSyncSuccess = true;

        } catch (SQLException e) {
            lastSyncSuccess = false;
            System.err.println("calendar.loadFromDB error: " + e.getMessage());
        }

        // update count badge
        if (countBadge != null) {
            int n = upcomingEvents.size();
            countBadge.setText(n + " appointment" + (n == 1 ? "" : "s"));
            countBadge.setVisible(n > 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ═══════════════════════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel centerCard = new JPanel();
        centerCard.setLayout(new BoxLayout(centerCard, BoxLayout.Y_AXIS));
        centerCard.setBackground(Color.WHITE);
        centerCard.setBorder(BorderFactory.createEmptyBorder(24, 60, 20, 60));

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("Calendar Sync Status");
        title.setFont(new Font("Tahoma", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setForeground(Color.LIGHT_GRAY);

        // ── Check / X icon ────────────────────────────────────────────────────
        checkIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (lastSyncSuccess) {
                    g2.setColor(new Color(34, 197, 94));
                    g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(20, 80, 60, 120);
                    g2.drawLine(60, 120, 130, 40);
                } else {
                    g2.setColor(new Color(220, 50, 50));
                    g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(25, 25, 125, 125);
                    g2.drawLine(125, 25, 25, 125);
                }
            }
        };
        checkIcon.setPreferredSize(new Dimension(150, 150));
        checkIcon.setMinimumSize (new Dimension(150, 150));
        checkIcon.setMaximumSize (new Dimension(150, 150));
        checkIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Status labels ─────────────────────────────────────────────────────
        syncStatusLabel = new JLabel("Last Sync: Not synced yet");
        syncStatusLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        syncStatusLabel.setForeground(new Color(30, 130, 30));
        syncStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        syncTimeLabel = new JLabel(" ");
        syncTimeLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        syncTimeLabel.setForeground(Color.DARK_GRAY);
        syncTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── SYNC NOW button ───────────────────────────────────────────────────
        syncBtn = new JButton("SYNC NOW") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        syncBtn.setFont(new Font("Tahoma", Font.BOLD, 20));
        syncBtn.setBackground(new Color(22, 78, 130));
        syncBtn.setForeground(Color.WHITE);
        syncBtn.setFocusPainted(false);
        syncBtn.setContentAreaFilled(false);
        syncBtn.setOpaque(false);
        syncBtn.setBorder(BorderFactory.createEmptyBorder(18, 40, 18, 40));
        syncBtn.setMaximumSize(new Dimension(500, 70));
        syncBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        syncBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        syncBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (syncBtn.isEnabled()) syncBtn.setBackground(new Color(30, 100, 160)); }
            public void mouseExited (MouseEvent e) { syncBtn.setBackground(new Color(22, 78, 130)); }
        });
        syncBtn.addActionListener(e -> performSync());

        JLabel hint = new JLabel("Click SYNC NOW to refresh upcoming appointments from the database");
        hint.setFont(new Font("Tahoma", Font.PLAIN, 12));
        hint.setForeground(Color.GRAY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Upcoming section header ───────────────────────────────────────────
        JPanel notifHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        notifHeader.setBackground(Color.WHITE);
        notifHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        notifHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel notifTitle = new JLabel("Upcoming Appointments");
        notifTitle.setFont(new Font("Tahoma", Font.BOLD, 16));

        countBadge = new JLabel("0 appointments");
        countBadge.setFont(new Font("Tahoma", Font.BOLD, 12));
        countBadge.setOpaque(true);
        countBadge.setBackground(new Color(22, 78, 130));
        countBadge.setForeground(Color.WHITE);
        countBadge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        countBadge.setVisible(false);

        // filter
        filterBox = new JComboBox<>(new String[]{"ALL", "PENDING only", "CONFIRMED only"});
        filterBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
        filterBox.addActionListener(e -> buildNotifications());

        notifHeader.add(notifTitle);
        notifHeader.add(countBadge);
        notifHeader.add(Box.createHorizontalStrut(20));
        notifHeader.add(new JLabel("Filter:"));
        notifHeader.add(filterBox);

        // ── Notification list ─────────────────────────────────────────────────
        notifPanel = new JPanel();
        notifPanel.setLayout(new BoxLayout(notifPanel, BoxLayout.Y_AXIS));
        notifPanel.setBackground(Color.WHITE);
        notifPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Assemble ──────────────────────────────────────────────────────────
        centerCard.add(title);
        centerCard.add(Box.createVerticalStrut(8));
        centerCard.add(sep);
        centerCard.add(Box.createVerticalStrut(18));
        centerCard.add(checkIcon);
        centerCard.add(Box.createVerticalStrut(18));
        centerCard.add(syncStatusLabel);
        centerCard.add(Box.createVerticalStrut(6));
        centerCard.add(syncTimeLabel);
        centerCard.add(Box.createVerticalStrut(22));
        centerCard.add(syncBtn);
        centerCard.add(Box.createVerticalStrut(8));
        centerCard.add(hint);
        centerCard.add(Box.createVerticalStrut(22));
        centerCard.add(notifHeader);
        centerCard.add(Box.createVerticalStrut(10));
        centerCard.add(notifPanel);
        centerCard.add(Box.createVerticalStrut(20));

        JScrollPane scroll = new JScrollPane(centerCard);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  BUILD NOTIFICATION CARDS
    // ═══════════════════════════════════════════════════════════════════════════
    private void buildNotifications() {
        notifPanel.removeAll();

        // apply filter
        String filter = filterBox != null ? (String) filterBox.getSelectedItem() : "ALL";
        List<ConsultEvent> visible = new ArrayList<>();
        for (ConsultEvent ev : upcomingEvents) {
            if ("ALL".equals(filter)) visible.add(ev);
            else if ("PENDING only".equals(filter)   && "PENDING".equals(ev.status))   visible.add(ev);
            else if ("CONFIRMED only".equals(filter) && "CONFIRMED".equals(ev.status)) visible.add(ev);
        }

        if (visible.isEmpty()) {
            JLabel empty = new JLabel(upcomingEvents.isEmpty()
                ? "No upcoming appointments. Click SYNC NOW to check."
                : "No appointments match the selected filter.");
            empty.setFont(new Font("Tahoma", Font.ITALIC, 13));
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            notifPanel.add(empty);
        } else {
            for (ConsultEvent ev : visible) {
                notifPanel.add(buildCard(ev));
                notifPanel.add(Box.createVerticalStrut(8));
            }
        }

        notifPanel.revalidate();
        notifPanel.repaint();
    }

    // ── Single appointment card ───────────────────────────────────────────────
    private JPanel buildCard(ConsultEvent ev) {
        boolean isConfirmed = "CONFIRMED".equals(ev.status);

        // card background: light blue for confirmed, light yellow for pending
        Color bg     = isConfirmed ? new Color(232, 248, 232) : new Color(255, 251, 230);
        Color border = isConfirmed ? new Color(100, 200, 120) : new Color(240, 190,  60);

        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, border),   // left accent stripe
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
            )
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // ── Left: bell icon ───────────────────────────────────────────────────
        JLabel bell = new JLabel(isConfirmed ? "✅" : "🔔");
        bell.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        bell.setVerticalAlignment(SwingConstants.CENTER);

        // ── Center: info ──────────────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(bg);

        JLabel nameLbl = new JLabel(ev.studentName
            + (ev.studentCourse != null && !ev.studentCourse.isEmpty()
               ? "  (" + ev.studentCourse + ")" : ""));
        nameLbl.setFont(new Font("Tahoma", Font.BOLD, 14));

        JLabel slotLbl = new JLabel("📅  " + ev.dayOfWeek + "   🕒  " + ev.timeSlot);
        slotLbl.setFont(new Font("Tahoma", Font.PLAIN, 12));
        slotLbl.setForeground(new Color(40, 40, 40));

        JLabel reasonLbl = new JLabel("Reason: " + ev.reason);
        reasonLbl.setFont(new Font("Tahoma", Font.ITALIC, 12));
        reasonLbl.setForeground(Color.DARK_GRAY);

        JLabel submittedLbl = new JLabel("Submitted: " + ev.submittedAt);
        submittedLbl.setFont(new Font("Tahoma", Font.PLAIN, 11));
        submittedLbl.setForeground(Color.GRAY);

        info.add(nameLbl);
        info.add(Box.createVerticalStrut(3));
        info.add(slotLbl);
        info.add(reasonLbl);
        info.add(submittedLbl);

        // ── Right: status badge ───────────────────────────────────────────────
        JLabel badge = new JLabel(ev.status);
        badge.setFont(new Font("Tahoma", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setForeground(Color.WHITE);
        badge.setBackground(isConfirmed ? new Color(30, 150, 60) : new Color(200, 130, 0));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setVerticalAlignment(SwingConstants.CENTER);

        card.add(bell,  BorderLayout.WEST);
        card.add(info,  BorderLayout.CENTER);
        card.add(badge, BorderLayout.EAST);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SYNC NOW  – re-queries DB on a background thread, then refreshes UI
    // ═══════════════════════════════════════════════════════════════════════════
    private void performSync() {
        if (isSyncing) return;
        isSyncing = true;
        syncBtn.setEnabled(false);
        syncBtn.setText("Syncing…");
        syncBtn.setBackground(new Color(100, 140, 180));
        checkIcon.setVisible(false);
        syncStatusLabel.setText("Syncing with database…");
        syncStatusLabel.setForeground(Color.DARK_GRAY);
        syncTimeLabel.setText("Please wait…");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                loadFromDB();           // DB query on background thread
                return lastSyncSuccess;
            }

            @Override
            protected void done() {
                try { lastSyncSuccess = get(); }
                catch (Exception ex) { lastSyncSuccess = false; }

                lastSyncTime = new SimpleDateFormat("MMMM d, yyyy, h:mm a").format(new Date());

                // restore UI
                checkIcon.setVisible(true);
                checkIcon.repaint();
                updateSyncLabel(lastSyncSuccess);

                syncBtn.setText("SYNC NOW");
                syncBtn.setBackground(new Color(22, 78, 130));
                syncBtn.setEnabled(true);
                isSyncing = false;

                buildNotifications();
                showSyncPopup();
            }
        };
        worker.execute();
    }

    private void updateSyncLabel(boolean success) {
        lastSyncTime = new SimpleDateFormat("MMMM d, yyyy, h:mm a").format(new Date());
        syncStatusLabel.setText("Last Sync: " + (success ? "SUCCESSFUL" : "FAILED"));
        syncStatusLabel.setForeground(success ? new Color(30, 130, 30) : new Color(200, 0, 0));
        syncTimeLabel.setText(lastSyncTime);
        checkIcon.repaint();
    }

    private void showSyncPopup() {
        if (!lastSyncSuccess) {
            JOptionPane.showMessageDialog(this,
                "Sync failed. Please check your database connection.",
                "Sync Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long pending   = upcomingEvents.stream().filter(e -> "PENDING".equals(e.status)).count();
        long confirmed = upcomingEvents.stream().filter(e -> "CONFIRMED".equals(e.status)).count();

        StringBuilder sb = new StringBuilder(
            "<html><b>✔ Calendar synced successfully!</b><br><br>"
            + "<b>Upcoming Appointments:</b><br>"
            + "&nbsp;&nbsp;• Confirmed : <b>" + confirmed + "</b><br>"
            + "&nbsp;&nbsp;• Pending   : <b>" + pending   + "</b><br><br>"
        );
        if (!upcomingEvents.isEmpty()) {
            sb.append("<b>Next Appointments:</b><br>");
            int shown = 0;
            for (ConsultEvent ev : upcomingEvents) {
                if (shown++ >= 5) { sb.append("&nbsp;&nbsp;• …and more<br>"); break; }
                sb.append("&nbsp;&nbsp;• <b>").append(ev.studentName).append("</b>  ")
                  .append(ev.dayOfWeek).append(", ").append(ev.timeSlot)
                  .append("  <i>[").append(ev.status).append("]</i><br>");
            }
        } else {
            sb.append("No upcoming appointments at this time.<br>");
        }
        sb.append("</html>");
        JOptionPane.showMessageDialog(this, sb.toString(),
            "Sync Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Public hook: called by approvedecline after instructor approves ────────
    public void addConsultEvent(String studentName, String dayOfWeek,
                                 String timeSlot, String reason) {
        upcomingEvents.add(0, new ConsultEvent(
            -1, studentName, "", dayOfWeek, timeSlot, reason, "CONFIRMED",
            new SimpleDateFormat("MMM dd, yyyy  hh:mm a").format(new Date())));
        buildNotifications();
    }

    // ── NetBeans GEN block (preserved) ────────────────────────────────────────
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(204, 204, 204));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("SYNC CALENDAR");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(575, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(503, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
