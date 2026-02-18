package pack.crs.ui;

import pack.crs.models.*;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import pack.crs.utils.*;

public class RegistrationWindow extends JFrame {

    public RegistrationWindow(String studentID, EligibilityResult result) {
        super("Student Registration");
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel title = new JLabel("Registration Form", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        infoPanel.add(new JLabel("Student ID: " + studentID));
        infoPanel.add(new JLabel(String.format("CGPA: %.2f", result.getCgpa())));
        infoPanel.add(new JLabel("Eligibility: Eligible for next semester"));
        infoPanel.add(new JLabel("Failed Courses: " + result.getFailedCount()));

        if (!result.getFailedCourses().isEmpty()) {
            infoPanel.add(new JLabel("Failed Course List: " + String.join(", ", result.getFailedCourses())));
        } else {
            infoPanel.add(new JLabel("No failed courses."));
        }

        //  Academic summary
        JTextArea academicArea = new JTextArea(10, 40);
        academicArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        academicArea.setEditable(false);
        academicArea.setBorder(BorderFactory.createTitledBorder("Course Summary"));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-10s\n", "Course", "Grade"));
        sb.append("--------------------\n");
        for (Map.Entry<String, Double> entry : result.getAllCourses().entrySet()) {
            sb.append(String.format("%-10s %.2f\n", entry.getKey(), entry.getValue()));
        }
        academicArea.setText(sb.toString());

        // Confirmation section
        JPanel confirmPanel = new JPanel(new BorderLayout(10, 10));
        JTextArea confirmationText = new JTextArea(
                "This student meets all progression criteria and may proceed with registration.\n" +
                        "Use this window to confirm enrolment into the next level of study.\n\n");
        confirmationText.setEditable(false);
        confirmationText.setBackground(new Color(240, 255, 240));
        confirmationText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confirmationText.setWrapStyleWord(true);
        confirmationText.setLineWrap(true);
        confirmationText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        confirmPanel.add(confirmationText, BorderLayout.CENTER);

        //  Button bar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRegister = new JButton("Confirm Registration");
        JButton btnClose = new JButton("Close");
        btnRegister.addActionListener(e -> {
            try {
                // Load all students from file
                java.util.List<Student> list = pack.crs.utils.FileUtils.importStudents(null);

                Student target = null;
                for (Student s : list) {
                    if (s.getStudentID().equals(studentID)) {
                        target = s;
                        break;
                    }
                }
                
                //
                // Email Part
                //
                if (target != null) {
                    pack.crs.utils.NotificationService.sendRegistrationConfirmationEmail(target);
                }

                JOptionPane.showMessageDialog(this,
                        "Student successfully registered for next semester!\nConfirmation email sent.",
                        "Registration Complete", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Registration complete, but email sending failed: " + ex.getMessage(),
                        "Warning", JOptionPane.WARNING_MESSAGE);
                ex.printStackTrace();
            }

            dispose();
        });


        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnRegister);
        btnPanel.add(btnClose);

        // Layout assembly
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(academicArea), BorderLayout.CENTER);
        centerPanel.add(confirmPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // ️ Frame setup
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}
