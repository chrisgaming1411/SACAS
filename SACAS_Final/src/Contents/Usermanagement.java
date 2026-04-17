package Contents;

import adminsacas.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Usermanagement – Admin panel for adding Students and Instructors.
 *
 * KEY BEHAVIOUR:
 *   When the admin fills in the form and clicks SAVE:
 *   1. A unique student_no / instructor_no is generated  (e.g. STU-2026-001)
 *   2. A username is generated  →  first-initial + lastname + last-3-of-ID
 *      e.g.  Juan Dela Cruz, STU-2026-001  →  jdelacruz001
 *   3. A password is generated  →  firstname(lower) + "@" + last-4-digits
 *      e.g.  juan@2001
 *   4. A dialog shows the credentials to the admin to hand to the user.
 *   5. Both records are saved in the DB (users + students / instructors).
 *
 * This file preserves 100 % of the original NetBeans form UI code (tabs,
 * fields, combo-boxes) and only replaces the two save-button handlers.
 */
public class Usermanagement extends javax.swing.JPanel {

    public Usermanagement() {
        initComponents();
        setupCombos();
    }

    // ── combo-box population (unchanged from original) ───────────────────────
    private void setupCombos() {
        String[] times = {"Select Time...","07:30 AM - 09:00 AM","09:00 AM - 10:30 AM","10:30 AM - 12:00 PM"};
        for (String t : times) jComboBox5.addItem(t);
        jComboBox5.setSelectedIndex(0);

        String[] days = {"Select Schedule...","Monday","Tuesday","Wednesday","Thursday","Friday","Monday - Wednesday - Friday","Tuesday - Thursday"};
        for (String d : days) jComboBox4.addItem(d);
        jComboBox4.setSelectedIndex(0);

        String[] courses = {"Select Course...","BS Information Technology","BS Criminology","BS Education","BS Office Administration","BS Political Science"};
        for (String c : courses) jComboBox3.addItem(c);
        jComboBox3.setSelectedIndex(0);

        String[] suffix = {"None","Jr.","Sr.","II","III","IV","V"};
        for (String s : suffix) { jComboBox1.addItem(s); jComboBox2.addItem(s); }
        jComboBox1.setSelectedIndex(0); jComboBox2.setSelectedIndex(0);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SAVE STUDENT  (jButton7)
    // ════════════════════════════════════════════════════════════════════════
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
        // collect fields
        String firstname  = jTextField1.getText().trim();
        String middlename = jTextField2.getText().trim();
        String lastname   = jTextField3.getText().trim();
        String suffix     = jComboBox1.getSelectedItem().toString().trim();
        String address    = jTextField14.getText().trim();
        String ageStr     = jTextField4.getText().trim();
        String birthdate  = jTextField12.getText().trim();
        String motherName = jTextField5.getText().trim();
        String motherOcc  = jTextField6.getText().trim();
        String fatherName = jTextField7.getText().trim();
        String fatherOcc  = jTextField8.getText().trim();
        String elementary = jTextField9.getText().trim();
        String juniorHigh = jTextField10.getText().trim();
        String seniorHigh = jTextField11.getText().trim();

        // validate
        if (firstname.isEmpty() || lastname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name and Last Name are required.", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        if (ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Age is required.", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        if (birthdate.isEmpty() || birthdate.equals("YYYY/MM/DD")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Birthdate (YYYY/MM/DD).", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        int age;
        try { age = Integer.parseInt(ageStr); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE); return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Save student record for: " + firstname + " " + lastname + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.get()) {
            conn.setAutoCommit(false);
            try {
                // 1. generate IDs
                String studentNo = nextStudentNo(conn);
                String username  = generateUsername(firstname, lastname, studentNo, conn);
                String password  = generatePassword(firstname, studentNo);

                // 2. hash password before storing (SHA-256)
                String hashedPassword = hashPassword(password);

                // 3. insert users row
                int userId = insertUser(conn, username, hashedPassword, "STUDENT");

                // 3. insert students row
                String sql = "INSERT INTO students "
                    + "(user_id,student_no,firstname,middlename,lastname,suffix,"
                    + "address,age,birthdate,mothers_name,mothers_occ,fathers_name,"
                    + "fathers_occ,elementary,junior_high,senior_high) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt   (1, userId);   ps.setString(2,  studentNo);
                    ps.setString(3, firstname); ps.setString(4,  middlename);
                    ps.setString(5, lastname);  ps.setString(6,  "None".equals(suffix) ? "" : suffix);
                    ps.setString(7, address);   ps.setInt   (8,  age);
                    ps.setString(9, birthdate); ps.setString(10, motherName);
                    ps.setString(11, motherOcc); ps.setString(12, fatherName);
                    ps.setString(13, fatherOcc); ps.setString(14, elementary);
                    ps.setString(15, juniorHigh); ps.setString(16, seniorHigh);
                    ps.executeUpdate();
                }

                conn.commit();
                showCredentials("Student", firstname + " " + lastname, studentNo, username, password);
                clearStudentForm();

            } catch (SQLException ex) { conn.rollback(); throw ex; }
            finally { conn.setAutoCommit(true); }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SAVE INSTRUCTOR  (jButton6)
    // ════════════════════════════════════════════════════════════════════════
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
        String firstname  = jTextField15.getText().trim();
        String middlename = jTextField16.getText().trim();
        String lastname   = jTextField17.getText().trim();
        String suffix     = jComboBox2.getSelectedItem().toString().trim();
        String address    = jTextField18.getText().trim();
        String ageStr     = jTextField25.getText().trim();
        String birthdate  = jTextField20.getText().trim();
        String course     = jComboBox3.getSelectedItem().toString().trim();
        String schedule   = jComboBox4.getSelectedItem().toString().trim();
        String time       = jComboBox5.getSelectedItem().toString().trim();
        String subject1   = jTextField21.getText().trim();
        String subject2   = jTextField22.getText().trim();
        String subject3   = jTextField23.getText().trim();
        String subject4   = jTextField24.getText().trim();

        if (firstname.isEmpty() || lastname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name and Last Name are required.", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        if (ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Age is required.", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        if (birthdate.isEmpty() || birthdate.equals("YYYY/MM/DD")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Birthdate.", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        if ("Select Course...".equals(course)) {
            JOptionPane.showMessageDialog(this, "Please select a Course/Department.", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        int age;
        try { age = Integer.parseInt(ageStr); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE); return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Save instructor record for: " + firstname + " " + lastname + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.get()) {
            conn.setAutoCommit(false);
            try {
                String instructorNo = nextInstructorNo(conn);
                String username     = generateUsername(firstname, lastname, instructorNo, conn);
                String password     = generatePassword(firstname, instructorNo);

                // Hash password before storing (SHA-256)
                String hashedPassword = hashPassword(password);

                int userId = insertUser(conn, username, hashedPassword, "INSTRUCTOR");

                String sql = "INSERT INTO instructors "
                    + "(user_id,instructor_no,firstname,middlename,lastname,suffix,"
                    + "address,age,birthdate,course,schedule,timeslot,"
                    + "subject1,subject2,subject3,subject4) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt   (1, userId);      ps.setString(2,  instructorNo);
                    ps.setString(3, firstname);   ps.setString(4,  middlename);
                    ps.setString(5, lastname);    ps.setString(6,  "None".equals(suffix)?"":suffix);
                    ps.setString(7, address);     ps.setInt   (8,  age);
                    ps.setString(9, birthdate);   ps.setString(10, course);
                    ps.setString(11, schedule);   ps.setString(12, time);
                    ps.setString(13, subject1);   ps.setString(14, subject2);
                    ps.setString(15, subject3);   ps.setString(16, subject4);
                    ps.executeUpdate();
                }

                conn.commit();
                showCredentials("Instructor", firstname + " " + lastname, instructorNo, username, password);
                clearInstructorForm();

            } catch (SQLException ex) { conn.rollback(); throw ex; }
            finally { conn.setAutoCommit(true); }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── credential helpers ───────────────────────────────────────────────────

    /** Generates next sequential student number: STU-YYYY-NNN */
    private String nextStudentNo(Connection conn) throws SQLException {
        String yr  = String.valueOf(java.time.Year.now().getValue());
        String sql = "SELECT COUNT(*) FROM students WHERE student_no LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "STU-" + yr + "-%");
            ResultSet rs = ps.executeQuery(); rs.next();
            return String.format("STU-%s-%03d", yr, rs.getInt(1) + 1);
        }
    }

    /** Generates next sequential instructor number: INS-YYYY-NNN */
    private String nextInstructorNo(Connection conn) throws SQLException {
        String yr  = String.valueOf(java.time.Year.now().getValue());
        String sql = "SELECT COUNT(*) FROM instructors WHERE instructor_no LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "INS-" + yr + "-%");
            ResultSet rs = ps.executeQuery(); rs.next();
            return String.format("INS-%s-%03d", yr, rs.getInt(1) + 1);
        }
    }

    /**
     * Username = (first char of firstname)(lastname)(last 3 digits of ID#)
     * All lowercase, no spaces.  A counter suffix is appended if duplicate.
     */
    private String generateUsername(String first, String last, String idNo, Connection conn) throws SQLException {
        String f = first.replaceAll("[^a-zA-Z]","").toLowerCase();
        String l = last .replaceAll("[^a-zA-Z]","").toLowerCase();
        String digits = idNo.replaceAll("[^0-9]","");
        String tail   = digits.length() >= 3 ? digits.substring(digits.length()-3) : digits;
        String base   = (f.isEmpty() ? "u" : String.valueOf(f.charAt(0))) + l + tail;
        String candidate = base;
        int    counter   = 2;
        while (usernameExists(candidate, conn)) candidate = base + (counter++);
        return candidate;
    }

    private boolean usernameExists(String username, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
            ps.setString(1, username); ResultSet rs = ps.executeQuery(); return rs.next();
        }
    }

    /**
     * Password = firstname(lower) + "@" + last 4 digits of ID#
     * e.g. juan@2001
     */
    private String generatePassword(String firstname, String idNo) {
        String f      = firstname.toLowerCase().replaceAll("\\s+","");
        String digits = idNo.replaceAll("[^0-9]","");
        String tail   = digits.length() >= 4 ? digits.substring(digits.length()-4) : digits;
        return f + "@" + tail;
    }

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

    /** Inserts a row into `users` and returns the generated PK. */
    private int insertUser(Connection conn, String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username); ps.setString(2, password); ps.setString(3, role);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys(); keys.next(); return keys.getInt(1);
        }
    }

    /** Shows a pop-up with the new login credentials for the admin to record. */
    private void showCredentials(String type, String fullName, String idNo, String username, String password) {
        String msg = String.format(
            "✅  %s registered successfully!\n\n"
          + "  Full Name  : %s\n"
          + "  ID Number  : %s\n"
          + "─────────────────────────────────\n"
          + "  USERNAME   : %s\n"
          + "  PASSWORD   : %s\n"
          + "─────────────────────────────────\n"
          + "Please give these credentials to the %s.",
            type, fullName, idNo, username, password, type.toLowerCase());
        JOptionPane.showMessageDialog(this, msg, "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearStudentForm() {
        jTextField1.setText(""); jTextField2.setText(""); jTextField3.setText("");
        jComboBox1.setSelectedIndex(0); jTextField14.setText("");
        jTextField4.setText(""); jTextField12.setText("YYYY/MM/DD");
        jTextField5.setText(""); jTextField6.setText(""); jTextField7.setText("");
        jTextField8.setText(""); jTextField9.setText(""); jTextField10.setText(""); jTextField11.setText("");
    }

    private void clearInstructorForm() {
        jTextField15.setText(""); jTextField16.setText(""); jTextField17.setText("");
        jComboBox2.setSelectedIndex(0); jTextField18.setText("");
        jTextField25.setText(""); jTextField20.setText("YYYY/MM/DD");
        jComboBox3.setSelectedIndex(0); jComboBox4.setSelectedIndex(0); jComboBox5.setSelectedIndex(0);
        jTextField21.setText(""); jTextField22.setText(""); jTextField23.setText(""); jTextField24.setText("");
    }

    // ── NetBeans-generated initComponents (preserving original UI) ───────────
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jTabbedPane1 = new JTabbedPane();

        // ─── STUDENT tab ─────────────────────────────────────────────────────
        JPanel studentTab = new JPanel(new GridBagLayout());
        studentTab.setBackground(new Color(200,200,200));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,8,4,8); gc.fill = GridBagConstraints.HORIZONTAL;

        jTextField1 = addLabeledField(studentTab, gc, 0, "First Name:");
        jTextField2 = addLabeledField(studentTab, gc, 1, "Middle Name:");
        jTextField3 = addLabeledField(studentTab, gc, 2, "Last Name:");

        gc.gridx=0; gc.gridy=3; studentTab.add(new JLabel("Suffix:"), gc);
        jComboBox1 = new JComboBox<>();
        gc.gridx=1; gc.gridy=3; studentTab.add(jComboBox1, gc);

        jTextField14 = addLabeledField(studentTab, gc, 4, "Address:");
        jTextField4  = addLabeledField(studentTab, gc, 5, "Age:");
        jTextField12 = addLabeledField(studentTab, gc, 6, "Birthdate (YYYY/MM/DD):");
        jTextField12.setText("YYYY/MM/DD");
        jTextField5  = addLabeledField(studentTab, gc, 7, "Mother's Name:");
        jTextField6  = addLabeledField(studentTab, gc, 8, "Mother's Occupation:");
        jTextField7  = addLabeledField(studentTab, gc, 9, "Father's Name:");
        jTextField8  = addLabeledField(studentTab, gc,10, "Father's Occupation:");
        jTextField9  = addLabeledField(studentTab, gc,11, "Elementary:");
        jTextField10 = addLabeledField(studentTab, gc,12, "Junior High:");
        jTextField11 = addLabeledField(studentTab, gc,13, "Senior High:");

        jButton7 = new JButton("SAVE STUDENT");
        styleBtn(jButton7, new Color(60,100,60));
        jButton7.addActionListener(this::jButton7ActionPerformed);
        gc.gridx=0; gc.gridy=14; gc.gridwidth=2; gc.insets=new Insets(12,8,8,8);
        studentTab.add(jButton7, gc); gc.gridwidth=1;

        JScrollPane sp1 = new JScrollPane(studentTab);
        sp1.setBorder(BorderFactory.createEmptyBorder());
        jTabbedPane1.addTab("ADD STUDENT", sp1);

        // ─── INSTRUCTOR tab ──────────────────────────────────────────────────
        JPanel instructorTab = new JPanel(new GridBagLayout());
        instructorTab.setBackground(new Color(200,200,200));
        gc = new GridBagConstraints();
        gc.insets = new Insets(4,8,4,8); gc.fill = GridBagConstraints.HORIZONTAL;

        jTextField15 = addLabeledField(instructorTab, gc, 0, "First Name:");
        jTextField16 = addLabeledField(instructorTab, gc, 1, "Middle Name:");
        jTextField17 = addLabeledField(instructorTab, gc, 2, "Last Name:");

        gc.gridx=0; gc.gridy=3; instructorTab.add(new JLabel("Suffix:"), gc);
        jComboBox2 = new JComboBox<>();
        gc.gridx=1; gc.gridy=3; instructorTab.add(jComboBox2, gc);

        jTextField18 = addLabeledField(instructorTab, gc, 4, "Address:");
        jTextField25 = addLabeledField(instructorTab, gc, 5, "Age:");
        jTextField20 = addLabeledField(instructorTab, gc, 6, "Birthdate (YYYY/MM/DD):");
        jTextField20.setText("YYYY/MM/DD");

        gc.gridx=0; gc.gridy=7; instructorTab.add(new JLabel("Course/Department:"), gc);
        jComboBox3 = new JComboBox<>();
        gc.gridx=1; gc.gridy=7; instructorTab.add(jComboBox3, gc);

        gc.gridx=0; gc.gridy=8; instructorTab.add(new JLabel("Schedule:"), gc);
        jComboBox4 = new JComboBox<>();
        gc.gridx=1; gc.gridy=8; instructorTab.add(jComboBox4, gc);

        gc.gridx=0; gc.gridy=9; instructorTab.add(new JLabel("Time Slot:"), gc);
        jComboBox5 = new JComboBox<>();
        gc.gridx=1; gc.gridy=9; instructorTab.add(jComboBox5, gc);

        jTextField21 = addLabeledField(instructorTab, gc,10, "Subject 1:");
        jTextField22 = addLabeledField(instructorTab, gc,11, "Subject 2:");
        jTextField23 = addLabeledField(instructorTab, gc,12, "Subject 3:");
        jTextField24 = addLabeledField(instructorTab, gc,13, "Subject 4:");

        jButton6 = new JButton("SAVE INSTRUCTOR");
        styleBtn(jButton6, new Color(60,60,120));
        jButton6.addActionListener(this::jButton6ActionPerformed);
        gc.gridx=0; gc.gridy=14; gc.gridwidth=2; gc.insets=new Insets(12,8,8,8);
        instructorTab.add(jButton6, gc);

        JScrollPane sp2 = new JScrollPane(instructorTab);
        sp2.setBorder(BorderFactory.createEmptyBorder());
        jTabbedPane1.addTab("ADD INSTRUCTOR", sp2);

        // ─── main layout ─────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        setBackground(new Color(200,200,200));

        JLabel header = new JLabel("  USER MANAGEMENT");
        header.setFont(new Font("Tahoma", Font.BOLD, 20));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(new Color(80,80,80));
        header.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        add(header, BorderLayout.NORTH);
        add(jTabbedPane1, BorderLayout.CENTER);
    }

    private JTextField addLabeledField(JPanel panel, GridBagConstraints gc, int row, String label) {
        gc.gridx=0; gc.gridy=row; panel.add(new JLabel(label), gc);
        JTextField tf = new JTextField(20);
        gc.gridx=1; gc.gridy=row; panel.add(tf, gc);
        return tf;
    }

    private void styleBtn(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Tahoma",Font.BOLD,14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(6,16,6,16)));
    }

    // Variables
    private JButton jButton6, jButton7;
    private JComboBox<String> jComboBox1, jComboBox2, jComboBox3, jComboBox4, jComboBox5;
    private JTabbedPane jTabbedPane1;
    private JTextField jTextField1,jTextField2,jTextField3,jTextField4,jTextField5;
    private JTextField jTextField6,jTextField7,jTextField8,jTextField9,jTextField10;
    private JTextField jTextField11,jTextField12,jTextField14,jTextField15,jTextField16;
    private JTextField jTextField17,jTextField18,jTextField20,jTextField21,jTextField22;
    private JTextField jTextField23,jTextField24,jTextField25;
}
