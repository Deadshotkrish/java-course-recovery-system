package pack.crs.utils;

import java.io.File;
import java.util.*;
import pack.crs.models.*;

/**
 * Utility class for file handling (import/export of data).
 * Corrected to support:
 *  - Student email loading
 *  - Proper constructor usage
 *  - Correct grade parsing index
 */
public class FileUtils {

    /**
     * Imports students from students.txt
     * Expected format:
     * StudentID | Name | Email | Program | CourseCode:Grade | CourseCode:Grade | ...
     */
    public static List<Student> importStudents(File file) {
        List<Student> students = new ArrayList<>();

        try {
            // Auto-locate file inside Resources if null or missing
            if (file == null || !file.exists()) {
                file = new File(System.getProperty("user.dir")
                        + File.separator + "Resources"
                        + File.separator + "students.txt");
            }

            System.out.println("🔍 Looking for students.txt at: " + file.getAbsolutePath());

            if (!file.exists()) {
                System.err.println("❌ File not found: " + file.getAbsolutePath());
                return students;
            }

            try (Scanner sc = new Scanner(file)) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\|");
                    if (parts.length < 4) {
                        System.err.println("⚠️ Skipping malformed line (needs ID|Name|Email|Program): " + line);
                        continue;
                    }

                    String studentID = parts[0].trim();
                    String name      = parts[1].trim();
                    String email     = parts[2].trim();
                    String program   = parts[3].trim();

                    // Parse course grades from index 4 onward
                    Map<Course, Double> grades = new LinkedHashMap<>();
                    for (int i = 4; i < parts.length; i++) {
                        String token = parts[i].trim();
                        if (token.isEmpty()) continue;

                        String[] courseGrade = token.split(":");
                        if (courseGrade.length != 2) {
                            System.err.println("⚠️ Invalid course entry: " + token);
                            continue;
                        }

                        String courseCode = courseGrade[0].trim();
                        double gradePoint;

                        try {
                            gradePoint = Double.parseDouble(courseGrade[1].trim());
                        } catch (NumberFormatException ex) {
                            System.err.println("⚠️ Invalid grade format: " + token);
                            continue;
                        }

                        grades.put(new Course(courseCode, courseCode, 3), gradePoint);
                    }

                    AcademicReport report = new AcademicReport("RPT-" + studentID, grades);

                    
                    Student student = new Student(
                            studentID,    
                            email,       
                            "password",   
                            true,         
                            studentID,
                            name,
                            email,        
                            program,
                            report
                    );

                    students.add(student);
                }
            }

            System.out.println(" Loaded " + students.size() + " student(s) from file.");

        } catch (Exception e) {
            System.err.println(" Error reading students.txt: " + e.getMessage());
        }

        return students;
    }
}
