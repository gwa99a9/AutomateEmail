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

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Date;
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
                data.put("subject", message.getSubject());
                data.put("body", getTextFromMessage(message));
                data.put("from", message.getFrom()[0].toString());
                data.put("sender", message.getHeader("Message-ID")[0]);
                data.put("priority", "1");
                data.put("status", "1");
                data.put("ticketStarted", new Date());

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


    private static String getTextFromMessage(Message message) throws IOException, MessagingException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws IOException, MessagingException {

        int count = mimeMultipart.getCount();
        if (count == 0)
            throw new MessagingException("Multipart with no body parts not supported.");
        boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
        if (multipartAlt)
            // alternatives appear in an order of increasing
            // faithfulness to the original content. Customize as req'd.
            return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));
        String result = "";
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            result += getTextFromBodyPart(bodyPart);
        }
        return result;
    }

    private static String getTextFromBodyPart(
            BodyPart bodyPart) throws IOException, MessagingException {

        String result = "";
        if (bodyPart.isMimeType("text/plain")) {
            result = (String) bodyPart.getContent();
        } else if (bodyPart.isMimeType("text/html")) {
            String html = (String) bodyPart.getContent();
            result = org.jsoup.Jsoup.parse(html).text();
        } else if (bodyPart.getContent() instanceof MimeMultipart) {
            result = getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
        }
        return result;
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
