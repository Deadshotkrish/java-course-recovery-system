/**
 * RecoveryPlanWindow - Option A1 behaviour
 * - Two-panel layout (left: editable JTable bound to RecoveryPlan; right: Export button)
 * - Table edits update the RecoveryMilestone objects immediately
 * - Students loaded from Resources/students.txt (relative) OR fallback to uploaded path
 */

package pack.crs.ui;

import pack.crs.utils.NotificationService;
import pack.crs.models.AcademicReport;
import pack.crs.models.Course;
import pack.crs.models.RecoveryMilestone;
import pack.crs.models.RecoveryPlan;
import pack.crs.models.RecoveryPlanPDFExporter;
import pack.crs.models.Student;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.io.File;


public class RecoveryPlanWindow extends JFrame {

    private static final String RELATIVE_STUDENTS_PATH = "Resources" + File.separator + "failed_students.txt";
    private static final String UPLOADED_FALLBACK_PATH = "/mnt/data/failed_students.txt";
    private static final String OUT_REPORTS_DIR = "Resources" + File.separator + "reports";

    private JComboBox<String> cmbStudents;
    private JTable tblMilestones;
    private MilestoneTableModel tableModel;
    private JLabel lblGPA;
    private JLabel lblFailed;
    private JButton btnExport;
    private List<Student> students = new ArrayList<>();
    private RecoveryPlan currentPlan;
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private JButton btnBack;
    private AcadOfficerDashboard dashboard;

    public RecoveryPlanWindow(AcadOfficerDashboard dashboard) {
        super("Recovery Plan Manager - Academic Officer");
        this.dashboard = dashboard; 
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        if (dashboard != null) {
            dashboard.setVisible(true); // show previous window
        }
        dispose(); // close current window
    }

    });
    initComponents();

        
    String projectPath = System.getProperty("user.dir") + File.separator + RELATIVE_STUDENTS_PATH;
    Path p = Path.of(projectPath);
    String usePath = Files.exists(p) ? projectPath : UPLOADED_FALLBACK_PATH;
    loadStudentsIntoCombo(usePath);
    setSize(900, 600);
    setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        top.add(new JLabel("Select Student:"));
        cmbStudents = new JComboBox<>();
        cmbStudents.setEditable(true);
        cmbStudents.setPreferredSize(new Dimension(440, 28));
        top.add(cmbStudents);
        
        JTextField editor = (JTextField) cmbStudents.getEditor().getEditorComponent();

        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = editor.getText().trim().toLowerCase();

       
                int caret = editor.getCaretPosition();

        // Filtered list
                List<String> filtered = new ArrayList<>();
                for (Student s : students) {
                    String idName = s.getStudentID() + " - " + s.getName();
                    if (idName.toLowerCase().contains(text)) {
                        filtered.add(idName);
                    }
                }
            
        // Only update if filtered list changed
        if (filtered.size() != cmbStudents.getItemCount() || !cmbStudents.getItemAt(0).equals(filtered.get(0))) {
            cmbStudents.setModel(new DefaultComboBoxModel<>(filtered.toArray(new String[0])));
            editor.setText(text); // keep typed text
            editor.setCaretPosition(caret);
            cmbStudents.setPopupVisible(!filtered.isEmpty()); // show popup only if something matches
        }

        // Update table if exact match exists
        for (Student s : students) {
            String idName = s.getStudentID() + " - " + s.getName();
            if (idName.toLowerCase().equals(text)) {
                currentPlan = new RecoveryPlan(s);
                tableModel.setMilestones(currentPlan.getMilestones());
                AcademicReport r = s.getAcademicReport();
                if (r != null) {
                    lblGPA.setText(String.format("GPA: %.2f", r.getGPA()));
                    lblFailed.setText("Failed Courses: " + r.countFailedCourses());
                } else {
                    lblGPA.setText("GPA: -");
                    lblFailed.setText("Failed Courses: -");
                }
                break;
            }
        }
    }
});

        tableModel = new MilestoneTableModel(Collections.emptyList());
        tblMilestones = new JTable(tableModel);
        tblMilestones.setFillsViewportHeight(true);
        tblMilestones.setRowHeight(24);
        tblMilestones.setRowSorter(new TableRowSorter<>(tableModel));
        String[] statusOptions = {"Pending", "Completed"};
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        tblMilestones.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusCombo));
        JScrollPane scroll = new JScrollPane(tblMilestones);
        String[] gradeOptions = {"A+", "A", "B+", "B", "C+", "C", "C-", "D", "F+", "F", "F-"};
        JComboBox<String> gradeCombo = new JComboBox<>(gradeOptions);
        tblMilestones.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(gradeCombo));

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(24, 12, 24, 12));

        btnExport = new JButton("Export to PDF");
        btnExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExport.setMaximumSize(new Dimension(160, 40));
        right.add(btnExport);


        right.add(Box.createVerticalStrut(12));

        btnBack = new JButton("Back");
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.setMaximumSize(new Dimension(160, 40));
        right.add(btnBack);
        
        btnBack.addActionListener(e -> {
            if (dashboard != null){
                dashboard.setVisible(true);
            }
             
            this.dispose(); // close current window
        });

        right.add(Box.createVerticalGlue());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 16,8));
        lblGPA = new JLabel("GPA: -");
        lblFailed = new JLabel("Failed Courses: -");
        bottom.add(lblGPA);
        bottom.add(Box.createHorizontalStrut(40));
        bottom.add(lblFailed);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(8,8));
        cp.add(top, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(8,8));
        center.add(scroll, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);
        cp.add(center, BorderLayout.CENTER);
        cp.add(bottom, BorderLayout.SOUTH);

        // listeners
        cmbStudents.addActionListener(e -> onStudentSelected());
        btnExport.addActionListener(e -> onExport());

    }

    private void loadStudentsIntoCombo(String path) {
        try {
            students = loadStudentsFromFile(path);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load students:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            students = new ArrayList<>();
        }
        cmbStudents.removeAllItems();
        List<Student> nonEligibleStudents = new ArrayList<>();
        for ( Student s : students) {
            AcademicReport r = s.getAcademicReport();
            if (r != null) {
                boolean nonEligible = (r.getGPA() < 2.0 || r.countFailedCourses() > 0);
                if (nonEligible){
                    nonEligibleStudents.add(s);
                    cmbStudents.addItem(s.getStudentID() + " - " + s.getName());
                }
            }
        }
        students = nonEligibleStudents;
        
        if (!students.isEmpty()){
            cmbStudents.setSelectedIndex(0);
            onStudentSelected();
        }
    }

    private void onStudentSelected() {
        int idx = cmbStudents.getSelectedIndex();
        if (idx < 0 || idx >= students.size()) {
            currentPlan = null;
            tableModel.setMilestones(Collections.emptyList());
            lblGPA.setText("GPA: -");
            lblFailed.setText("Failed Courses: -");
            return;
        }
        Student selected = students.get(idx);
        currentPlan = new RecoveryPlan(selected); // auto-populates failed courses
        tableModel.setMilestones(currentPlan.getMilestones());
        AcademicReport r = selected.getAcademicReport();
        if (r != null) {
            lblGPA.setText(String.format("GPA: %.2f", r.getGPA()));
            lblFailed.setText("Failed Courses: " + r.countFailedCourses());
        } else {
            lblGPA.setText("GPA: -");
            lblFailed.setText("Failed Courses: -");
        }
    }

private void onExport() {
    if (currentPlan == null) {
        JOptionPane.showMessageDialog(this, "No student loaded.");
        return;
    }
    
    for (RecoveryMilestone m : currentPlan.getMilestones()){
        if (m.getTaskDescription() == null || m.getTaskDescription().isBlank()){
            JOptionPane.showMessageDialog(this, "All rows must have a Task Description", "Incomplete Data", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (m.getDueDate() == null){
            JOptionPane.showMessageDialog(this, "All rows must have a Due Date.", "Incomplete Data", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (m.getStatus() == null || m.getStatus().isBlank()){
            JOptionPane.showMessageDialog(this, "All rows must have a Status", "Incomplete Data", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (m.getGrade() == null || m.getGrade().isBlank()){
            JOptionPane.showMessageDialog(this, "All rows must have a Grade", "Incomplete Data", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    
    try {
        // Build cross-platform project-relative path:
        String folderPath = System.getProperty("user.dir")
                + File.separator + "Resources"
                + File.separator + "Recovery_Plan_Reports";

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); // creates folder if missing
        }

        // Build output file name
        String fileName = "RecoveryPlan_"
                + currentPlan.getStudent().getStudentID()
                + ".pdf";

        File outputFile = new File(folder, fileName);
        RecoveryPlanPDFExporter.exportToPDF(currentPlan, outputFile.getAbsolutePath());

        JOptionPane.showMessageDialog(this,
                "PDF successfully saved in:\n" + outputFile.getAbsolutePath(),
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

        //
        // Email part
        //
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to email this recovery plan to the student?",
                "Send Email",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) 
        {
            NotificationService.sendRecoveryPlanEmail(currentPlan.getStudent(), currentPlan, outputFile);
            JOptionPane.showMessageDialog(this,
                    "Recovery plan emailed to the student.",
                    "Email Sent",
                    JOptionPane.INFORMATION_MESSAGE);
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Failed to generate PDF:\n" + ex.getMessage(),
                "PDF Error",
                JOptionPane.ERROR_MESSAGE
        );
        ex.printStackTrace();
    }
}

    private List<Student> loadStudentsFromFile(String filePath) throws IOException {
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.println("Looking for: " + filePath);

        List<Student> list = new ArrayList<>();
        File f = new File(filePath);
        if (!f.exists()) throw new FileNotFoundException("Students file not found: " + filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 7) continue;

                String studentID  = parts[0].trim();
                String name       = parts[1].trim();
                String email      = parts[2].trim();
                String program    = parts[3].trim();
                double gpa        = Double.parseDouble(parts[4].trim());
                int failedCount   = Integer.parseInt(parts[5].trim());

                String failedCoursesRaw = parts[6].trim();  // CSV of failed courses
                String[] failedArr = failedCoursesRaw.split(",");

                Map<Course, Double> grades = new LinkedHashMap<>();
                for (String courseCode : failedArr) {
                    courseCode = courseCode.trim();
                    if (!courseCode.isEmpty()) {
                        Course c = new Course(courseCode, courseCode, 3);
                        grades.put(c, 0.0); // failed means grade = 0.0
                    }
                }

                AcademicReport report = new AcademicReport("RPT-" + studentID, grades);
                report.setGPA(gpa);

                Student s = new Student(
                        studentID, email, "password", true,
                        studentID, name, email, program, report
                );

                list.add(s);
            }
        }

    return list;
}


    // TableModel for live-binding to RecoveryPlan.getMilestones()
    private static class MilestoneTableModel extends AbstractTableModel {
        private final String[] columns = {
            "Course", "Task Description", "Due Date (yyyy-MM-dd)", "Status", "Grade"
        };

    private List<RecoveryMilestone> milestones = new ArrayList<>();

    public MilestoneTableModel(List<RecoveryMilestone> list) {
        this.milestones = (list == null) ? new ArrayList<>() : list;
    }

    public void setMilestones(List<RecoveryMilestone> list) {
        this.milestones = (list == null) ? new ArrayList<>() : list;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() { return milestones.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int r, int c) {
        RecoveryMilestone m = milestones.get(r);
        switch (c) {
            case 0: return m.getCourseCode();
            case 1: return m.getTaskDescription() == null ? "" : m.getTaskDescription();
            case 2: return m.getDueDate() == null ? "" : m.getDueDate().toString();
            case 3: return m.getStatus() == null ? "" : m.getStatus();
            case 4: return m.getGrade() == null ? "" : m.getGrade();
        }
        return "";
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col != 0; // Course Code cannot be edited
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        RecoveryMilestone m = milestones.get(row);
        String v = value == null ? "" : value.toString().trim();

        switch (col) {
            case 1:
                if (v.length() > 250){
                    JOptionPane.showMessageDialog(null, "cannot exceed more than 250 charaters", "Description Too Long",
                            JOptionPane.ERROR_MESSAGE);
                }
                else{
                    m.setTaskDescription(v);
                }
                break;

            case 2:
                if (v.isEmpty()){
                    m.setDueDate(null);
                }
                else{
                    try{
                        m.setDueDate(LocalDate.parse(v));
                    }
                    catch(Exception ex){
                        JOptionPane.showMessageDialog(
                        null, "Must follow the format yyyy-MM-dd",
                        "Invalid Date Format", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;

            case 3:
                m.setStatus(v);
                break;

            case 4:
                if (v.isEmpty()){
                    m.setGrade(null);
                }
                else{
                    String grade = v.toUpperCase().trim();
                    if (grade.matches("A\\+|A-|A|B\\+|B-|B|C\\+|C-|C|D|F\\+|F-|F")){    //use \\ on '+' as it is a special regex
                        m.setGrade(grade);
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "grade must be one of the following options", "Invalid Grade",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
        }

        fireTableCellUpdated(row, col);
    }
}

}
