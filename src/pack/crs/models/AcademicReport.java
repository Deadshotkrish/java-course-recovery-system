package pack.crs.models;

import java.util.*;

public class AcademicReport {
    private String reportID;
    private Map<Course, Double> grades; // Course to grade map
    private Double gpa;

    public AcademicReport(String reportID, Map<Course, Double> grades) {
        if (reportID == null || reportID.isEmpty())
            throw new IllegalArgumentException("reportID cannot be empty");
        if (grades == null)
            throw new IllegalArgumentException("grades cannot be null");
        this.reportID = reportID;
        this.grades = grades;
    }
    
    private double computeGPA(){
        if (grades == null || grades.isEmpty()){
            return 0.0;
        }
        double total = 0;
        int count = 0;
        
        for (Double d : grades.values()){
            total += d;
            count++;
        }
        return (count == 0) ? 0.0 : total / count;
    }
    
    // Calculates GPA as average of grades
    public double getGPA() {
        // If GPA was manually set, use that instead of recalculating
        if (gpa != null) {
            return gpa;
        }
        return computeGPA();
    }
    public void setGPA(double gpa){
        this.gpa = gpa;
    }

    // Count failed courses (<2.0 grade)
    public int countFailedCourses() {
        int count = 0;
        for (Double g : grades.values()) if (g < 2.0) count++;
        return count;
    }
    
    public Map<Course, Double> getGrades() 
    {
    return grades;
    }

}