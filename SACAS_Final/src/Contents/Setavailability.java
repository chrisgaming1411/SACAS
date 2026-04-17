package Contents;

import adminsacas.DBConnection;
import adminsacas.Session;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * Setavailability – Instructor sets weekly time-slot availability.
 * Selections are saved to and loaded from the `availability` table.
 */
public class Setavailability extends javax.swing.JPanel {

    private static final String[] TIME_SLOTS = {
        "7:00 A.M","8:00 A.M","9:00 A.M","10:00 A.M","11:00 A.M",
        "1:00 P.M","2:00 P.M","3:00 P.M","4:00 P.M","5:00 P.M"
    };
    private static final String[] DAYS = {
        "Monday","Tuesday","Wednesday","Thursday","Friday"
    };

    // checkboxes[timeRow][dayCol]  col 5 = Recurring
    private JCheckBox[][] checkboxes;

    public Setavailability() {
        initComponents();
        initUI();
        loadFromDB();          // populate checkboxes from DB on open
    }

    // ── Build UI ─────────────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout(0,0));
        setBackground(Color.WHITE);

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(18,24,8,24));
        JLabel title = new JLabel("SET AVAILABILITY");
        title.setFont(new Font("Tahoma",Font.BOLD,22));
        header.add(title, BorderLayout.WEST);
        JSeparator sep = new JSeparator(); sep.setForeground(Color.LIGHT_GRAY);
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(header, BorderLayout.CENTER);
        top.add(sep, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // grid
        int rows = TIME_SLOTS.length, cols = DAYS.length + 1;
        checkboxes = new JCheckBox[rows][cols];
        String[] headers = {"TIME","Monday","Tuesday","Wednesday","Thursday","Friday","Recurring?"};

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(10,24,10,24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(2,4,2,4);

        for (int c = 0; c < headers.length; c++) {
            gbc.gridx=c; gbc.gridy=0; gbc.weightx=(c==0)?0.18:0.12; gbc.weighty=0;
            JLabel lbl = new JLabel(headers[c], SwingConstants.CENTER);
            lbl.setFont(new Font("Tahoma",Font.BOLD,13));
            lbl.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.BLACK));
            lbl.setOpaque(true); lbl.setBackground(new Color(230,230,230));
            lbl.setPreferredSize(new Dimension(0,32));
            grid.add(lbl, gbc);
        }

        for (int r = 0; r < rows; r++) {
            gbc.gridy=r+1; gbc.weighty=0.08;
            gbc.gridx=0; gbc.weightx=0.18;
            JLabel tl = new JLabel(TIME_SLOTS[r], SwingConstants.CENTER);
            tl.setFont(new Font("Tahoma",Font.BOLD,12));
            tl.setBorder(BorderFactory.createMatteBorder(0,1,1,1,Color.BLACK));
            tl.setOpaque(true); tl.setBackground(Color.WHITE);
            grid.add(tl, gbc);

            for (int c = 0; c < cols; c++) {
                gbc.gridx=c+1; gbc.weightx=0.12;
                JCheckBox cb = new JCheckBox();
                cb.setHorizontalAlignment(SwingConstants.CENTER);
                cb.setBackground(Color.WHITE); cb.setOpaque(true);
                cb.setBorder(BorderFactory.createMatteBorder(0,1,1,1,Color.BLACK));
                final int fr=r, fc=c;
                cb.addActionListener(e -> cb.setBackground(cb.isSelected() ? new Color(200,240,200) : Color.WHITE));
                checkboxes[r][c] = cb;
                grid.add(cb, gbc);
            }
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT,24,14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.LIGHT_GRAY));

        JButton clearBtn = makeBtn("CLEAR ALL", new Color(100,100,100));
        clearBtn.addActionListener(e -> clearAll());

        JButton saveBtn = makeBtn("UPDATE WEEKLY AVAILABILITY", new Color(40,90,40));
        saveBtn.addActionListener(e -> saveToDB());

        JLabel note = new JLabel("<html><b>Tip:</b> Check the time slots you are available, then click Update.</html>");
        note.setFont(new Font("Tahoma",Font.PLAIN,11)); note.setForeground(Color.DARK_GRAY);

        footer.add(clearBtn); footer.add(saveBtn); footer.add(note);
        add(footer, BorderLayout.SOUTH);
    }

    // ── SAVE to DB ───────────────────────────────────────────────────────────
    private void saveToDB() {
        int instructorId = Session.getProfileId();
        if (instructorId < 0) {
            JOptionPane.showMessageDialog(this, "No instructor logged in.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }

        // build selected slots
        List<String[]> selected = new ArrayList<>();   // [day, timeSlot, recurring]
        for (int r = 0; r < TIME_SLOTS.length; r++) {
            for (int c = 0; c < DAYS.length; c++) {
                if (checkboxes[r][c].isSelected()) {
                    String recurring = checkboxes[r][5].isSelected() ? "1" : "0";
                    selected.add(new String[]{DAYS[c], TIME_SLOTS[r], recurring});
                }
            }
        }

        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No slots selected. Please check at least one time slot.", "No Selection", JOptionPane.WARNING_MESSAGE); return;
        }

        try (Connection conn = DBConnection.get()) {
            // delete existing availability for this instructor
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM availability WHERE instructor_id=?")) {
                del.setInt(1, instructorId); del.executeUpdate();
            }
            // insert new selections
            String ins = "INSERT INTO availability (instructor_id,day_of_week,time_slot,is_recurring) VALUES (?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(ins)) {
                for (String[] row : selected) {
                    ps.setInt   (1, instructorId);
                    ps.setString(2, row[0]);
                    ps.setString(3, row[1]);
                    ps.setInt   (4, Integer.parseInt(row[2]));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            JOptionPane.showMessageDialog(this,
                "✅ Availability updated successfully!\n" + selected.size() + " slot(s) saved.",
                "Saved", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "DB error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── LOAD from DB ─────────────────────────────────────────────────────────
    public void loadFromDB() {
        int instructorId = Session.getProfileId();
        if (instructorId < 0) return;

        try (Connection conn = DBConnection.get()) {
            String sql = "SELECT day_of_week, time_slot, is_recurring FROM availability WHERE instructor_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, instructorId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String day  = rs.getString("day_of_week");
                    String time = rs.getString("time_slot");
                    boolean rec = rs.getBoolean("is_recurring");

                    int dayIdx  = indexOf(DAYS, day);
                    int timeIdx = indexOf(TIME_SLOTS, time);

                    if (dayIdx >= 0 && timeIdx >= 0 && checkboxes != null) {
                        checkboxes[timeIdx][dayIdx].setSelected(true);
                        checkboxes[timeIdx][dayIdx].setBackground(new Color(200,240,200));
                        if (rec) {
                            checkboxes[timeIdx][5].setSelected(true);
                            checkboxes[timeIdx][5].setBackground(new Color(200,240,200));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load availability: " + e.getMessage());
        }
    }

    private int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return -1;
    }

    private void clearAll() {
        int c = JOptionPane.showConfirmDialog(this, "Clear all selections?", "Clear", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        for (JCheckBox[] row : checkboxes)
            for (JCheckBox cb : row) { cb.setSelected(false); cb.setBackground(Color.WHITE); }
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma",Font.BOLD,12)); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(6,14,6,14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void initComponents() { /* layout built entirely in initUI */ }
}
