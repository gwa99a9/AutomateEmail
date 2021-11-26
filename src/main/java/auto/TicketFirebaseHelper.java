package auto;

import categorizer.EmailCheck.MailCategorizer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

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

public class TicketFirebaseHelper {

    public static boolean hasDuplicate(String s) throws ExecutionException, InterruptedException {
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

    public static String getTextFromMessage(Message message) throws IOException, MessagingException {
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

    public static String getEmailPriority(Firestore db, String messageBody, MailCategorizer mailCategorizer) throws InterruptedException, ExecutionException {
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
        return priority;
    }

    public static void addEmailToFireBase(Message message, String category, String priority) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            boolean hasDuplicate = TicketFirebaseHelper.hasDuplicate(message.getHeader("Message-ID")[0]);
            if (!hasDuplicate) {
                Map<String, Object> data = new HashMap<>();
                data.put("subject", message.getSubject());
                data.put("body", TicketFirebaseHelper.getTextFromMessage(message));
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