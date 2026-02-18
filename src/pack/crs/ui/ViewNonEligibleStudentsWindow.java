package pack.crs.ui;

import pack.crs.models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

// ONLY import List from java.util
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class ViewNonEligibleStudentsWindow extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    private AcademicOfficer officer; // used if opened by AO
    private SystemAdmin admin;       // used if opened by Admin

    // ----------------------------------------------------------
    // 1️⃣ Constructor for AcademicOfficer
    // ----------------------------------------------------------
    public ViewNonEligibleStudentsWindow(AcademicOfficer officer) {
        super("View Non-Eligible Students");
        this.officer = officer;
        this.admin = null;
        initUI();
        loadNonEligibleStudents();
    }

    // ----------------------------------------------------------
    // 2️⃣ Constructor for SystemAdmin (FULL AcademicOfficer Powers)
    // ----------------------------------------------------------
    public ViewNonEligibleStudentsWindow(SystemAdmin admin) {
        super("View Non-Eligible Students");
        this.admin = admin;
        this.officer = null;
        initUI();
        loadNonEligibleStudents();
    }

    // ----------------------------------------------------------
    // Shared UI initialization
    // ----------------------------------------------------------
    private void initUI() {

        setLayout(new BorderLayout(10, 10));

        String[] columns = {"Student ID", "Name", "Program", "CGPA", "Failed", "Courses"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        // Highlight non-eligible students
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    double cgpa = Double.parseDouble(table.getValueAt(row, 3).toString());
                    if (!isSelected) {
                        if (cgpa < 2.0) c.setBackground(new Color(255, 200, 200));
                        else c.setBackground(Color.white);
                    }
                } catch (Exception ignored) {}
                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        JButton btnClose = new JButton("Close");
        btnRefresh.addActionListener(e -> loadNonEligibleStudents());
        btnClose.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(750, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    // ----------------------------------------------------------
    // Load non-eligible students (AcademicOfficer + Admin supported)
    // ----------------------------------------------------------
    private void loadNonEligibleStudents() {

        tableModel.setRowCount(0);

        File file = new File(System.getProperty("user.dir")
                + File.separator + "Resources" + File.separator + "students.txt");

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "students.txt file not found.",
                    "File Not Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Scanner sc = new Scanner(file)) {

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                String id = parts[0];
                String name = parts[1];
                String email = parts[2];
                String program = parts[3];


                try {
                    EligibilityResult result;

                    if (officer != null) {
                        // AcademicOfficer mode → use AO method
                        result = officer.checkEligibility(id);

                    } else {
                        // SystemAdmin mode → compute eligibility internally
                        result = computeEligibilityForAdmin(id);
                    }

                    if (!result.isEligible()) {
                        String courses = String.join(",", result.getFailedCourses());
                        tableModel.addRow(new Object[]{
                                id,
                                name,
                                program,
                                String.format("%.2f", result.getCgpa()),
                                result.getFailedCount(),
                                courses.isEmpty() ? "-" : courses
                        });
                    }

                } catch (Exception ignored) {}
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "All students are currently eligible!",
                        "No Non-Eligible Students",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "️Error reading students.txt: " + e.getMessage(),
                    "Read Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------------------------------------------------
    // Internal eligibility computation for SystemAdmin
    // EXACT REPLICA of AcademicOfficer.checkEligibility
    // ----------------------------------------------------------
    private EligibilityResult computeEligibilityForAdmin(String studentID) throws Exception {

        File studentFile = new File(System.getProperty("user.dir")
                + File.separator + "Resources" + File.separator + "students.txt");
        File courseFile = new File(System.getProperty("user.dir")
                + File.separator + "Resources" + File.separator + "courses.txt");

        if (!studentFile.exists())
            throw new Exception("students.txt not found.");
        if (!courseFile.exists())
            throw new Exception("courses.txt not found.");

        // Load credit hours
        Map<String, Integer> courseCredits = new HashMap<>();
        try (Scanner sc = new Scanner(courseFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length > 1) {
                    try {
                        courseCredits.put(parts[0], Integer.parseInt(parts[1]));
                    } catch (Exception ignored) {}
                }
            }
        }

        // Locate student
        String studentLine = null;
        try (Scanner sc = new Scanner(studentFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith(studentID + "|")) {
                    studentLine = line;
                    break;
                }
            }
        }

        if (studentLine == null)
            throw new Exception("Student record not found: " + studentID);

        String[] fields = studentLine.split("\\|");
        if (fields.length < 4)
            throw new Exception("No course data for: " + studentID);

        double totalPoints = 0;
        int totalCredits = 0;
        int failedCount = 0;

        List<String> failedCourses = new ArrayList<>();
        Map<String, Double> courseGrades = new LinkedHashMap<>();

        for (int i = 4; i < fields.length; i++) {
            String[] pair = fields[i].split(":");
            if (pair.length != 2) continue;

            String courseID = pair[0];
            double grade = Double.parseDouble(pair[1]);
            int credit = courseCredits.getOrDefault(courseID, 3);

            courseGrades.put(courseID, grade);

            totalPoints += grade * credit;
            totalCredits += credit;

            if (grade < 2.0) {
                failedCourses.add(courseID);
                failedCount++;
            }
        }

        double cgpa = totalPoints / totalCredits;
        boolean eligible = (cgpa >= 2.0 && failedCount <= 3);

        return new EligibilityResult(cgpa, eligible, failedCount, failedCourses, courseGrades);
    }
}
