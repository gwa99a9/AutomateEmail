package email.config;

import email.config.property.PropertyImpl;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class EmailConfig {

    private final PropertyImpl property;
    private Authenticator auth;

    public EmailConfig(PropertyImpl property) {
        this.property = property;
        auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailAccount.EMAIL_ADDRESS, EmailAccount.EMAIL_PASSWORD);
            }
        };
    }

    public Session getSession() {
        return Session.getDefaultInstance(property.getProperties(), auth);
    }

}
