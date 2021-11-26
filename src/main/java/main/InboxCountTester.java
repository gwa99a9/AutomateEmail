package main;

import auto.TicketFirebaseHelper;
import categorizer.EmailCheck.MailCategorizer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import email.config.EmailConfig;
import email.config.property.EmailProperty;
import email.config.property.PropertyFactory;
import email.listener.EmailListener;
import email.listener.EmailListenerUtil;
import firebase.FirebaseUtil;

import javax.mail.Message;


public class InboxCountTester {
    public static void main(String[] args) {
        // TODO: runs on startup
        MailCategorizer mailCategorizer = new MailCategorizer();
        EmailListener emailListener = new EmailListener(new EmailConfig(PropertyFactory.getConfig(EmailProperty.IMAP)).getSession());
        try {
            emailListener.init();

            int newMessageCount = emailListener.getInbox().getMessageCount();
            FirebaseUtil.initializeApp();
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<DocumentSnapshot> query = db.collection("settings").document("statics").get();
            DocumentSnapshot querySnapshot = query.get();

            int oldMessageCount = querySnapshot.get("lastReadMailCount", Integer.class); // TODO: implement db

            if (oldMessageCount < newMessageCount) {
                final Message[] messages = emailListener.getInbox().getMessages(oldMessageCount, newMessageCount);
                for (int i = 0; i < messages.length; i++) {

                    String messageBody = TicketFirebaseHelper.getTextFromMessage(messages[i]);
                    String category = mailCategorizer.getCategory(messageBody, messages[i].getSubject());
                    // TODO: send them to categorizer
                    String priority = TicketFirebaseHelper.getEmailPriority(db, messageBody, mailCategorizer);
                    TicketFirebaseHelper.addEmailToFireBase(messages[i], category, priority);

                    TicketFirebaseHelper.updateOldMessageCount(oldMessageCount + i);
                    // TODO: oldMessageCount = oldMessageCount+i ; // update the oldCount after adding email to db
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            EmailListenerUtil.closeFolder(emailListener.getInbox());
            EmailListenerUtil.closeStore(emailListener.getStore());
        }
    }

}
