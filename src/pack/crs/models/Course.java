package pack.crs.models;

public class Course {
    private String courseID;
    private String courseName;
    private int creditHours;

    public Course(String courseID, String courseName, int creditHours) {
        if(courseID == null || courseID.isEmpty())
            throw new IllegalArgumentException("courseID cannot be empty");
        if(courseName == null || courseName.isEmpty())
            throw new IllegalArgumentException("courseName cannot be empty");
        if(creditHours <= 0)
            throw new IllegalArgumentException("creditHours must be positive");
        this.courseID = courseID;
        this.courseName = courseName;
        this.creditHours = creditHours;
    }

    public String getCourseID() { return courseID; }
    public String getCourseName() { return courseName; }
    public int getCreditHours() { return creditHours; }
}
