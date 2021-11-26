package email.listener;

import javax.mail.Folder;
import javax.mail.MessagingException;


public interface EmailListenerImpl {
    void init() throws MessagingException;

    void connect() throws MessagingException;

    boolean hasIdleSupport() throws MessagingException;

    Folder getInbox() throws MessagingException;
}
