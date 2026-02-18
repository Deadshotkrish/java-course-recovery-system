package pack.crs.utils;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailSender
{
    private static final String SENDER_EMAIL = "aiboyzuniversity@gmail.com";
    private static final String APP_PASSWORD = "bxqg ypok llsl hvvi"; // Gmail app password

    private static Session createSession()
    {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });
    }

    //
    //Sends an email with a text body and optional file attachment.
    //
    public static void sendEmail(String to, String subject, String body, File attachment) throws MessagingException
    {
        Session session = createSession();
        MimeMessage message = new MimeMessage(session);
        
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        // Text part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body);
        multipart.addBodyPart(textPart);

        // Attachment part (PDF)
        if (attachment != null)
        {
            MimeBodyPart filePart = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(attachment);
            filePart.setDataHandler(new DataHandler(fds));
            filePart.setFileName(attachment.getName());
            multipart.addBodyPart(filePart);
        }

        message.setContent(multipart);
        Transport.send(message);
    }
}
