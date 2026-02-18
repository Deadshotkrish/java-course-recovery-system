/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pack.crs.models;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author User
 */
public class RecoveryPlan {
   private Student student;
   private List<RecoveryMilestone> milestones;
   
   public RecoveryPlan(Student student){
       if (student == null)
           throw new IllegalArgumentException("student cannot be a null");
       
       this.student = student;
       this.milestones = new ArrayList<>();
        populateFromFailedCourses();
   }
   
   private void populateFromFailedCourses() {
        AcademicReport report = student.getAcademicReport();
        report.getGrades().forEach((course, grade) -> {
            if (grade < 2.0) {
                milestones.add(new RecoveryMilestone(
                        course.getCourseID(),  // courseCode
                        "",                    // taskDescription
                        null,                  // dueDate
                        "Pending",             // status
                        null                   // grade
                ));
            }
        });
    }
   
   
   public List<RecoveryMilestone> getMilestones()
   {
       return milestones;
   }
   
   public void addMilestone(RecoveryMilestone milestone)
   {
       if (milestone != null) milestones.add(milestone);
   }
   
   public Student getStudent()
   {
       return student;
   }
   
   public void removeMilestone(RecoveryMilestone milestone)
   {
       milestones.remove(milestone);
   }
   
   public String toString()
   {
       StringBuilder sb = new StringBuilder();
       sb.append("Recovery Plan for ").append(student.getName()).append(" (").append(student.getStudentID()).append(")\n");
       for (RecoveryMilestone m : milestones)
       {
           sb.append(m).append("\n");
       }
       return sb.toString();
   }
}
