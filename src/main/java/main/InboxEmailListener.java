package main;

import email.config.EmailConfig;
import email.config.property.EmailProperty;
import email.config.property.PropertyFactory;
import email.listener.EmailListener;
import email.listener.EmailListenerUtil;
import email.listener.IdleThread;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

public class InboxEmailListener {

    public static void main(String[] args) {
        // TODO: starts after inboxcounter and keeps listening
        EmailListener emailListener = new EmailListener(new EmailConfig(PropertyFactory.getConfig(EmailProperty.IMAP)).getSession());
        try {
            emailListener.init();

            emailListener.getInbox().addMessageCountListener(new MessageCountAdapter() {

                @Override
                public void messagesAdded(MessageCountEvent event) {
                    Message[] messages = event.getMessages();
                    for (Message message : messages) {
                        try {
                            System.out.println("Mail Subject:- " + message.getSubject());
                            // TODO: send them to categorizer
                            // TODO: update the oldMessagesCount in db after new message
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            IdleThread idleThread = new IdleThread(emailListener.getInbox());
            idleThread.setDaemon(false);
            idleThread.start();

            idleThread.join();
            // TODO: idleThread.kill(); // to terminate from another thread
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            EmailListenerUtil.closeFolder(emailListener.getInbox());
            EmailListenerUtil.closeStore(emailListener.getStore());
        }
    }

}


