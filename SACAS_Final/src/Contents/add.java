package Contents;

import adminsacas.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * add – Dialog for adding a new Department/Course.
 * Called from Departmentconfiguration when admin clicks ADD DEPARTMENT.
 */
public class add extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(add.class.getName());

    private JTextField  tfDeptName, tfCourseCode, tfHead, tfFloors;
    private JTextArea   taDescript;
    private JComboBox<String> cboDuration;

    public add() {
        initComponents();
        setTitle("Add Department / Course");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(102,102,102));
        panel.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,8,6,8); gc.fill = GridBagConstraints.HORIZONTAL; gc.anchor = GridBagConstraints.WEST;

        Color fg = Color.WHITE;
        java.util.function.Supplier<JLabel> lbl = () -> { JLabel l=new JLabel(); l.setForeground(fg); l.setFont(new Font("Segoe UI",Font.BOLD,14)); return l; };

        JLabel h = lbl.get(); h.setText("DEPARTMENT NAME / COURSE:");
        gc.gridx=0; gc.gridy=0; panel.add(h, gc);
        tfDeptName = new JTextField(24);
        gc.gridx=1; gc.gridy=0; panel.add(tfDeptName, gc);

        JLabel h2 = lbl.get(); h2.setText("COURSE CODE:");
        gc.gridx=0; gc.gridy=1; panel.add(h2, gc);
        tfCourseCode = new JTextField(24);
        gc.gridx=1; gc.gridy=1; panel.add(tfCourseCode, gc);

        JLabel h3 = lbl.get(); h3.setText("DEPARTMENT HEAD:");
        gc.gridx=0; gc.gridy=2; panel.add(h3, gc);
        tfHead = new JTextField(24);
        gc.gridx=1; gc.gridy=2; panel.add(tfHead, gc);

        JLabel h4 = lbl.get(); h4.setText("DESCRIPTION:");
        gc.gridx=0; gc.gridy=3; gc.anchor=GridBagConstraints.NORTHWEST; panel.add(h4, gc); gc.anchor=GridBagConstraints.WEST;
        taDescript = new JTextArea(4,24); taDescript.setLineWrap(true); taDescript.setWrapStyleWord(true);
        gc.gridx=1; gc.gridy=3; panel.add(new JScrollPane(taDescript), gc);

        JLabel h5 = lbl.get(); h5.setText("DURATION (years):");
        gc.gridx=0; gc.gridy=4; panel.add(h5, gc);
        cboDuration = new JComboBox<>(new String[]{"1","2","3","4","5"});
        gc.gridx=1; gc.gridy=4; panel.add(cboDuration, gc);

        JLabel h6 = lbl.get(); h6.setText("FLOORS:");
        gc.gridx=0; gc.gridy=5; panel.add(h6, gc);
        tfFloors = new JTextField("1", 24);
        gc.gridx=1; gc.gridy=5; panel.add(tfFloors, gc);

        JButton saveBtn = new JButton("SAVE DEPARTMENT");
        saveBtn.setBackground(new Color(50,90,50)); saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI",Font.BOLD,14)); saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(8,18,8,18)));
        gc.gridx=0; gc.gridy=6; gc.gridwidth=2; gc.insets=new Insets(14,8,8,8);
        panel.add(saveBtn, gc);
        saveBtn.addActionListener(e -> saveDepartment());

        setContentPane(panel);
        pack();
    }

    private void saveDepartment() {
        String deptName  = tfDeptName.getText().trim();
        String code      = tfCourseCode.getText().trim().toUpperCase();
        String head      = tfHead.getText().trim();
        String desc      = taDescript.getText().trim();
        String duration  = cboDuration.getSelectedItem().toString();

        if (deptName.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Department name and course code are required.", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        if (head.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Department Head.", "Validation", JOptionPane.WARNING_MESSAGE); return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Save department: " + deptName + " [" + code + "]?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.get()) {
            String sql = "INSERT INTO departments (name, code, head, description) VALUES (?,?,?,?) "
                       + "ON DUPLICATE KEY UPDATE head=VALUES(head), description=VALUES(description)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, deptName); ps.setString(2, code);
                ps.setString(3, head);     ps.setString(4, desc);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Department saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new add().setVisible(true));
    }
}
