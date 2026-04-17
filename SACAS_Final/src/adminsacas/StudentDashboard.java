package adminsacas;

import Contents.*;
import javax.swing.*;
import java.awt.*;

/**
 * StudentForm – Main frame shown after student login.
 * Displays the logged-in student's name and routes to sub-panels.
 */
public class StudentDashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(StudentDashboard.class.getName());

    private viewfaculty viewfac;
    private bookapp     book;
    private manageapp   manage;
    private viewnotif   viewnot;

    public StudentDashboard() {
        initComponents();
        setTitle("SACAS – Student Dashboard");
        setLocationRelativeTo(null);

        // Show logged-in name
        fname.setText(Session.getFullName().isEmpty() ? "STUDENT" : Session.getFullName().toUpperCase());
        course.setText(Session.getExtra());   // course

        // Create panels
        viewfac = new viewfaculty();
        book    = new bookapp();
        manage  = new manageapp();
        viewnot = new viewnotif();

        main.setLayout(new CardLayout());
        main.add(viewfac, "VIEW_FACULTY");
        main.add(book,    "BOOK");
        main.add(manage,  "MANAGE");
        main.add(viewnot, "NOTIF");

        hideAll();
    }

    private void hideAll() {
        viewfac.setVisible(false); book.setVisible(false);
        manage.setVisible(false);  viewnot.setVisible(false);
    }

    // ── button handlers ──────────────────────────────────────────────────────
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {   // LOGOUT
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            Session.logout();
            new Login().setVisible(true);
            dispose();
        }
    }
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {   // VIEW FACULTY AVAILABILITY
        hideAll(); viewfac.setVisible(true); viewfac.refreshData();
    }
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {   // BOOK APPOINTMENT
        hideAll(); book.setVisible(true); book.refreshData();
    }
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {   // MANAGE APPOINTMENT
        hideAll(); manage.setVisible(true); manage.refreshData();
    }
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {   // VIEW NOTIFICATION
        hideAll(); viewnot.setVisible(true); viewnot.refreshData();
    }

    // ── UI layout ────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void initComponents() {
        darkgrey=new JPanel(); fname=new JLabel(); icon=new JLabel(); course=new JLabel();
        logout=new JButton(); availablility=new JButton(); bookapp=new JButton();
        manageapp=new JButton(); viewnotif=new JButton();
        main = new JLayeredPane();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        darkgrey.setBackground(new Color(153,153,153));
        darkgrey.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        java.net.URL userUrl = getClass().getResource("user.png");
        if (userUrl != null) icon.setIcon(new ImageIcon(userUrl));

        fname.setFont(new Font("Tahoma",Font.BOLD,14)); fname.setText("STUDENT");
        course.setFont(new Font("Tahoma",Font.PLAIN,11)); course.setForeground(new Color(240,240,240));

        Color panelBg = new Color(153,153,153);
        for (JButton b : new JButton[]{logout,availablility,bookapp,manageapp,viewnotif}) {
            b.setBackground(panelBg); b.setForeground(Color.WHITE);
            b.setFont(new Font("Tahoma",Font.BOLD,13));
            b.setBorder(BorderFactory.createLineBorder(Color.BLACK)); b.setFocusPainted(false);
        }
        logout.setText("LOGOUT");
        availablility.setText("VIEW FACULTY AVAILABILITY");
        bookapp.setText("BOOK APPOINTMENT");
        manageapp.setText("MANAGE APPOINTMENT");
        viewnotif.setText("VIEW NOTIFICATION");

        logout.addActionListener(this::jButton1ActionPerformed);
        availablility.addActionListener(this::jButton2ActionPerformed);
        bookapp.addActionListener(this::jButton3ActionPerformed);
        manageapp.addActionListener(this::jButton4ActionPerformed);
        viewnotif.addActionListener(this::jButton5ActionPerformed);

        darkgrey.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets=new Insets(4,10,4,10); gc.fill=GridBagConstraints.HORIZONTAL; gc.gridx=0;

        gc.gridy=0; gc.insets=new Insets(20,10,4,10); darkgrey.add(icon, gc);
        gc.gridy=1; gc.insets=new Insets(0,10,2,10);  darkgrey.add(fname, gc);
        gc.gridy=2; gc.insets=new Insets(0,10,16,10); darkgrey.add(course, gc);
        gc.gridy=3; gc.insets=new Insets(4,10,4,10);  darkgrey.add(availablility, gc);
        gc.gridy=4; darkgrey.add(new JSeparator(), gc);
        gc.gridy=5; darkgrey.add(bookapp, gc);
        gc.gridy=6; darkgrey.add(new JSeparator(), gc);
        gc.gridy=7; darkgrey.add(manageapp, gc);
        gc.gridy=8; darkgrey.add(new JSeparator(), gc);
        gc.gridy=9; darkgrey.add(viewnotif, gc);
        gc.gridy=10; darkgrey.add(new JSeparator(), gc);
        gc.gridy=11; gc.weighty=1.0; darkgrey.add(Box.createVerticalGlue(), gc); gc.weighty=0;
        gc.gridy=12; gc.insets=new Insets(4,10,24,10); darkgrey.add(logout, gc);

        main.setLayout(new CardLayout());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(darkgrey, BorderLayout.WEST);
        getContentPane().add(main,   BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000,660));
        pack();
    }

    public static void main(String[] args) {
        try { for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; } } }
        catch (Exception ex) { LOG.log(java.util.logging.Level.SEVERE,null,ex); }
        java.awt.EventQueue.invokeLater(() -> new StudentDashboard().setVisible(true));
    }

    private JButton logout,availablility,bookapp,manageapp,viewnotif;
    private JLabel  fname,icon,course;
    private JPanel  darkgrey;
    private JLayeredPane main;
}
