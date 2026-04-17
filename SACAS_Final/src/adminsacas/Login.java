package adminsacas;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * tryy – Student / Admin Login
 *
 * Students enter their auto-generated username + password.
 * Admin can also log in here (username: admin, password: admin123).
 * The INSTRUCTOR button opens InstructorForm.
 */
public class Login extends javax.swing.JFrame {

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(Login.class.getName());

    public Login() {
        initComponents();
        setTitle("SACAS – Student Login");
        setLocationRelativeTo(null);
        loadImages();
        setupPlaceholders();
    }

    // ── image loading (works on any machine) ────────────────────────────────
    private void loadImages() {
        setIcon(tmclogo, "tmclogo.png");
        setIcon(lock, "lock.png");
        setIcon(user, "user.png");
    }

    private void setIcon(JLabel lbl, String name) {
        java.net.URL url = getClass().getResource(name);
        if (url != null) lbl.setIcon(new ImageIcon(url));
        else lbl.setText(name.replace(".png", ""));
    }

    // ── placeholder text behaviour ──────────────────────────────────────────
    private void setupPlaceholders() {
        addTFPlaceholder(username, "Enter Username");
        addPFPlaceholder(password, "Enter Password");
    }

    private static void addTFPlaceholder(JTextField tf, String ph) {
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

    private static void addPFPlaceholder(JPasswordField pf, String ph) {
        pf.setForeground(Color.LIGHT_GRAY); pf.setText(ph); pf.setEchoChar((char)0);
        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (new String(pf.getPassword()).equals(ph)) {
                    pf.setText(""); pf.setForeground(Color.WHITE); pf.setEchoChar('●');
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (pf.getPassword().length == 0) {
                    pf.setText(ph); pf.setForeground(Color.LIGHT_GRAY); pf.setEchoChar((char)0);
                }
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
            // Try admin first
            if ("admin".equalsIgnoreCase(user)) {
                String sql = "SELECT id FROM users WHERE username=? AND password=? AND role='ADMIN'";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user); ps.setString(2, hashedPass);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Session.login(rs.getInt("id"), user, "ADMIN", -1, "Administrator", "");
                            new AdministratorDashboard().setVisible(true); dispose(); return;
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Try student
            String sql = "SELECT u.id, s.id AS sid, CONCAT(s.firstname,' ',s.lastname) AS fn, s.course, s.schedule "
                       + "FROM users u JOIN students s ON s.user_id=u.id "
                       + "WHERE u.username=? AND u.password=? AND u.role='STUDENT'";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user); ps.setString(2, hashedPass);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Session.login(rs.getInt("id"), user, "STUDENT",
                                      rs.getInt("sid"), rs.getString("fn"),
                                      rs.getString("course"));
                        new StudentDashboard().setVisible(true); dispose(); return;
                    }
                }
            }
            JOptionPane.showMessageDialog(this,
                "Invalid username or password.\nPlease ask your administrator for your credentials.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage()
                + "\n\nMake sure:\n1. MySQL is running\n2. sacas_db.sql has been executed\n3. DBConnection.java has the correct password.",
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── INSTRUCTOR button ────────────────────────────────────────────────────
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        new InstructorLogin().setVisible(true); dispose();
    }

    // ── NetBeans-generated UI (kept intact, only image paths fixed) ──────────
    @SuppressWarnings("unchecked")
    private void initComponents() {
        darkgrey = new JPanel();
        tmclogo = new JLabel();
        grey = new JPanel();
        username = new JTextField();
        password = new JPasswordField();
        jSeparator1 = new JSeparator();
        jSeparator2 = new JSeparator();
        lock = new JLabel();
        user = new JLabel();
        instructor = new JButton();
        login = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        darkgrey.setBackground(new Color(102,102,102));
        darkgrey.setPreferredSize(new java.awt.Dimension(348,229));
        javax.swing.GroupLayout p1 = new javax.swing.GroupLayout(darkgrey);
        darkgrey.setLayout(p1);
        p1.setHorizontalGroup(p1.createParallelGroup().addGroup(p1.createSequentialGroup().addGap(92).addComponent(tmclogo,160,160,160).addContainerGap(96,Short.MAX_VALUE)));
        p1.setVerticalGroup(p1.createParallelGroup().addGroup(p1.createSequentialGroup().addGap(61).addComponent(tmclogo,160,160,160).addContainerGap()));

        grey.setBackground(new Color(153,153,153));
        username.setBackground(new Color(153,153,153)); username.setFont(new Font("Tahoma",Font.BOLD,12)); username.setForeground(new Color(204,204,204)); username.setBorder(null);
        password.setBackground(new Color(153,153,153)); password.setFont(new Font("Tahoma",Font.BOLD,12)); password.setForeground(new Color(204,204,204)); password.setBorder(null);
        jSeparator1.setForeground(Color.BLACK); jSeparator2.setForeground(Color.BLACK);
        instructor.setBackground(new Color(153,153,153)); instructor.setFont(new Font("Tahoma",Font.BOLD,12)); instructor.setForeground(Color.WHITE); instructor.setText("INSTRUCTOR");
        instructor.addActionListener(this::jButton2ActionPerformed);
        login.setBackground(new Color(153,153,153)); login.setFont(new Font("Tahoma",Font.BOLD,12)); login.setText("LOGIN");
        login.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        login.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout p2 = new javax.swing.GroupLayout(grey);
        grey.setLayout(p2);
        p2.setHorizontalGroup(p2.createParallelGroup()
            .addGroup(p2.createSequentialGroup().addGap(0,89,Short.MAX_VALUE)
                .addGroup(p2.createParallelGroup()
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, p2.createSequentialGroup()
                        .addGroup(p2.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator1,160,160,160)
                            .addGroup(p2.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING,false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, p2.createSequentialGroup().addGap(0,0,Short.MAX_VALUE).addComponent(user).addGap(6).addComponent(username,160,160,160))
                                .addGroup(p2.createSequentialGroup().addComponent(lock).addGap(6).addGroup(p2.createParallelGroup().addComponent(password,160,160,Short.MAX_VALUE).addComponent(jSeparator2)))))
                        .addGap(73))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, p2.createSequentialGroup().addComponent(instructor).addGap(113))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, p2.createSequentialGroup().addGap(0,0,Short.MAX_VALUE).addComponent(login,66,66,66).addGap(132)));
        p2.setVerticalGroup(p2.createParallelGroup()
            .addGroup(p2.createSequentialGroup().addGap(67)
                .addGroup(p2.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(username,33,33,33).addComponent(user))
                .addGap(6).addComponent(jSeparator1,7,7,7).addGap(7)
                .addGroup(p2.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(password,33,33,33).addComponent(lock))
                .addGap(6).addComponent(jSeparator2,10,10,10).addGap(18)
                .addComponent(instructor).addGap(18).addComponent(login).addContainerGap(52,Short.MAX_VALUE)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup().addComponent(darkgrey,javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE,javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0).addComponent(grey,javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE,javax.swing.GroupLayout.PREFERRED_SIZE)));
        layout.setVerticalGroup(layout.createParallelGroup().addComponent(darkgrey,javax.swing.GroupLayout.DEFAULT_SIZE,298,Short.MAX_VALUE).addComponent(grey,javax.swing.GroupLayout.DEFAULT_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE));
        pack();
    }

    public static void main(String[] args) {
        try { for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; } } }
        catch (Exception ex) { LOG.log(java.util.logging.Level.SEVERE, null, ex); }
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    private JButton login, instructor;
    private JLabel tmclogo, lock, user;
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
