package email.config.property;

public class PropertyFactory {
    public static PropertyImpl getConfig(EmailProperty configType) {
        if (configType.equals(EmailProperty.POP3)) {
            return new POP3Properties();
        } else if (configType.equals(EmailProperty.IMAP)) {
            return new IMAPProperties();
        } else if (configType.equals(EmailProperty.SMTP)) {
            return new SMTPProperties();
        } else {
            return null;
        }
    }
}
