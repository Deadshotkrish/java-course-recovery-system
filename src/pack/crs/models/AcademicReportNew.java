package pack.crs.models;

import java.util.List;
import java.util.Map;

public class AcademicReportNew {
    private String studentID;
    private String studentName;
    private String program;
    private double cgpa;
    private List<CourseGrade> courseGrades;
    private List<String> failedCourseCodes;

    public AcademicReportNew(String studentID, String studentName, String program,
                          List<CourseGrade> courseGrades, double cgpa, List<String> failedCourseCodes) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.program = program;
        this.courseGrades = courseGrades;
        this.cgpa = cgpa;
        this.failedCourseCodes = failedCourseCodes;
    }

    public String getStudentID() { return studentID; }
    public String getStudentName() { return studentName; }
    public String getProgram() { return program; }
    public double getCgpa() { return cgpa; }
    public List<CourseGrade> getCourseGrades() { return courseGrades; }
    public List<String> getFailedCourseCodes() { return failedCourseCodes; }

    public static class CourseGrade {
        private String courseCode;
        private String courseName;
        private int creditHours;
        private double gradePoint;

        public CourseGrade(String code, String name, int credits, double grade) {
            this.courseCode = code;
            this.courseName = name;
            this.creditHours = credits;
            this.gradePoint = grade;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public int getCreditHours() { return creditHours; }
        public double getGradePoint() { return gradePoint; }
    }
}