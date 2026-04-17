package Contents;

import adminsacas.DBConnection;
import adminsacas.Session;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * bookapp – Student panel: pick an instructor, see their available slots, submit.
 * Instructors and their availability come from the database.
 * This is the quick-form version; viewfaculty has the card-based version.
 */
public class bookapp extends javax.swing.JPanel {

    private static class InstructorItem {
        int    id;
        String name, dept;
        @Override public String toString() { return name + (dept!=null&&!dept.isEmpty()?" ("+dept+")":""); }
    }
    private static class SlotItem {
        String day, time;
        @Override public String toString() { return day + "  –  " + time; }
    }

    private final List<InstructorItem> instructors = new ArrayList<>();
    private final List<SlotItem>       slots       = new ArrayList<>();

    private JComboBox<InstructorItem> cboInstructor;
    private JComboBox<SlotItem>       cboSlot;
    private JTextArea                 txtReason;
    private JButton                   btnSubmit;
    private JLabel                    statusLbl;

    public bookapp() {
        initComponents();
        initUI();
        refreshData();
    }

    public void refreshData() {
        loadInstructors();
        onInstructorChanged();
    }

    // ── build UI ─────────────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout()); setBackground(Color.WHITE);

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE); header.setBorder(BorderFactory.createEmptyBorder(18,24,8,24));
        JLabel title = new JLabel("BOOK APPOINTMENT");
        title.setFont(new Font("Tahoma",Font.BOLD,22)); header.add(title, BorderLayout.WEST);
        JSeparator sep = new JSeparator(); sep.setForeground(Color.LIGHT_GRAY);
        JPanel top = new JPanel(new BorderLayout()); top.setBackground(Color.WHITE);
        top.add(header, BorderLayout.CENTER); top.add(sep, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE); form.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets=new Insets(8,8,8,8); gc.fill=GridBagConstraints.HORIZONTAL; gc.anchor=GridBagConstraints.WEST;

        // Instructor
        gc.gridx=0; gc.gridy=0; gc.weightx=0;
        form.add(bold("Select Instructor:"), gc);
        cboInstructor = new JComboBox<>();
        cboInstructor.setFont(new Font("Segoe UI",Font.PLAIN,14));
        gc.gridx=1; gc.weightx=1; form.add(cboInstructor, gc);
        cboInstructor.addActionListener(e -> onInstructorChanged());

        // Available Slot
        gc.gridx=0; gc.gridy=1; gc.weightx=0;
        form.add(bold("Available Slot:"), gc);
        cboSlot = new JComboBox<>();
        cboSlot.setFont(new Font("Segoe UI",Font.PLAIN,14));
        gc.gridx=1; gc.weightx=1; form.add(cboSlot, gc);

        // Reason
        gc.gridx=0; gc.gridy=2; gc.weightx=0; gc.anchor=GridBagConstraints.NORTHWEST;
        form.add(bold("Reason for consultation:"), gc);
        txtReason = new JTextArea(4,30); txtReason.setLineWrap(true); txtReason.setWrapStyleWord(true);
        txtReason.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtReason.setFont(new Font("Segoe UI",Font.PLAIN,13));
        gc.gridx=1; gc.weightx=1; form.add(new JScrollPane(txtReason), gc); gc.anchor=GridBagConstraints.WEST;

        // Submit button
        btnSubmit = new JButton("SUBMIT APPOINTMENT REQUEST");
        btnSubmit.setBackground(new Color(0,51,153)); btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Tahoma",Font.BOLD,14)); btnSubmit.setFocusPainted(false);
        btnSubmit.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK,2), BorderFactory.createEmptyBorder(10,22,10,22)));
        btnSubmit.addActionListener(e -> submitAppointment());
        gc.gridx=0; gc.gridy=3; gc.gridwidth=2; gc.insets=new Insets(20,8,8,8); form.add(btnSubmit, gc);

        // Status label
        statusLbl = new JLabel(" "); statusLbl.setFont(new Font("Segoe UI",Font.ITALIC,13));
        gc.gridy=4; gc.insets=new Insets(4,8,4,8); form.add(statusLbl, gc);

        JScrollPane scrollForm = new JScrollPane(form); scrollForm.setBorder(BorderFactory.createEmptyBorder());
        add(scrollForm, BorderLayout.CENTER);
    }

    // ── load instructors ─────────────────────────────────────────────────────
    private void loadInstructors() {
        instructors.clear();
        cboInstructor.removeAllItems();
        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT i.id, CONCAT(i.firstname,' ',i.lastname) AS name, i.course "
                       + "FROM instructors i ORDER BY i.lastname";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InstructorItem ii = new InstructorItem();
                    ii.id   = rs.getInt("id");
                    ii.name = rs.getString("name");
                    ii.dept = rs.getString("course");
                    instructors.add(ii);
                    cboInstructor.addItem(ii);
                }
            }
        } catch (SQLException e) {
            statusLbl.setText("DB error loading instructors: " + e.getMessage());
            statusLbl.setForeground(Color.RED);
        }
        if (instructors.isEmpty()) {
            statusLbl.setText("No instructors found. Ask the admin to register instructors first.");
            statusLbl.setForeground(Color.GRAY);
        }
    }

    // ── when instructor changes, load their availability ─────────────────────
    private void onInstructorChanged() {
        cboSlot.removeAllItems(); slots.clear();
        InstructorItem sel = (InstructorItem) cboInstructor.getSelectedItem();
        if (sel == null) return;
        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT day_of_week, time_slot FROM availability WHERE instructor_id=? ORDER BY day_of_week, time_slot";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sel.id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    SlotItem si = new SlotItem();
                    si.day  = rs.getString("day_of_week");
                    si.time = rs.getString("time_slot");
                    slots.add(si); cboSlot.addItem(si);
                }
            }
        } catch (SQLException e) {
            statusLbl.setText("Could not load availability."); statusLbl.setForeground(Color.RED);
        }
        if (slots.isEmpty()) {
            statusLbl.setText("This instructor has no availability set yet. Try another or check back later.");
            statusLbl.setForeground(new Color(160,100,0));
        } else {
            statusLbl.setText(" "); statusLbl.setForeground(Color.BLACK);
        }
    }

    // ── submit ────────────────────────────────────────────────────────────────
    private void submitAppointment() {
        InstructorItem instructor = (InstructorItem) cboInstructor.getSelectedItem();
        SlotItem       slot       = (SlotItem)       cboSlot.getSelectedItem();
        String         reason     = txtReason.getText().trim();

        if (instructor == null) {
            warn("Please select an instructor."); return;
        }
        if (slot == null) {
            warn("Please select an available time slot."); return;
        }
        if (reason.isEmpty()) {
            warn("Please enter a reason for the consultation."); return;
        }

        int studentId = Session.getProfileId();
        if (studentId < 0) { warn("Session error. Please re-login."); return; }

        try (Connection conn = DBConnection.get()) {
            // check for duplicate pending appointment
            String checkSql = "SELECT COUNT(*) FROM appointments WHERE student_id=? AND instructor_id=? AND day_of_week=? AND time_slot=? AND status='PENDING'";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1,studentId); ps.setInt(2,instructor.id);
                ps.setString(3,slot.day); ps.setString(4,slot.time);
                ResultSet rs = ps.executeQuery(); rs.next();
                if (rs.getInt(1) > 0) {
                    warn("You already have a PENDING appointment with this instructor at that slot."); return;
                }
            }

            // insert appointment
            String sql = "INSERT INTO appointments (student_id,instructor_id,day_of_week,time_slot,reason,status) VALUES (?,?,?,?,?,'PENDING')";
            try (PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1,studentId); ps.setInt(2,instructor.id);
                ps.setString(3,slot.day); ps.setString(4,slot.time); ps.setString(5,reason);
                ps.executeUpdate();
            }

            // notify instructor
            String notifMsg = "📅 New appointment request from " + Session.getFullName()
                + " on " + slot.day + " at " + slot.time + ". Reason: " + reason;
            String notifSql = "INSERT INTO notifications (user_id, message) "
                + "SELECT user_id, ? FROM instructors WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(notifSql)) {
                ps.setString(1, notifMsg); ps.setInt(2, instructor.id); ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                "✅ Appointment request submitted!\n\n"
                + "Instructor : " + instructor.name + "\n"
                + "Slot       : " + slot.day + "  –  " + slot.time + "\n"
                + "Status     : PENDING\n\n"
                + "You will receive a notification when the instructor responds.",
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);

            // clear form
            txtReason.setText("");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel bold(String text) { JLabel l=new JLabel(text); l.setFont(new Font("Segoe UI",Font.BOLD,14)); return l; }
    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Required", JOptionPane.WARNING_MESSAGE); }
    private void initComponents() { /* built in initUI */ }
}
