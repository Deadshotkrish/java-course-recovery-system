package pack.crs.ui;

import pack.crs.utils.AcademicReportGenerator;
import pack.crs.utils.NotificationService;
import java.io.File;
import pack.crs.models.*;
import pack.crs.ui.SysAdminWelcome;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import pack.crs.utils.IReportGenerator;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class AcademicReportWindow extends JFrame {

    private JTable tblStudents;
    private DefaultTableModel tableModel;
    private JTextArea txtReport;
    private JButton btnGenerate, btnRefresh, btnBack, btnExportPDF;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<Student> students;
    private AcademicOfficer officer;
    private IReportGenerator reportGenerator;
    private AcademicReportNew currentReport;
    private JFrame dashboard;
    private Student currentStudent;

    // First contructor for acadOfficer
    public AcademicReportWindow(List<Student> students, AcademicOfficer officer, AcadOfficerDashboard dashboard) {
        super("Academic Performance Reporting");
        this.students = students;
        this.officer = officer;
        this.dashboard = dashboard;
        this.reportGenerator = new AcademicReportGenerator();

        initUI();
        refreshTable();
        
    }
    
    // Second contructor for sysadmin
    public AcademicReportWindow(List<Student> students, SystemAdmin admin, SysAdminWelcome dashboard) {
        super("Academic Performance Reporting");
        this.students = students;
        this.officer = null;
        this.dashboard = dashboard;
        this.reportGenerator = new AcademicReportGenerator();

        initUI();
        refreshTable();
    }


    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Student"));
        
        JLabel lblSearch = new JLabel("Search:");
        txtSearch = new JTextField(20);
        
        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchInputPanel.add(lblSearch);
        searchInputPanel.add(txtSearch);
        
        searchPanel.add(searchInputPanel, BorderLayout.NORTH);
        
        JLabel lblHint = new JLabel("Search by Student ID, Name, or Program");
        lblHint.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 11));
        lblHint.setForeground(Color.GRAY);
        searchPanel.add(lblHint, BorderLayout.SOUTH);

        String[] columns = {"Student ID", "Name", "Program"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblStudents = new JTable(tableModel);
        
        rowSorter = new TableRowSorter<>(tableModel);
        tblStudents.setRowSorter(rowSorter);
        
        JScrollPane scrollTable = new JScrollPane(tblStudents);

        txtReport = new JTextArea(15, 50);
        txtReport.setEditable(false);
        txtReport.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13));
        JScrollPane scrollReport = new JScrollPane(txtReport);

        btnGenerate = new JButton("Generate Report");
        btnGenerate.addActionListener(e -> generateReport());

        btnExportPDF = new JButton("Export as PDF");
        btnExportPDF.addActionListener(e -> {
            if (currentReport != null) exportToPDF(currentReport);
            else JOptionPane.showMessageDialog(this, "Generate a report first!");
        });

        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTable());

        btnBack = new JButton("Back");
        btnBack.addActionListener(e -> {
            this.dispose();
            if (dashboard != null) dashboard.setVisible(true);
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnGenerate);
        btnPanel.add(btnExportPDF);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnBack);

        // Search function calllls
        setupSearchFilter();

        // Layouts
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(scrollTable, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollReport, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(800, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void setupSearchFilter() {
        // Add document listener to search fields for filtering
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }
        });
    }

    private void filterTable() {
        String text = txtSearch.getText().trim();
        if (text.length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            // Filter that searches in all columns
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{s.getStudentID(), s.getName(), s.getProgram()});
        }
        
        // Clear search when refreshing the table
        txtSearch.setText("");
        rowSorter.setRowFilter(null);
        
        txtReport.setText("Student list refreshed successfully. " + students.size() + " students loaded.");
    }

    private void generateReport() {
        int row = tblStudents.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student first!");
            return;
        }

        int modelRow = tblStudents.convertRowIndexToModel(row);
        
        currentStudent = students.get(modelRow);
        
        String id = (String) tableModel.getValueAt(modelRow, 0);
        
        try {
            currentReport = reportGenerator.generateReport(id);
            txtReport.setText(formatReport(currentReport));
            JOptionPane.showMessageDialog(this,
                    "Report successfully generated for " + currentReport.getStudentName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String formatReport(AcademicReportNew report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Student ID : ").append(report.getStudentID()).append("\n");
        sb.append("Name       : ").append(report.getStudentName()).append("\n");
        sb.append("Program    : ").append(report.getProgram()).append("\n");
        sb.append("----------------------------------------\n");

        for (AcademicReportNew.CourseGrade cg : report.getCourseGrades()) {
            sb.append(String.format("%-8s %-25s Credits: %-2d Grade: %.2f\n",
                    cg.getCourseCode(), cg.getCourseName(), cg.getCreditHours(), cg.getGradePoint()));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("CGPA: %.2f%n", report.getCgpa()));
        sb.append("Failed Courses: ");
        if (report.getFailedCourseCodes().isEmpty()) sb.append("None");
        else sb.append(String.join(", ", report.getFailedCourseCodes()));

        return sb.toString();
    }

    private void exportToPDF(AcademicReportNew report) {
    try {
        String reportsDirPath = System.getProperty("user.dir") + File.separator +
                "Resources" + File.separator + "reports";
        File reportsDir = new File(reportsDirPath);
        if (!reportsDir.exists()) reportsDir.mkdirs();

        String outputPath = reportsDirPath + File.separator +
                report.getStudentID() + "_AcademicReport.pdf";

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font textFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);

        doc.add(new Paragraph("Academic Performance Report", titleFont));
        doc.add(new Paragraph("Generated by Academic Officer\n\n", textFont));

        doc.add(new Paragraph("Student ID: " + report.getStudentID(), textFont));
        doc.add(new Paragraph("Name: " + report.getStudentName(), textFont));
        doc.add(new Paragraph("Program: " + report.getProgram(), textFont));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell("Course Code");
        table.addCell("Course Name");
        table.addCell("Credit Hours");
        table.addCell("Grade Point");

        for (AcademicReportNew.CourseGrade cg : report.getCourseGrades()) {

            System.out.println("DEBUG COURSE:");
            System.out.println(" code = " + cg.getCourseCode());
            System.out.println(" name = " + cg.getCourseName());
            System.out.println(" credit = " + cg.getCreditHours());
            System.out.println(" grade = " + cg.getGradePoint());

            String code = (cg.getCourseCode() == null) ? "" : cg.getCourseCode();
            String name = (cg.getCourseName() == null) ? "" : cg.getCourseName();
            String credits = String.valueOf(cg.getCreditHours());
            String grade = String.format("%.2f", cg.getGradePoint());

            table.addCell(code);
            table.addCell(name);
            table.addCell(credits);
            table.addCell(grade);
        }

        doc.add(table);

        doc.add(new Paragraph("\nCGPA: " + String.format("%.2f", report.getCgpa()), textFont));

        doc.add(new Paragraph("Failed Courses: " +
                (report.getFailedCourseCodes().isEmpty() ? "None" : String.join(", ", report.getFailedCourseCodes())), textFont));

        doc.close();

        JOptionPane.showMessageDialog(this,
                "PDF Report exported successfully:\n" + outputPath);

        //
        // Email part
        //
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to email this academic report to the student?",
                "Send Academic Report",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) 
        {
            Student target = currentStudent;
            File pdfFile = new File(outputPath);

            NotificationService.sendAcademicReportEmail(target,pdfFile);

            JOptionPane.showMessageDialog(this,
                    "Academic report emailed to the student.",
                    "Email Sent",
                    JOptionPane.INFORMATION_MESSAGE);
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
                "Failed to export PDF: " + e.getMessage());
        e.printStackTrace();
    }
}
}