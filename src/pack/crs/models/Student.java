package pack.crs.models;

public class Student extends User {
    private String studentID;
    private String name;
    private String program;
    private AcademicReport academicReport;
    private String email;

    public Student(String userId, String username, String password, boolean active,
                   String studentID, String name, String email, String program,
                   AcademicReport academicReport) {
        super(userId, username, password, "Student", active);
        if (studentID == null || studentID.isEmpty()) throw new IllegalArgumentException("studentID cannot be empty");
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name cannot be empty");
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("email cannot be empty");
        if (program == null || program.isEmpty()) throw new IllegalArgumentException("program cannot be empty");
        if (academicReport == null) throw new IllegalArgumentException("academicReport cannot be null");
        this.studentID = studentID;
        this.name = name;
        this.program = program;
        this.academicReport = academicReport;
        this.email = email;
    }

    public String getStudentID() { return studentID; }
    public String getName() { return name; }
    public String getProgram() { return program; }
    public AcademicReport getAcademicReport() { return academicReport; }

    public String getEmail() {
        return email;
    }
}
