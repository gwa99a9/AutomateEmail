package main;

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
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class InboxCountTester {
    public static void main(String[] args) {
        // TODO: runs on startup
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

                    // TODO: send them to categorizer
                    addEmailToFireBase(messages[i]);

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

    private static void addEmailToFireBase(Message message) {
        try {
            Firestore db = FirestoreClient.getFirestore();


            boolean hasDuplicate = hasDuplicate(message.getHeader("Message-ID")[0]);
            if (!hasDuplicate) {
                Map<String, Object> data = new HashMap<>();
                data.put("subject", message.getSubject().toString());
                data.put("body", getText(message).toString());
                data.put("from", message.getFrom()[0].toString());
                data.put("sender", message.getHeader("Message-ID")[0].toString());
                data.put("priority", "1");
                data.put("status", "1");
                ApiFuture<WriteResult> future = db.collection("tickets").document().set(data);

                System.out.println("Update time : " + future.get().getUpdateTime());
            }
        } catch (MessagingException | IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static boolean hasDuplicate(String s) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> tickets = db.collection("tickets").get();
        List<QueryDocumentSnapshot> documents = tickets.get().getDocuments();
        for (QueryDocumentSnapshot doc : documents) {
            if (s.equals(doc.getString("sender"))) {
                return true;
            }
        }
        return false;
    }


    private static boolean textIsHtml = false;

    /**
     * Return the primary text content of the message.
     */
    private static String getText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

    private static void updateOldMessageCount(int i) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentReference docRef = db.collection("settings").document("statics");
            Map<String, Object> data = new HashMap<>();
            data.put("lastReadMailCount", i);
            ApiFuture<WriteResult> result = docRef.set(data);
            System.out.println("Update time : " + result.get().getUpdateTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
