package pack.crs.ui;

import pack.crs.models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EligibilityCheckerWindow
 *
 * Supports two ways to be constructed:
 *  - AcademicOfficer mode
 *  - SystemAdmin mode
 *
 * SystemAdmin uses internal eligibility computation identical to AcademicOfficer.
 */
public class EligibilityCheckerWindow extends JFrame {

    private JTextField txtSearch;
    private JTable tblStudents;
    private DefaultTableModel tableModel;
    private JButton btnCheckEligibility, btnExportFailed, btnViewNonEligible, btnRegister, btnBack;
    private JTextArea txtEligibilityResult;

    private AcademicOfficer officer;     // non-null in AO mode
    private SystemAdmin admin;           // non-null in Admin mode

    private List<Student> allStudents;
    private Map<String, EligibilityResult> checkedResults;

    private AcadOfficerDashboard officerDashboard;
    private SysAdminWelcome adminWelcome;

    private File failedFile;

    // -----------------------------
    // Academic Officer constructor
    // -----------------------------
    public EligibilityCheckerWindow(List<Student> students, AcademicOfficer officer, AcadOfficerDashboard dashboard) {
        super("Check Student Eligibility");
        this.allStudents = students;
        this.officer = officer;
        this.officerDashboard = dashboard;
        this.checkedResults = new HashMap<>();

        initUI(true);
    }

    // -----------------------------
    // System Admin constructor
    // -----------------------------
    public EligibilityCheckerWindow(List<Student> students, SystemAdmin admin, SysAdminWelcome welcome) {
        super("Check Student Eligibility");
        this.allStudents = students;
        this.admin = admin;
        this.adminWelcome = welcome;
        this.checkedResults = new HashMap<>();

        initUI(false);
    }

    // -----------------------------
    // Shared UI Initialization
    // -----------------------------
    private void initUI(boolean isOfficerMode) {

        setLayout(new BorderLayout(10, 10));

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("Search Student (Name or ID): "), BorderLayout.WEST);
        txtSearch = new JTextField();
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        // Table
        String[] columns = {"Student ID", "Name", "Program"};
        tableModel = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        tblStudents = new JTable(tableModel);
        refreshTable(allStudents);

        JScrollPane tableScroll = new JScrollPane(tblStudents);

        // Buttons
        btnCheckEligibility = new JButton("Check Eligibility");
        btnExportFailed = new JButton("Export Non-Eligible Students");
        btnViewNonEligible = new JButton("View Non-Eligible Students");
        btnRegister = new JButton("Register Student");
        btnRegister.setEnabled(false);

        btnBack = new JButton("Back");
        btnBack.addActionListener(e -> backToCaller(isOfficerMode));

        // Result Area
        txtEligibilityResult = new JTextArea(10, 45);
        txtEligibilityResult.setEditable(false);
        txtEligibilityResult.setLineWrap(true);
        txtEligibilityResult.setWrapStyleWord(true);

        // Search live filter
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = txtSearch.getText().toLowerCase();
                refreshTable(allStudents.stream()
                        .filter(s -> s.getName().toLowerCase().contains(q)
                        || s.getStudentID().toLowerCase().contains(q))
                        .collect(Collectors.toList()));
            }
        });

        // BUTTON: CHECK ELIGIBILITY
        btnCheckEligibility.addActionListener(e -> handleEligibilityCheck());

        // BUTTON: EXPORT FAILED
        btnExportFailed.addActionListener(e -> exportFailedStudents());

        // BUTTON: VIEW NON-ELIGIBLE
        btnViewNonEligible.addActionListener(e -> {
            if (officer != null) {
                new ViewNonEligibleStudentsWindow(officer);
            } else {
                new ViewNonEligibleStudentsWindow(admin);
            }
        });

        // BUTTON: REGISTER
        btnRegister.addActionListener(e -> handleRegistration());

        // BUTTON PANEL
        JPanel buttons = new JPanel();
        buttons.add(btnCheckEligibility);
        buttons.add(btnExportFailed);
        buttons.add(btnViewNonEligible);
        buttons.add(btnRegister);
        buttons.add(btnBack);

        add(searchPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        add(new JScrollPane(txtEligibilityResult), BorderLayout.EAST);

        // failed_students.txt
        failedFile = new File(System.getProperty("user.dir")
                + File.separator + "Resources" + File.separator + "failed_students.txt");
        failedFile.getParentFile().mkdirs();
        try { if (!failedFile.exists()) failedFile.createNewFile(); } catch (IOException ignored) {}

        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void backToCaller(boolean isOfficerMode) {
        dispose();
        if (isOfficerMode && officerDashboard != null) officerDashboard.setVisible(true);
        if (!isOfficerMode && adminWelcome != null) adminWelcome.setVisible(true);
    }

    // -----------------------------
    // Refresh Table
    // -----------------------------
    private void refreshTable(List<Student> students) {
        tableModel.setRowCount(0);
        if (students == null) return;
        for (Student s : students) {
            tableModel.addRow(new Object[]{s.getStudentID(), s.getName(), s.getProgram()});
        }
    }

    // -----------------------------
    // CHECK ELIGIBILITY
    // -----------------------------
    private void handleEligibilityCheck() {

        int row = tblStudents.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student first.");
            return;
        }

        String id = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String program = (String) tableModel.getValueAt(row, 2);

        try {
            EligibilityResult result;

            if (officer != null) {
                result = officer.checkEligibility(id);
            } else {
                result = computeEligibilityForAdmin(id);
            }

            checkedResults.put(id, result);

            // UI formatting
            StringBuilder details = new StringBuilder();
            details.append("Student: ").append(name).append(" (").append(id).append(")\n")
                   .append("Program: ").append(program).append("\n-----------------------------\n")
                   .append(String.format("CGPA: %.2f\n", result.getCgpa()))
                   .append("Failed Courses: ").append(result.getFailedCount()).append("\n");

            if (!result.getFailedCourses().isEmpty())
                details.append("Courses: ").append(String.join(", ", result.getFailedCourses())).append("\n");

            if (result.isEligible()) {
                details.append("\nELIGIBLE for next level.");
                txtEligibilityResult.setForeground(new Color(0, 120, 0));
                btnRegister.setEnabled(true);
            } else {
                details.append("\nNOT ELIGIBLE.");
                txtEligibilityResult.setForeground(Color.RED);
                btnRegister.setEnabled(false);
            }

            txtEligibilityResult.setText(details.toString());

        } catch (Exception e) {
            txtEligibilityResult.setText("️Error: " + e.getMessage());
        }
    }

    // -----------------------------
    // Registration window
    // -----------------------------
    private void handleRegistration() {
        int row = tblStudents.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to register.");
            return;
        }

        String id = (String) tableModel.getValueAt(row, 0);
        EligibilityResult result = checkedResults.get(id);

        if (result == null || !result.isEligible()) {
            JOptionPane.showMessageDialog(this, "Student not eligible.");
            return;
        }

        new RegistrationWindow(id, result).setVisible(true);
    }

    // -----------------------------
    // EXPORT FAILED STUDENTS
    // -----------------------------
    private void exportFailedStudents() {
    if (checkedResults.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Please check at least one student's eligibility before exporting.",
                "No Data",
                JOptionPane.WARNING_MESSAGE);
        return;
    }

    StringBuilder report = new StringBuilder();
    int exportedCount = 0;

    try {
        // Load existing exported IDs
        Set<String> exportedIDs = new HashSet<>();
        try (Scanner sc = new Scanner(failedFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length > 0) exportedIDs.add(parts[0]);
                }
            }
        }

        List<String> linesToExport = new ArrayList<>();

        for (Map.Entry<String, EligibilityResult> entry : checkedResults.entrySet()) {

            String id = entry.getKey();
            EligibilityResult res = entry.getValue();

            Student s = allStudents.stream()
                    .filter(stu -> stu.getStudentID().equals(id))
                    .findFirst()
                    .orElse(null);

            if (s == null) {
                report.append(id).append(" – Student record not found.\n");
                continue;
            }

            if (res.isEligible()) {
                report.append(id).append(" – This student is eligible, cannot be exported.\n");
                continue;
            }

            if (exportedIDs.contains(id)) {
                report.append(id).append(" – This student's record already exists in failed_students.txt.\n");
                continue;
            }

            // Build export line
            String recoveryEmail = "aiboyzcourserecoverysystem@gmail.com";

            String line = s.getStudentID() + "|" +
              s.getName() + "|" +
              recoveryEmail + "|" +
              s.getProgram() + "|" +
              String.format("%.2f", res.getCgpa()) + "|" +
              res.getFailedCount() + "|" +
              String.join(",", res.getFailedCourses());



            linesToExport.add(line);
            report.append(id).append(" – Exported successfully.\n");
            exportedCount++;
        }

        // Write new lines to file
        if (!linesToExport.isEmpty()) {
            try (FileWriter fw = new FileWriter(failedFile, true)) {
                for (String line : linesToExport) fw.write(line + "\n");
            }
        }

        report.append("\n----------------------------------------\n");
        report.append("Total new exports: ").append(exportedCount).append("\n");

        // Add timestamp
        report.append("Timestamp: ").append(new java.util.Date().toString()).append("\n");

        // Show summary in dialog
        JTextArea reportArea = new JTextArea(report.toString(), 15, 60);
        reportArea.setEditable(false);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(reportArea),
                "Export Summary",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
                "Export failed: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
    }
}


    // -----------------------------
    // INTERNAL Admin Eligibility Calculation
    // -----------------------------
    private EligibilityResult computeEligibilityForAdmin(String studentID) throws Exception {

        File studentFile = new File(System.getProperty("user.dir")
                + File.separator + "Resources" + File.separator + "students.txt");
        File courseFile = new File(System.getProperty("user.dir")
                + File.separator + "Resources" + File.separator + "courses.txt");

        if (!studentFile.exists()) throw new Exception("students.txt missing");
        if (!courseFile.exists()) throw new Exception("courses.txt missing");

        // Load course credit hours
        Map<String, Integer> credits = new HashMap<>();
        try (Scanner sc = new Scanner(courseFile)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split("\\|");
                if (p.length >= 2) {
                    try { credits.put(p[0].trim(), Integer.parseInt(p[1].trim())); }
                    catch (Exception ignored) {}
                }
            }
        }

        // Find student line
        String studentLine = null;
        try (Scanner sc = new Scanner(studentFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith(studentID + "|")) {
                    studentLine = line;
                    break;
                }
            }
        }

        if (studentLine == null) throw new Exception("Student not found");

        String[] fields = studentLine.split("\\|");
        if (fields.length < 4) throw new Exception("No grades found");

        double totalPoints = 0;
        int totalCredits = 0;
        int failedCount = 0;
        List<String> failed = new ArrayList<>();
        Map<String, Double> gradeMap = new LinkedHashMap<>();

        for (int i = 3; i < fields.length; i++) {
            String[] pair = fields[i].split(":");
            if (pair.length == 2) {
                String courseID = pair[0];
                double grade = Double.parseDouble(pair[1]);
                int credit = credits.getOrDefault(courseID, 3);

                gradeMap.put(courseID, grade);

                totalPoints += grade * credit;
                totalCredits += credit;

                if (grade < 2.0) {
                    failedCount++;
                    failed.add(courseID);
                }
            }
        }

        double cgpa = totalPoints / totalCredits;
        boolean eligible = (cgpa >= 2.0 && failedCount <= 3);

        return new EligibilityResult(cgpa, eligible, failedCount, failed, gradeMap);
    }
}
