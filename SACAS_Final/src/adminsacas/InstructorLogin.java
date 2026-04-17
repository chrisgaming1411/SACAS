package adminsacas;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * InstructorForm – Instructor / Admin Login
 * Instructors use their auto-generated username + password.
 * Admin can also log in here (admin / admin123).
 */
public class InstructorLogin extends javax.swing.JFrame {

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(InstructorLogin.class.getName());

    public InstructorLogin() {
        initComponents();
        setTitle("SACAS – Instructor Login");
        setLocationRelativeTo(null);
        loadImages();
        setupPlaceholders();
    }

    private void loadImages() {
        setIcon(tmclogo, "tmclogo.png");
        setIcon(user, "user.png");
        setIcon(lock, "lock.png");
    }

    private void setIcon(JLabel lbl, String name) {
        java.net.URL url = getClass().getResource(name);
        if (url != null) lbl.setIcon(new ImageIcon(url));
    }

    private void setupPlaceholders() {
        addTFPH(username, "Enter Username");
        addPFPH(password, "Enter Password");
    }

    private static void addTFPH(JTextField tf, String ph) {
        tf.setForeground(Color.LIGHT_GRAY); tf.setText(ph);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(ph)) { tf.setText(""); tf.setForeground(Color.WHITE); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(ph); tf.setForeground(Color.LIGHT_GRAY); }
            }
        });
    }

    private static void addPFPH(JPasswordField pf, String ph) {
        pf.setForeground(Color.LIGHT_GRAY); pf.setText(ph); pf.setEchoChar((char)0);
        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (new String(pf.getPassword()).equals(ph)) { pf.setText(""); pf.setForeground(Color.WHITE); pf.setEchoChar('●'); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (pf.getPassword().length == 0) { pf.setText(ph); pf.setForeground(Color.LIGHT_GRAY); pf.setEchoChar((char)0); }
            }
        });
    }

    // ── LOGIN button ─────────────────────────────────────────────────────────
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String user = username.getText().trim();
        String pass = new String(password.getPassword()).trim();

        if (user.isEmpty() || user.equals("Enter Username")) {
            JOptionPane.showMessageDialog(this, "Please enter your username.", "Login", JOptionPane.WARNING_MESSAGE); return;
        }
        if (pass.isEmpty() || pass.equals("Enter Password")) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Login", JOptionPane.WARNING_MESSAGE); return;
        }

        // Hash the entered password to compare against stored hash
        String hashedPass = hashPassword(pass);

        try (Connection conn = DBConnection.get()) {

            // ── Admin ─────────────────────────────────────────────────────────
            if ("admin".equalsIgnoreCase(user)) {
                String sql = "SELECT id FROM users WHERE username=? AND password=? AND role='ADMIN'";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user); ps.setString(2, hashedPass);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        Session.login(rs.getInt("id"), user, "ADMIN", -1, "Administrator", "");
                        new AdministratorDashboard().setVisible(true); dispose(); return;
                    }
                }
                JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ── Instructor ────────────────────────────────────────────────────
            String sql = "SELECT u.id, i.id AS iid, "
                       + "CONCAT(i.firstname,' ',i.lastname) AS fn, i.course "
                       + "FROM users u JOIN instructors i ON i.user_id=u.id "
                       + "WHERE u.username=? AND u.password=? AND u.role='INSTRUCTOR'";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user); ps.setString(2, hashedPass);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Session.login(rs.getInt("id"), user, "INSTRUCTOR",
                                  rs.getInt("iid"), rs.getString("fn"), rs.getString("course"));
                    new InstructorDashboard().setVisible(true); dispose(); return;
                }
            }
            JOptionPane.showMessageDialog(this,
                "Invalid username or password.\nAsk your administrator for your login credentials.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── NetBeans UI ──────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void initComponents() {
        darkgrey = new JPanel(); tmclogo = new JLabel();
        grey = new JPanel(); username = new JTextField();
        password = new JPasswordField();
        jSeparator1 = new JSeparator(); jSeparator2 = new JSeparator();
        user = new JLabel(); lock = new JLabel(); login = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        darkgrey.setBackground(new Color(102,102,102));
        darkgrey.setPreferredSize(new java.awt.Dimension(348,229));
        javax.swing.GroupLayout p1 = new javax.swing.GroupLayout(darkgrey);
        darkgrey.setLayout(p1);
        p1.setHorizontalGroup(p1.createParallelGroup().addGroup(p1.createSequentialGroup().addGap(84).addComponent(tmclogo).addContainerGap(100,Short.MAX_VALUE)));
        p1.setVerticalGroup(p1.createParallelGroup().addGroup(p1.createSequentialGroup().addGap(64).addComponent(tmclogo).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));

        grey.setBackground(new Color(153,153,153));
        username.setBackground(new Color(153,153,153)); username.setFont(new Font("Tahoma",Font.BOLD,12)); username.setForeground(new Color(204,204,204)); username.setBorder(null);
        password.setBackground(new Color(153,153,153)); password.setFont(new Font("Tahoma",Font.BOLD,12)); password.setForeground(new Color(204,204,204)); password.setBorder(null);
        jSeparator1.setForeground(Color.BLACK); jSeparator2.setForeground(Color.BLACK);
        login.setBackground(new Color(102,102,102)); login.setFont(new Font("Tahoma",Font.BOLD,14)); login.setForeground(Color.WHITE); login.setText("LOGIN");
        login.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout p2 = new javax.swing.GroupLayout(grey);
        grey.setLayout(p2);
        p2.setHorizontalGroup(p2.createParallelGroup()
            .addGroup(p2.createSequentialGroup().addGap(68).addComponent(user).addGap(6)
                .addGroup(p2.createParallelGroup().addComponent(jSeparator1,160,160,160).addComponent(username,160,160,160)).addGap(0,0,Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, p2.createSequentialGroup().addGap(68,68,Short.MAX_VALUE).addComponent(lock).addGap(6)
                .addGroup(p2.createParallelGroup()
                    .addComponent(password,160,160,160)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, p2.createSequentialGroup().addComponent(login).addGap(133))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, p2.createSequentialGroup().addComponent(jSeparator2,160,160,160).addGap(84)))));
        p2.setVerticalGroup(p2.createParallelGroup()
            .addGroup(p2.createSequentialGroup().addGap(78)
                .addGroup(p2.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(user).addComponent(username,33,33,33))
                .addGap(6).addComponent(jSeparator1,10,10,10).addGap(7)
                .addGroup(p2.createParallelGroup().addComponent(lock).addComponent(password,33,33,33))
                .addGap(7).addComponent(jSeparator2,10,10,10).addGap(24)
                .addComponent(login).addContainerGap(56,Short.MAX_VALUE)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup().addComponent(darkgrey,javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE,javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0).addComponent(grey,javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE,javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0,0,Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup().addComponent(darkgrey,javax.swing.GroupLayout.DEFAULT_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE).addComponent(grey,javax.swing.GroupLayout.DEFAULT_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE));
        pack();
    }

    public static void main(String[] args) {
        try { for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; } } }
        catch (Exception ex) { LOG.log(java.util.logging.Level.SEVERE, null, ex); }
        java.awt.EventQueue.invokeLater(() -> new InstructorLogin().setVisible(true));
    }

    private JButton login;
    private JLabel user, lock, tmclogo;
    private JPanel darkgrey, grey;
    private JPasswordField password;
    private JSeparator jSeparator1, jSeparator2;
    private JTextField username;

    /**
     * Hashes a plain-text password using SHA-256.
     * Returns the hex-encoded digest (64 chars).
     * Required by SECURITY guidelines: no plain-text passwords stored in DB.
     */
    private String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
