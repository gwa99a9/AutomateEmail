package email.listener;

import com.sun.mail.imap.IMAPStore;
import email.config.EmailAccount;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;

public class EmailListener implements EmailListenerImpl {
    private final Session session;
    private IMAPStore store;
    private Folder inbox;

    public EmailListener(Session session) {
        this.session = session;
    }

    @Override
    public void init() throws MessagingException {
        store = (IMAPStore) session.getStore("imaps");
        connect();
        if (hasIdleSupport()) {
            throw new RuntimeException("IDLE not supported");
        }
        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
    }

    @Override
    public void connect() throws MessagingException {
        store.connect(EmailAccount.EMAIL_ADDRESS, EmailAccount.EMAIL_PASSWORD);
    }

    @Override
    public boolean hasIdleSupport() throws MessagingException {
        return !store.hasCapability("IDLE");
    }

    public Session getSession() {
        return session;
    }

    public IMAPStore getStore() {
        return store;
    }

    @Override
    public Folder getInbox() {
        return inbox;
    }
}
