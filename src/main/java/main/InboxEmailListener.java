package main;

import auto.TicketFirebaseHelper;
import categorizer.EmailCheck.MailCategorizer;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import email.config.EmailConfig;
import email.config.property.EmailProperty;
import email.config.property.PropertyFactory;
import email.listener.EmailListener;
import email.listener.EmailListenerUtil;
import email.listener.IdleThread;
import firebase.FirebaseUtil;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class InboxEmailListener {

    public static void main(String[] args) {
        // TODO: starts after inboxcounter and keeps listening
        MailCategorizer mailCategorizer = new MailCategorizer();
        EmailListener emailListener = new EmailListener(new EmailConfig(PropertyFactory.getConfig(EmailProperty.IMAP)).getSession());
        try {
            emailListener.init();

            emailListener.getInbox().addMessageCountListener(new MessageCountAdapter() {

                @Override
                public void messagesAdded(MessageCountEvent event) {
                    Message[] messages = event.getMessages();
                    for (Message message : messages) {
                        try {
                            FirebaseUtil.initializeApp();
                            Firestore db = FirestoreClient.getFirestore();

                            String messageBody = TicketFirebaseHelper.getTextFromMessage(message);
                            String category = mailCategorizer.getCategory(messageBody, message.getSubject());

                            String priority = TicketFirebaseHelper.getEmailPriority(db, messageBody, mailCategorizer);
                            // TODO: send them to categorizer
                            TicketFirebaseHelper.addEmailToFireBase(message, category, priority);

                            // updateOldMessageCount(oldMessageCount + i);
                            // TODO: oldMessageCount = oldMessageCount+i ; // update the oldCount after adding email to db
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
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


