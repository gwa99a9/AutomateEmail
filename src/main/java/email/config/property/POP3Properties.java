package email.config.property;

import java.util.Properties;

public class POP3Properties implements PropertyImpl {
    @Override
    public Properties getProperties() {
        final Properties properties = System.getProperties();
        properties.put("mail.pop3.host", "pop.gmail.com");
        properties.put("mail.pop3.port", "995");
        properties.put("mail.pop3.starttls.enable", "true");
        return properties;
    }
}
