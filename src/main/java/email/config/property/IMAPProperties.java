package email.config.property;

import java.util.Properties;

public class IMAPProperties implements PropertyImpl {
    @Override
    public Properties getProperties() {
        final Properties properties = System.getProperties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com");
        properties.put("mail.imaps.port", "993");
        properties.put("mail.imaps.timeout", "10000");
        return properties;
    }
}
