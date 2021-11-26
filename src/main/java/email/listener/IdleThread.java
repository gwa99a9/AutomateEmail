package email.listener;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.Folder;
import javax.mail.MessagingException;

public class IdleThread extends Thread {

    private final Folder folder;
    private volatile boolean running = true;

    public IdleThread(Folder folder) {
        this.folder = folder;
    }

    public synchronized void kill() {
        if (!running) {
            return;
        }
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                EmailListenerUtil.ensureOpen(folder);
                System.out.println("enter idle");
                ((IMAPFolder) folder).idle();
            } catch (MessagingException e) {
                // something went wrong
                // wait and try again
                e.printStackTrace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    // ignore
                }
            }

        }
    }
}