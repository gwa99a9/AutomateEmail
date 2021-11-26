package email.sender;

import email.config.EmailAccount;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    public static void send(String to, String subject, String content, Session s) throws MessagingException {

        MimeMessage msg = new MimeMessage(s);
        msg.setFrom(EmailAccount.EMAIL_ADDRESS);
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setRecipients(Message.RecipientType.TO, to);
        msg.setSubject(subject);

        msg.setText(content);
        //msg.setContent(content, "text/html");
        Transport.send(msg);
    }
}
