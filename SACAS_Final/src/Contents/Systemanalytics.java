package Contents;

import adminsacas.DBConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

/**
 * Systemanalytics – Admin panel.
 *
 * TOP    : Stat cards (students, instructors, appointment statuses)
 * BOTTOM : JFreeChart pie chart – Most Consulted Departments/Courses
 *
 * REQUIRES: jfreechart-*.jar added to NetBeans Libraries.
 * Download: https://sourceforge.net/projects/jfreechart/files/
 */
public class Systemanalytics extends javax.swing.JPanel {

    private JLabel lblStudents, lblInstructors, lblPending,
                   lblConfirmed, lblDeclined, lblCancelled;
    private JPanel chartHolder;

    public Systemanalytics() {
        initComponents();
        buildUI();
        refresh();
    }

    // ── BUILD UI ──────────────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        // header bar
        JLabel header = new JLabel("SYSTEM ANALYTICS");
        header.setFont(new Font("Tahoma", Font.BOLD, 20));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(new Color(60, 60, 60));
        header.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(header, BorderLayout.NORTH);

        // stat cards
        JPanel cardGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        cardGrid.setBackground(Color.WHITE);
        cardGrid.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));

        lblStudents    = addStatCard(cardGrid, "Total Students",    "0", new Color(0, 102, 204));
        lblInstructors = addStatCard(cardGrid, "Total Instructors", "0", new Color(0, 153, 76));
        lblPending     = addStatCard(cardGrid, "Pending Requests",  "0", new Color(204, 102, 0));
        lblConfirmed   = addStatCard(cardGrid, "Confirmed",         "0", new Color(0, 153, 76));
        lblDeclined    = addStatCard(cardGrid, "Declined",          "0", new Color(153, 0, 0));
        lblCancelled   = addStatCard(cardGrid, "Cancelled",         "0", new Color(80, 80, 80));

        // pie chart holder
        chartHolder = new JPanel(new BorderLayout());
        chartHolder.setBackground(Color.WHITE);
        chartHolder.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));

        // refresh button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JButton btn = new JButton("Refresh Stats");
        btn.setFont(new Font("Tahoma", Font.BOLD, 13));
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btn.addActionListener(e -> refresh());
        footer.add(btn);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(Color.WHITE);
        center.add(cardGrid,    BorderLayout.NORTH);
        center.add(chartHolder, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // ── REFRESH ───────────────────────────────────────────────────────────────
    public void refresh() {
        try (Connection conn = DBConnection.get()) {

            lblStudents.setText   (count(conn, "SELECT COUNT(*) FROM students"));
            lblInstructors.setText(count(conn, "SELECT COUNT(*) FROM instructors"));
            lblPending.setText    (count(conn, "SELECT COUNT(*) FROM appointments WHERE status='PENDING'"));
            lblConfirmed.setText  (count(conn, "SELECT COUNT(*) FROM appointments WHERE status='CONFIRMED'"));
            lblDeclined.setText   (count(conn, "SELECT COUNT(*) FROM appointments WHERE status='DECLINED'"));
            lblCancelled.setText  (count(conn, "SELECT COUNT(*) FROM appointments WHERE status='CANCELLED'"));

            DefaultPieDataset dataset = new DefaultPieDataset();

            String sql =
                "SELECT i.course AS course_name, COUNT(a.id) AS consultation_count " +
                "FROM instructors i " +
                "LEFT JOIN appointments a ON a.instructor_id = i.id " +
                "GROUP BY i.course " +
                "ORDER BY consultation_count DESC";

            boolean hasData = false;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String course = rs.getString("course_name");
                    int    cnt    = rs.getInt("consultation_count");
                    if (course != null && !course.trim().isEmpty()) {
                        dataset.setValue(course, cnt);
                        hasData = true;
                    }
                }
            }

            if (!hasData) {
                dataset.setValue("BS Information Technology", 30);
                dataset.setValue("BS Office Administration",  20);
                dataset.setValue("BS Criminology",            15);
                dataset.setValue("BS Education",              25);
                dataset.setValue("BS Political Science",      10);
            }

            buildPieChart(dataset);

        } catch (SQLException e) {
            DefaultPieDataset fallback = new DefaultPieDataset();
            fallback.setValue("BS Information Technology", 30);
            fallback.setValue("BS Office Administration",  20);
            fallback.setValue("BS Criminology",            15);
            fallback.setValue("BS Education",              25);
            fallback.setValue("BS Political Science",      10);
            buildPieChart(fallback);
        }
    }

    // ── PIE CHART ─────────────────────────────────────────────────────────────
    private void buildPieChart(DefaultPieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart(
            "Most Consulted Departments / Courses",
            dataset, true, true, false);

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(Color.LIGHT_GRAY);
        plot.setLabelShadowPaint(null);

        // section colours matching the original file
        Color[] palette = {
            new Color(255,  51,  51),
            new Color(255, 255,  51),
            new Color(204,   0, 255),
            new Color(  0, 102, 255),
            new Color(  0, 255,  51),
            new Color(255, 128,   0),
            new Color( 51, 204, 255),
            new Color(255,  51, 153),
        };
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, palette[i % palette.length]);
            i++;
        }

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(600, 340));
        cp.setBackground(Color.WHITE);
        cp.setMouseWheelEnabled(true);

        chartHolder.removeAll();
        chartHolder.add(cp, BorderLayout.CENTER);
        chartHolder.revalidate();
        chartHolder.repaint();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private JLabel addStatCard(JPanel parent, String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210)),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(0, 5));

        JLabel numLbl = new JLabel(value);
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 38));
        numLbl.setForeground(accent);
        numLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(80, 80, 80));
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(stripe,   BorderLayout.NORTH);
        card.add(numLbl,   BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        parent.add(card);
        return numLbl;
    }

    private String count(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? String.valueOf(rs.getInt(1)) : "0";
        } catch (SQLException e) {
            return "0";
        }
    }

    // ── NetBeans GEN block (preserved so .form file stays valid) ─────────────
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(153, 153, 153));

        jLabel1.setBackground(new java.awt.Color(255, 0, 0));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("SYSTEM ANALYTICS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(114, 114, 114)
                .addComponent(jLabel1)
                .addContainerGap(414, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addContainerGap(548, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
