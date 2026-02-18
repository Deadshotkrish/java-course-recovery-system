package pack.crs.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import pack.crs.models.AcademicReportNew;

public class AcademicReportGenerator implements IReportGenerator {

    private final Path resourcesDir;

    private static final double PASS_THRESHOLD = 2.0;

    public AcademicReportGenerator() {
        this("Resources");
    }

    public AcademicReportGenerator(String resourcesFolder) {
        this.resourcesDir = Paths.get(resourcesFolder);
    }

    public AcademicReportNew generateReport(String studentID) throws IOException {
        Map<String, CourseInfo> courseMap = loadCourses();
        StudentRecord srec = loadStudentRecord(studentID);
        if (srec == null) {
            throw new FileNotFoundException("Student ID not found in students.txt: " + studentID);
        }

        List<AcademicReportNew.CourseGrade> courseGrades = new ArrayList<>();
        double totalPoints = 0.0;
        int totalCredits = 0;
        List<String> failed = new ArrayList<>();

        for (Map.Entry<String, Double> entry : srec.courseGrades.entrySet()) {
            String courseCode = entry.getKey();
            double gradePoint = entry.getValue();
            CourseInfo cinfo = courseMap.get(courseCode);
            if (cinfo == null) {
                // If no course, set to "unknown" or value 0
                cinfo = new CourseInfo(courseCode, "UNKNOWN", 0);
            }
            courseGrades.add(new AcademicReportNew.CourseGrade(courseCode, cinfo.courseName, cinfo.creditHours, gradePoint));

            totalPoints += gradePoint * cinfo.creditHours;
            totalCredits += cinfo.creditHours;

            if (gradePoint < PASS_THRESHOLD) {
                failed.add(courseCode);
            }
        }

        double cgpa = totalCredits == 0 ? 0.0 : totalPoints / totalCredits;
        // Rounds up to only 2 decimal value
        cgpa = Math.round(cgpa * 100.0) / 100.0;

        AcademicReportNew report = new AcademicReportNew(srec.studentID, srec.name, srec.program, courseGrades, cgpa, failed);
        // Write into file
        writeReportToFile(report);
        return report;
    }

    private Map<String, CourseInfo> loadCourses() throws IOException {
        Path path = resourcesDir.resolve("courses.txt");
        Map<String, CourseInfo> map = new HashMap<>();
        if (!Files.exists(path)) return map;
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    int credits = 0;
                    try {
                        credits = Integer.parseInt(parts[2].trim());
                    } catch (NumberFormatException ex) { credits = 0; }
                    map.put(code, new CourseInfo(code, name, credits));
                }
            }
        }
        return map;
    }

    private StudentRecord loadStudentRecord(String studentID) throws IOException {
        Path path = resourcesDir.resolve("students.txt");
        if (!Files.exists(path)) return null;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                String id = parts[0].trim();
                if (!id.equals(studentID)) continue;

                String name = parts[1].trim();
                String program = parts[3].trim();

                Map<String, Double> courseGrades = new LinkedHashMap<>();
                for (int i = 4; i < parts.length; i++) {
                    String tok = parts[i].trim();
                    if (tok.isEmpty()) continue;
                    String[] cg = tok.split(":");
                    if (cg.length == 2) {
                        String courseCode = cg[0].trim();
                        double gp;
                        try { gp = Double.parseDouble(cg[1].trim()); }
                        catch (NumberFormatException ex) { gp = 0.0; }
                        courseGrades.put(courseCode, gp);
                    }
                }

                return new StudentRecord(id, name, program, courseGrades);
            }
        }
        return null;
    }


    private void writeReportToFile(AcademicReportNew report) throws IOException {
        Path outDir = resourcesDir.resolve("reports");
        if (!Files.exists(outDir)) Files.createDirectories(outDir);
        Path outFile = outDir.resolve(report.getStudentID() + "_AcademicReport.txt");
        try (BufferedWriter bw = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(generateReportText(report));
        }
    }

    private String generateReportText(AcademicReportNew report) {
        StringBuilder sb = new StringBuilder();
        sb.append("ACADEMIC PERFORMANCE REPORT").append(System.lineSeparator());
        sb.append("Student ID: ").append(report.getStudentID()).append(System.lineSeparator());
        sb.append("Name      : ").append(report.getStudentName()).append(System.lineSeparator());
        sb.append("Program   : ").append(report.getProgram()).append(System.lineSeparator());
        sb.append("------------------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("%-8s %-30s %5s %8s%n","Code", "Course Title", "Credit", "Grade"));
        sb.append("------------------------------------------------------------").append(System.lineSeparator());
        for (AcademicReportNew.CourseGrade cg : report.getCourseGrades()) {
            sb.append(String.format("%-8s %-30s %5d %8.2f%n",
                    cg.getCourseCode(),
                    abbreviate(cg.getCourseName(), 30),
                    cg.getCreditHours(),
                    cg.getGradePoint()));
        }
        sb.append("------------------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("Cumulative GPA (CGPA): %.2f%n", report.getCgpa()));
        sb.append(System.lineSeparator());
        sb.append("Failed Courses: ");
        if (report.getFailedCourseCodes().isEmpty()) sb.append("None");
        else sb.append(String.join(", ", report.getFailedCourseCodes()));
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Generated on: ").append(new Date()).append(System.lineSeparator());
        return sb.toString();
    }

    private String abbreviate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }

    private static class CourseInfo {
        final String code;
        final String courseName;
        final int creditHours;
        CourseInfo(String code, String courseName, int creditHours) {
            this.code = code;
            this.courseName = courseName;
            this.creditHours = creditHours;
        }
    }

    private static class StudentRecord {
        final String studentID;
        final String name;
        final String program;
        final Map<String, Double> courseGrades;
        StudentRecord(String studentID, String name, String program, Map<String, Double> courseGrades) {
            this.studentID = studentID;
            this.name = name;
            this.program = program;
            this.courseGrades = courseGrades;
        }
    }
}