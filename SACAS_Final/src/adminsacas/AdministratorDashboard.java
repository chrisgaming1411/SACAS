package adminsacas;

import Contents.Usermanagement;
import Contents.Departmentconfiguration;
import Contents.Systemanalytics;
import javax.swing.*;
import java.awt.*;

/**
 * cl – Admin Dashboard frame.
 * Sidebar: USER MANAGEMENT | DEPARTMENT CONFIG | SYSTEM ANALYTICS | LOGOUT
 */
public class AdministratorDashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(AdministratorDashboard.class.getName());

    private Usermanagement       a;
    private Departmentconfiguration b;
    private Systemanalytics      c;

    public AdministratorDashboard() {
        initComponents();
        setTitle("SACAS – Admin Dashboard");
        setLocationRelativeTo(null);

        a = new Usermanagement();
        b = new Departmentconfiguration();
        c = new Systemanalytics();

        main.add(a); main.add(b); main.add(c);
        a.setVisible(false); b.setVisible(false); c.setVisible(false);
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {  // USER MANAGEMENT
        a.setVisible(true); b.setVisible(false); c.setVisible(false);
    }
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {  // DEPT CONFIG
        a.setVisible(false); b.setVisible(true); c.setVisible(false);
    }
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {  // SYSTEM ANALYTICS
        a.setVisible(false); b.setVisible(false); c.setVisible(true);
    }
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {  // LOGOUT
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Session.logout();
            new Login().setVisible(true);
            dispose();
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        darkgrey=new JPanel(); usermanage=new JButton(); depcon=new JButton();
        systemanaly=new JButton(); logout=new JButton();
        fname=new JLabel(); icon=new JLabel();
        line1=new JSeparator(); line2=new JSeparator(); line3=new JSeparator();
        main=new JLayeredPane();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        darkgrey.setBackground(new Color(102,102,102));
        darkgrey.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));

        java.net.URL userUrl = getClass().getResource("user.png");
        if (userUrl != null) icon.setIcon(new ImageIcon(userUrl));

        fname.setFont(new Font("Tahoma",Font.BOLD,14)); fname.setForeground(Color.WHITE);
        fname.setText("ADMINISTRATOR");

        Color bg = new Color(102,102,102);
        for (JButton btn : new JButton[]{usermanage,depcon,systemanaly,logout}) {
            btn.setBackground(bg); btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Tahoma",Font.BOLD,14));
            btn.setBorder(BorderFactory.createLineBorder(Color.BLACK)); btn.setFocusPainted(false);
        }
        usermanage.setText("USER MANAGEMENT");
        depcon.setText("DEPARTMENT CONFIG");
        systemanaly.setText("SYSTEM ANALYTICS");
        logout.setText("LOGOUT");

        usermanage.addActionListener(this::jButton1ActionPerformed);
        depcon.addActionListener(this::jButton2ActionPerformed);
        systemanaly.addActionListener(this::jButton3ActionPerformed);
        logout.addActionListener(this::jButton4ActionPerformed);

        darkgrey.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets=new Insets(4,10,4,10); gc.fill=GridBagConstraints.HORIZONTAL; gc.gridx=0;

        gc.gridy=0; gc.insets=new Insets(30,10,4,10); darkgrey.add(icon, gc);
        gc.gridy=1; gc.insets=new Insets(4,10,20,10); darkgrey.add(fname, gc);
        gc.gridy=2; gc.insets=new Insets(4,10,4,10);  darkgrey.add(usermanage, gc);
        gc.gridy=3; darkgrey.add(line1, gc);
        gc.gridy=4; darkgrey.add(depcon, gc);
        gc.gridy=5; darkgrey.add(line2, gc);
        gc.gridy=6; darkgrey.add(systemanaly, gc);
        gc.gridy=7; darkgrey.add(line3, gc);
        gc.gridy=8; gc.weighty=1.0; darkgrey.add(Box.createVerticalGlue(), gc); gc.weighty=0;
        gc.gridy=9; gc.insets=new Insets(4,10,30,10); darkgrey.add(logout, gc);

        main.setLayout(new CardLayout());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(darkgrey, BorderLayout.WEST);
        getContentPane().add(main,   BorderLayout.CENTER);
        setPreferredSize(new Dimension(800,630));
        pack();
    }

    public static void main(String[] args) {
        try { for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; } } }
        catch (Exception ex) { LOG.log(java.util.logging.Level.SEVERE,null,ex); }
        java.awt.EventQueue.invokeLater(() -> new AdministratorDashboard().setVisible(true));
    }

    private JButton usermanage,depcon,systemanaly,logout;
    private JLabel  fname, icon;
    private JPanel  darkgrey;
    private JSeparator line1,line2,line3;
    private JLayeredPane main;
}
