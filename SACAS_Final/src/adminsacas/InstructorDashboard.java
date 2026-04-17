package adminsacas;

import Contents.*;
import javax.swing.*;
import java.awt.*;

/**
 * InstructorDashboard – Main frame shown after instructor login.
 * Displays the logged-in instructor's name and routes to sub-panels.
 */
public class InstructorDashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(InstructorDashboard.class.getName());

    private Setavailability set;
    private approvedecline  app;
    private viewlogs        view;
    private calendar        cal;

    public InstructorDashboard() {
        initComponents();
        setTitle("SACAS – Instructor Dashboard");
        setLocationRelativeTo(null);

        // Show logged-in name
        fname.setText(Session.getFullName().isEmpty() ? "INSTRUCTOR" : Session.getFullName().toUpperCase());

        // Create panels
        set  = new Setavailability();
        app  = new approvedecline();
        view = new viewlogs();
        cal  = new calendar();

        main.setLayout(new CardLayout());
        main.add(set,  "SET_AVAIL");
        main.add(app,  "APPROVE");
        main.add(view, "LOGS");
        main.add(cal,  "CALENDAR");

        hideAll();
    }

    private void hideAll() {
        set.setVisible(false); app.setVisible(false);
        view.setVisible(false); cal.setVisible(false);
    }

    // ── button handlers ──────────────────────────────────────────────────────
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {   // LOGOUT
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            Session.logout();
            new InstructorLogin().setVisible(true);
            dispose();
        }
    }
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {   // SET AVAILABILITY
        hideAll(); set.setVisible(true); set.loadFromDB();
    }
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {   // APPROVE/DECLINE
        hideAll(); app.setVisible(true); app.refreshData();
    }
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {   // VIEW LOGS
        hideAll(); view.setVisible(true); view.refreshData();
    }
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {   // SYNC CALENDAR
        hideAll(); cal.setVisible(true);
    }

    // ── UI (mirrors original NetBeans layout) ────────────────────────────────
    @SuppressWarnings("unchecked")
    private void initComponents() {
        darkgrey = new JPanel(); icon = new JLabel(); fname = new JLabel();
        line1=new JSeparator(); line2=new JSeparator();
        line3=new JSeparator(); line4=new JSeparator();
        logout=new JButton(); setavail=new JButton(); appdec=new JButton();
        viewcon=new JButton(); sync=new JButton();
        main = new JLayeredPane();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        darkgrey.setBackground(new Color(153,153,153));
        darkgrey.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // avatar icon
        java.net.URL userUrl = getClass().getResource("user.png");
        if (userUrl != null) icon.setIcon(new ImageIcon(userUrl));

        fname.setFont(new Font("Tahoma",Font.BOLD,14));
        fname.setText("INSTRUCTOR");

        Color panelBg = new Color(153,153,153);
        for (JButton b : new JButton[]{logout,setavail,appdec,viewcon,sync}) {
            b.setBackground(panelBg); b.setForeground(Color.WHITE);
            b.setFont(new Font("Tahoma",Font.BOLD,13));
            b.setBorder(BorderFactory.createLineBorder(Color.BLACK)); b.setFocusPainted(false);
        }
        logout.setText("LOGOUT");
        setavail.setText("SET AVAILABILITY");
        appdec.setText("APPROVE/DECLINE REQUEST");
        viewcon.setText("VIEW CONSULTATION LOGS");
        sync.setText("SYNC CALENDAR");

        logout.addActionListener(this::jButton1ActionPerformed);
        setavail.addActionListener(this::jButton2ActionPerformed);
        appdec.addActionListener(this::jButton3ActionPerformed);
        viewcon.addActionListener(this::jButton4ActionPerformed);
        sync.addActionListener(this::jButton5ActionPerformed);

        // sidebar layout
        darkgrey.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,10,4,10); gc.fill = GridBagConstraints.HORIZONTAL; gc.gridx=0;

        gc.gridy=0; gc.insets=new Insets(20,10,4,10); darkgrey.add(icon, gc);
        gc.gridy=1; gc.insets=new Insets(4,10,16,10); darkgrey.add(fname, gc);
        gc.gridy=2; gc.insets=new Insets(4,10,4,10);  darkgrey.add(setavail, gc);
        gc.gridy=3; darkgrey.add(new JSeparator(), gc);
        gc.gridy=4; darkgrey.add(appdec, gc);
        gc.gridy=5; darkgrey.add(new JSeparator(), gc);
        gc.gridy=6; darkgrey.add(viewcon, gc);
        gc.gridy=7; darkgrey.add(new JSeparator(), gc);
        gc.gridy=8; darkgrey.add(sync, gc);
        gc.gridy=9; darkgrey.add(new JSeparator(), gc);
        // spacer
        gc.gridy=10; gc.weighty=1.0; darkgrey.add(Box.createVerticalGlue(), gc); gc.weighty=0;
        gc.gridy=11; gc.insets=new Insets(4,10,24,10); darkgrey.add(logout, gc);

        main.setLayout(new CardLayout());

        // root layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(darkgrey, BorderLayout.WEST);
        getContentPane().add(main,   BorderLayout.CENTER);
        setPreferredSize(new Dimension(980,650));
        pack();
    }

    public static void main(String[] args) {
        try { for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; } } }
        catch (Exception ex) { LOG.log(java.util.logging.Level.SEVERE,null,ex); }
        java.awt.EventQueue.invokeLater(() -> new InstructorDashboard().setVisible(true));
    }

    private JButton logout,setavail,appdec,viewcon,sync;
    private JLabel  icon, fname;
    private JPanel  darkgrey;
    private JSeparator line1,line2,line3,line4;
    private JLayeredPane main;
}
