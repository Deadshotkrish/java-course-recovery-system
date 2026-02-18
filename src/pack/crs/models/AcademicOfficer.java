package pack.crs.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.*;


/**
 * AcademicOfficer class
 * Handles operations specific to academic officers, including eligibility checking.
 *
 * NOTE: This version follows UML — checkEligibility() is a method, not a class.
 * It throws Exception, and the GUI catches and handles it.
 */
public class AcademicOfficer extends User {

    private String facultyName;  // Example of subclass-specific field

    // Constructor
    public AcademicOfficer(String userId, String username, String password, boolean active) {
        super(userId, username, password, "AcademicOfficer", active);
        this.facultyName = "Unassigned"; // default value
    }
    
    // Alternate constructor for email
    public AcademicOfficer(String userId, String username, String password, boolean active, String email) {
        super(userId, username, password, "AcademicOfficer", active);
        setEmail(email);
    }

    // Getters and setters
    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    /**
     * Checks student eligibility based on CGPA and number of failed courses.
     * Reads student info from students.txt with format:
     * StudentID|Name|Program|CourseCode:Grade|CourseCode:Grade|...
     * <p>
     * Rules:
     * - CGPA >= 2.0
     * - Failed courses (grade < 2.0) <= 3
     *
     * @param studentID The ID of the student to check.
     * @return true if eligible, otherwise throws Exception with reason.
     * @throws Exception if student not eligible or file not found.
     */
    public EligibilityResult checkEligibility(String studentID) throws Exception {
        File studentFile = new File(System.getProperty("user.dir")
                + File.separator + "Resources" + File.separator + "students.txt");
        File courseFile = new File(System.getProperty("user.dir")
                + File.separator + "Resources" + File.separator + "courses.txt");

        if (!studentFile.exists()) {
            throw new FileNotFoundException("students.txt file not found in Resources folder.");
        }
        if (!courseFile.exists()) {
            throw new FileNotFoundException("courses.txt file not found in Resources folder.");
        }

        // Load course credit hours
        Map<String, Integer> courseCredits = new HashMap<>();
        try (Scanner sc = new Scanner(courseFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    try {
                        courseCredits.put(parts[0].trim(), Integer.parseInt(parts[2].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Find student
        String foundLine = null;
        try (Scanner sc = new Scanner(studentFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts[0].equalsIgnoreCase(studentID)) {
                    foundLine = line;
                    break;
                }
            }
        }

        if (foundLine == null)
            throw new Exception("Student ID " + studentID + " not found in students.txt.");

        // Parse record
        String[] fields = foundLine.split("\\|");
        if (fields.length < 4)
            throw new Exception("No course data found for student " + studentID);

        double totalGradePoints = 0;
        int totalCredits = 0;
        int failedCount = 0;
        Map<String, Double> allCourses = new LinkedHashMap<>();
        List<String> failedCourses = new ArrayList<>();

        for (int i = 3; i < fields.length; i++) {
            String[] pair = fields[i].split(":");
            if (pair.length == 2) {
                String courseID = pair[0].trim();
                try {
                    double grade = Double.parseDouble(pair[1].trim());
                    int credit = courseCredits.getOrDefault(courseID, 3);
                    allCourses.put(courseID, grade);
                    totalGradePoints += grade * credit;
                    totalCredits += credit;
                    if (grade < 2.0) {
                        failedCount++;
                        failedCourses.add(courseID);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (totalCredits == 0)
            throw new Exception("No valid credit hours found for student " + studentID);

        double cgpa = totalGradePoints / totalCredits;
        boolean eligible = (cgpa >= 2.0 && failedCount <= 3);

        return new EligibilityResult(cgpa,eligible, failedCount, failedCourses, allCourses);
    }
}