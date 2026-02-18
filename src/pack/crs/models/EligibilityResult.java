package pack.crs.models;

import java.util.*;

public class EligibilityResult {
    private double cgpa;
    private boolean eligible;
    private int failedCount;
    private List<String> failedCourses;
    private Map<String, Double> allCourses;

    public EligibilityResult(double cgpa, boolean eligible, int failedCount,
                             List<String> failedCourses, Map<String, Double> allCourses) {
        this.cgpa = cgpa;
        this.eligible = eligible;
        this.failedCount = failedCount;
        this.failedCourses = failedCourses;
        this.allCourses = allCourses;
    }

    public double getCgpa() { return cgpa; }
    public boolean isEligible() { return eligible; }
    public int getFailedCount() { return failedCount; }
    public List<String> getFailedCourses() { return failedCourses; }
    public Map<String, Double> getAllCourses() { return allCourses; }
}
