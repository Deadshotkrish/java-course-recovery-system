/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pack.crs.models;

import java.time.LocalDate;
/**
 *
 * @author User
 */
public class RecoveryMilestone {
    private String courseCode;
    private String taskDescription;
    private LocalDate dueDate;
    private String status;
    private String grade;
    
    public RecoveryMilestone( String courseCode, String taskDescription, LocalDate dueDate, String status, Double grade)
    {
        if (courseCode == null || courseCode.isEmpty())
            throw new IllegalArgumentException("course cannot be empty");
        this.courseCode = courseCode;
        this.taskDescription = taskDescription;
        this.dueDate = dueDate;
        this.status = "Pending";
        this.grade = null;
    }
    
    public String getCourseCode()
    {
        return courseCode;
    }
    public void setCourseCode(String courseCode)
    {
        this.courseCode = courseCode;
    }
    
    public String getTaskDescription()
    {
        return taskDescription;
    }
    public void setTaskDescription(String taskDescription)
    {
        this.taskDescription = taskDescription;
    }
    
    public LocalDate getDueDate()
    {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate)
    {
        this.dueDate = dueDate;
    }
    
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    
    public String getGrade()
    {
        return grade;
    }
    public void setGrade(String grade)
    {
        this.grade = grade;
    }
    
    @Override
    public String toString()
    {
        return String.format("Course: %s, Task: %s, Status: %s, Grade: %s",
                courseCode, taskDescription, dueDate, status, grade == null ? "N/A" : grade);
    }
}
