package email.config.property;

import java.util.Properties;

public class SMTPProperties implements PropertyImpl {
    @Override
    public Properties getProperties() {
        final Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        return properties;
    }
}