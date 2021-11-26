package main;

import email.config.EmailConfig;
import email.config.property.EmailProperty;
import email.config.property.PropertyFactory;
import email.sender.EmailSender;

import javax.mail.Session;

public class EmailSendTester {
    public static void main(String[] args) {
        Session emailSession = new EmailConfig(PropertyFactory.getConfig(EmailProperty.SMTP)).getSession();
        try {
            EmailSender.send("jamesheller.jeewake@gmail.com", "Test Mail", "Test Mail Body", emailSession);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
