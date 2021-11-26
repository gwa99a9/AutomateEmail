package email.listener;

import com.sun.mail.imap.IMAPStore;
import email.config.EmailAccount;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

public class EmailListenerUtil {
    public static void closeStore(IMAPStore store) {
        try {
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    public static void closeFolder(Folder folder) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    public static void ensureOpen(Folder folder) throws MessagingException {
        if (folder != null) {
            Store store = folder.getStore();
            if (store != null && !store.isConnected()) {
                store.connect(EmailAccount.EMAIL_ADDRESS, EmailAccount.EMAIL_PASSWORD);
            }
        } else {
            throw new MessagingException("Unable to open a null folder");
        }

        if (folder.exists() && !folder.isOpen() && (folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
            System.out.println("open folder " + folder.getFullName());
            folder.open(Folder.READ_ONLY);
            if (!folder.isOpen()) {
                throw new MessagingException("Unable to open folder " + folder.getFullName());
            }
        }
    }

    private String parseAddresses(Address[] address) {

        String listOfAddress = "";
        if ((address == null) || (address.length < 1))
            return null;
        if (!(address[0] instanceof InternetAddress))
            return null;

        for (int i = 0; i < address.length; i++) {
            InternetAddress internetAddress =
                    (InternetAddress) address[0];
            listOfAddress += internetAddress.getAddress() + ",";
        }
        return listOfAddress;
    }
}
