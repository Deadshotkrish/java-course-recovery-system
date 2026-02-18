package pack.crs.models;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;

public class RecoveryPlanPDFExporter {

    public static void exportToPDF(RecoveryPlan plan, String outPath) throws Exception {

        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(outPath));

        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font textFont = new Font(Font.FontFamily.HELVETICA, 11);

        // Title
        Paragraph title = new Paragraph("Recovery Plan Report\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        // Student info
        Student s = plan.getStudent();
        doc.add(new Paragraph("Student ID: " + s.getStudentID(), textFont));
        doc.add(new Paragraph("Name: " + s.getName(), textFont));
        doc.add(new Paragraph("Program: " + s.getProgram(), textFont));
        doc.add(new Paragraph("\n"));

        // Table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 4, 2, 2, 2});

        addHeaderCell(table, "Course");
        addHeaderCell(table, "Task");
        addHeaderCell(table, "Due Date");
        addHeaderCell(table, "Status");
        addHeaderCell(table, "Grade");

        for (RecoveryMilestone m : plan.getMilestones()) {
            table.addCell(safe(m.getCourseCode()));
            table.addCell(safe(m.getTaskDescription()));
            table.addCell(m.getDueDate() == null ? "" : m.getDueDate().toString());
            table.addCell(safe(m.getStatus()));
            table.addCell(m.getGrade() == null ? "" : m.getGrade().toString());
        }

        doc.add(table);
        doc.close();
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
