package main;

import auto.TickerFirebaseHelper;
import categorizer.EmailCheck.MailCategorizer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import email.config.EmailConfig;
import email.config.property.EmailProperty;
import email.config.property.PropertyFactory;
import email.listener.EmailListener;
import email.listener.EmailListenerUtil;
import firebase.FirebaseUtil;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


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

            int oldMessageCount = (int) querySnapshot.get("lastReadMailCount", Integer.class); // TODO: implement db

            if (oldMessageCount < newMessageCount) {
                final Message[] messages = emailListener.getInbox().getMessages(oldMessageCount, newMessageCount); // get only new messages
                for (int i = 0; i < messages.length; i++) {

                    String messageBody = TickerFirebaseHelper.getTextFromMessage(messages[i]);
                    String category = mailCategorizer.getCategory(messageBody, messages[i].getSubject());
                    String priority = mailCategorizer.getPriority(messageBody);
                    if (!priority.equals("1")) {
                        ApiFuture<QuerySnapshot> tickets = db.collection("tickets").get();
                        List<QueryDocumentSnapshot> documents = tickets.get().getDocuments();
                        for (QueryDocumentSnapshot doc : documents) {
                            if ((doc.getString("body")).contains(messageBody)) {
                                priority = "1";
                                break;
                            }
                        }
                    }
                    // TODO: send them to categorizer
                    addEmailToFireBase(messages[i], category, priority);

                    // updateOldMessageCount(oldMessageCount + i);
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

    private static void addEmailToFireBase(Message message, String category, String priority) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            boolean hasDuplicate = TickerFirebaseHelper.hasDuplicate(message.getHeader("Message-ID")[0]);
            if (!hasDuplicate) {
                Map<String, Object> data = new HashMap<>();
                data.put("subject", message.getSubject());
                data.put("body", TickerFirebaseHelper.getTextFromMessage(message));
                data.put("from", message.getFrom()[0].toString());
                data.put("sender", message.getHeader("Message-ID")[0]);
                data.put("category", category);
                data.put("priority", priority);
                data.put("status", "1");
                data.put("ticketStarted", new Date());

                ApiFuture<WriteResult> future = db.collection("tickets").document().set(data);

                System.out.println("Update time : " + future.get().getUpdateTime());
            }
        } catch (MessagingException | IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
