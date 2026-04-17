package Contents;

import adminsacas.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Departmentconfiguration – Admin panel for managing departments.
 * Departments are stored in the DB but are not required for the core
 * appointment flow; this panel gives admin visibility and control.
 */
public class Departmentconfiguration extends javax.swing.JPanel {

    private JList<String> deptList;
    private DefaultListModel<String> listModel;

    public Departmentconfiguration() {
        initComponents();
        initUI();
        loadDepartments();
    }

    private void initUI() {
        setLayout(new BorderLayout()); setBackground(new Color(180,180,180));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // header
        JLabel header = new JLabel("  DEPARTMENT CONFIGURATION");
        header.setFont(new Font("Segoe UI",Font.BOLD,18)); header.setForeground(Color.WHITE);
        header.setOpaque(true); header.setBackground(new Color(80,80,80));
        header.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        add(header, BorderLayout.NORTH);

        // list
        listModel = new DefaultListModel<>();
        deptList  = new JList<>(listModel);
        deptList.setFont(new Font("Segoe UI",Font.PLAIN,14));
        deptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(deptList);
        scroll.setBorder(BorderFactory.createTitledBorder("Departments in Database"));
        add(scroll, BorderLayout.CENTER);

        // buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,12,12));
        btnPanel.setBackground(new Color(180,180,180));

        JButton addBtn  = makeBtn("ADD DEPARTMENT",    new Color(60,100,60));
        JButton delBtn  = makeBtn("DELETE DEPARTMENT", new Color(130,40,40));
        JButton refBtn  = makeBtn("REFRESH",           new Color(60,60,100));

        addBtn.addActionListener(e -> addDepartment());
        delBtn.addActionListener(e -> deleteDepartment());
        refBtn.addActionListener(e -> loadDepartments());

        btnPanel.add(addBtn); btnPanel.add(delBtn); btnPanel.add(refBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadDepartments() {
        listModel.clear();
        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT name, code FROM departments ORDER BY name";
            // departments table may not exist if user hasn't run sacas_db.sql yet
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) listModel.addElement(rs.getString("name") + "  [" + rs.getString("code") + "]");
            }
        } catch (SQLException e) {
            listModel.addElement("(No departments table yet – run sacas_db.sql)");
        }
    }

    private void addDepartment() {
        String name = JOptionPane.showInputDialog(this, "Enter Department Name:", "Add Department", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        String code = JOptionPane.showInputDialog(this, "Enter Department Code (e.g. BSIT):", "Add Department", JOptionPane.PLAIN_MESSAGE);
        if (code == null || code.trim().isEmpty()) return;

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO departments (name,code) VALUES (?,?)")) {
            ps.setString(1, name.trim()); ps.setString(2, code.trim().toUpperCase());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Department added.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadDepartments();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDepartment() {
        String sel = deptList.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Select a department first."); return; }
        int c = JOptionPane.showConfirmDialog(this, "Delete: " + sel + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        // extract code
        String code = sel.replaceAll(".*\\[(.*)\\].*","$1");
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM departments WHERE code=?")) {
            ps.setString(1, code); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.", "Done", JOptionPane.INFORMATION_MESSAGE);
            loadDepartments();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI",Font.BOLD,13)); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(7,14,7,14)));
        return b;
    }

    private void initComponents() { /* built in initUI */ }
}
