\# Course Recovery System (Java OOP Project)



&nbsp;Overview

The Course Recovery System is a Java-based desktop application developed using Object-Oriented Programming principles. The system manages students who require course recovery plans based on academic eligibility.



The project demonstrates strong implementation of core OOP concepts and multi-role system architecture.



\##  Key Features



\- Multi-role login system (Student, Academic Officer, System Admin)

\- Course eligibility checking

\- Academic report generation

\- Recovery plan creation with milestones

\- PDF export functionality

\- Email notification system

\- GUI-based system using Java Swing


## Data Storage

The system uses text files stored in the `resources/` directory
to simulate database functionality.

These files manage:
- User accounts
- Course records
- Recovery plans
- Academic reports




\## OOP Concepts Implemented



\- Encapsulation

\- Inheritance (User → Student / AcademicOfficer / SystemAdmin)

\- Polymorphism

\- Abstraction via Interfaces

\- Separation of Concerns (Models, UI, Utils, Auth layers)



\## Technologies Used



\- Java

\- Java Swing (GUI)

\- File Handling

\- OOP Design Principles



\## Project Structure



\- `models/` → Core data classes

\- `auth/` → Authentication and user management

\- `ui/` → GUI components

\- `utils/` → Helper services (PDF, Email, Reports)




##  Requirements

- Java 17
- NetBeans / IntelliJ (or any Java IDE)

##  External Libraries

This project uses the following external libraries:

- iText PDF
- Jakarta Mail
- Jakarta Activation

If running the project outside the original development environment,
you may need to manually add the required JAR files to your project's classpath.


##  Important Folder Structure

The system uses text files stored inside the `Resources` folder
located in the project root directory.

Do NOT move or rename the `Resources` folder,
or the application may fail to locate required data files.





\## How To Run



1\. Clone repository

2\. Open in NetBeans / IntelliJ

3\. Run `OODJAssignment.java`

---

##  Application Screenshots

### Login Interface
![Login](Login.png)

Use the users.txt file for login information

### Academic Officer Dashboard
![Academic Officer Dashboard](AcademicOfficerDashboard.png)

###  Academic Report Generation
![Academic Report](AcademicReport.png)

###  Course Recovery Plan Interface
![Course Recovery](CourseRecovery.png)

###  Eligibility Checking Module
![Eligibility](Eligibility.png)







---

##  Future Improvements

While the system successfully implements core Object-Oriented Programming concepts and functional requirements, several improvements could enhance the overall quality and user experience:

###  User Interface Enhancements
- Redesign the GUI with improved layout consistency and visual hierarchy
- Implement modern UI frameworks (e.g., JavaFX) instead of basic Swing components
- Improve spacing, alignment, and color scheme for better usability
- Add responsive resizing support

###  Code & Architecture Improvements
- Refactor file handling to use resource-based loading instead of absolute paths
- Implement proper logging instead of console printing
- Introduce unit testing (JUnit) for core modules
- Improve exception handling with custom exceptions

###  Database Integration
- Replace text-file storage with a relational database (e.g., MySQL or PostgreSQL)
- Implement DAO pattern for better separation of concerns

###  Security Enhancements
- Encrypt stored passwords
- Add role-based access validation
- Improve email configuration security

###  Deployment & Packaging
- Package as an executable JAR
- Add installation guide
- Improve portability across systems

---

This project focused primarily on demonstrating strong Object-Oriented Programming principles and system structure rather than advanced UI/UX design.




---



Developed by Krish Sharma  

BSc Computer Science (AI)  

Asia Pacific University










