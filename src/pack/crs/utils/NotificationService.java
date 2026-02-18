package pack.crs.utils;

import jakarta.mail.MessagingException;
import pack.crs.models.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class NotificationService 
{
    // Session based keys to avoid sending duplicate emails
    private static final Set<String> Keys = new HashSet<>(); //HashSet for collection of unique values

    private static void sendCheck(String key, String to, String subject, String body, File attachment) 
    {
        if (!Keys.add(key)) return; //Doesn't send if key already existed

        try 
        {
            EmailSender.sendEmail(to, subject, body, attachment);
        } 
        catch (MessagingException e){}
    }

    //
    // Recovery Plan
    //
    public static void sendRecoveryPlanEmail(Student student, RecoveryPlan plan, File pdfFile) 
    {
        if (student == null || pdfFile == null) return;

        String to = student.getEmail();
        String subject = "Course Recovery Plan for " + plan.getStudent().getStudentID();
        String body = "Dear " + student.getName() + ",\n\n"
                + "Please find attached your course recovery plan.\n"
                + "This plan includes your milestones and action steps.\n\n"
                + "Regards,\nAcademic Office";

        String key = student.getStudentID() + "_RECOVERY";  //Key stored

        sendCheck(key, to, subject, body, pdfFile); //Email sent (once per session)
    }
    
    //
    // Academic Report
    //
    public static void sendAcademicReportEmail(Student student, File pdfFile) 
    {
        if (student == null || pdfFile == null) return;

        String to = student.getEmail();
        String subject = "Your Semester Academic Performance Report for " + student.getName();
        String body = "Dear " + student.getName() + ",\n\n"
                + "Please find attached your academic performance report for this semester.\n\n"
                + "Regards,\nAcademic Office";

        String key = student.getStudentID() + "_REPORT_";  //Key stored

        sendCheck(key, to, subject, body, pdfFile); //Email sent (once per session)
    }
    
    //
    // New user creation
    //
    public static void sendAccountCreatedEmail(User user, String password) 
    {
        if (user == null || user.getEmail() == null) return;

        String subject = "Your account has been created";
        String body = "Dear " + user.getUsername() + ",\n\n"
                + "An account has been created for you on the Course Recovery System with the following details:\n\n"
                + "Username: " + user.getUsername() + "\n"
                + "Temporary password: " + password + "\n"
                + "(Please change it after first login.)\n"
                +"\nRole: " + user.getRole() + "\n\n"
                + "If you didn't request this, please contact the admin.\n\n"
                + "Regards,\nSystem Administration";

        String key = user.getUserId() + "_ACCOUNT_CREATED"; //To make sure no accidental duplicate users
        sendCheck(key, user.getEmail(), subject, body, null);
    }

    //
    // User edit
    //
    public static void sendAccountUpdatedEmail(User user, int edit) 
    {
        if (user == null || user.getEmail() == null) return;
        
        String subject = "Your account was updated";
        String body = "Hello " + user.getUsername() + ",\n\n"
            + "Your account information has been updated.\n\n"
            + "Current account details:\n\n"
            + "Username: " + user.getUsername() + "\n"
            + "Password: " + user.getPassword() + "\n"
            + "Email: " + user.getEmail() + "\n"
            + "Role: " + user.getRole() + "\n"
            + "Active: " + user.isActive() + "\n\na"
            + "If you did not request this change, please contact admin.\n";

        String key = user.getUserId() + "_UPDATED" + edit;  //+edit as a workaround because there can be multiple edits
        sendCheck(key, user.getEmail(), subject, body, null);
}


    //
    // Password recovery
    //
    public static void sendPasswordRecoveryEmail(User user)
    {
        if (user == null || user.getEmail() == null) return;
        
        String to = user.getEmail();

        String subject = "Password recovery for your CRS account";
        String body = "Dear " + user.getUsername() + ",\n\n"
                + "You requested a password recovery. Your current password is:\n\n"
                + "    " + user.getPassword() + "\n\n"
                + "If you did not request this, contact the admin immediately.\n\n"
                + "Regards,\nSystem Administration";

        String key = user.getUserId() + "_PASSWORD_RECOVERY";
        sendCheck(key, to, subject, body, null);
    }

    //
    // Register to next semester confirmation
    //
    public static void sendRegistrationConfirmationEmail(Student student) {
        if (student == null || student.getEmail() == null) return;
        
        String to = student.getEmail();

        String subject = "Registration Confirmation - Accepted";
        String body = "Dear " + student.getName() + ",\n\n"
                + "Congratulations! you have been registered eligible for the next semester.\n\n"
                + "Student ID: " + student.getStudentID() + "\n"
                + "Program   : " + student.getProgram() + "\n\n"
                + "If you have any questions, please contact the Academic Office.\n\n"
                + "Regards,\nAcademic Office";

        String key = student.getStudentID() + "_REG_";
        sendCheck(key, to, subject, body, null);
    }
}
