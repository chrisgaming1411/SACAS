package Contents;

import adminsacas.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * delete – Dialog for deleting a Department from the DB.
 */
public class delete extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(delete.class.getName());

    private JList<String>          deptList;
    private DefaultListModel<String> listModel;
    private java.util.Map<String,String> nameToCode = new java.util.LinkedHashMap<>();

    public delete() {
        initComponents();
        setTitle("Delete Department");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        loadDepts();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBackground(new Color(102,102,102));
        panel.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        JLabel title = new JLabel("SELECT DEPARTMENT TO DELETE");
        title.setFont(new Font("Segoe UI",Font.BOLD,16)); title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        deptList  = new JList<>(listModel);
        deptList.setFont(new Font("Segoe UI",Font.PLAIN,14));
        deptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(deptList);
        scroll.setPreferredSize(new Dimension(400, 200));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(new Color(102,102,102));
        JButton delBtn = new JButton("DELETE SELECTED");
        delBtn.setBackground(new Color(160,40,40)); delBtn.setForeground(Color.WHITE);
        delBtn.setFont(new Font("Segoe UI",Font.BOLD,13)); delBtn.setFocusPainted(false);
        delBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(7,14,7,14)));
        delBtn.addActionListener(e -> deleteSelected());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        btnRow.add(cancelBtn); btnRow.add(delBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        setContentPane(panel);
        setSize(460, 300);
    }

    private void loadDepts() {
        listModel.clear(); nameToCode.clear();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement("SELECT name, code FROM departments ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                String code = rs.getString("code");
                listModel.addElement(name + "  [" + code + "]");
                nameToCode.put(name + "  [" + code + "]", code);
            }
        } catch (SQLException e) {
            listModel.addElement("Could not load departments.");
        }
    }

    private void deleteSelected() {
        String sel = deptList.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Select a department first."); return; }
        String code = nameToCode.get(sel);
        if (code == null) return;

        int c = JOptionPane.showConfirmDialog(this,
            "Permanently delete:\n" + sel + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM departments WHERE code=?")) {
            ps.setString(1, code); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted: " + sel, "Done", JOptionPane.INFORMATION_MESSAGE);
            loadDepts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new delete().setVisible(true));
    }
}
