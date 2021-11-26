package categorizer.EmailCheck;

import java.io.*;
import java.util.ArrayList;

public class MailCategorizer implements Serializable {

    private static final String REQUEST = "request";
    private static final String ISSUE = "issue";
    private static final String UNKNOWN = "uncategorized";

    private static final String REQUEST_FILE_PATH = "src/TextFiles/requests.txt";
    private static final String ISSUE_FILE_PATH = "src/TextFiles/issues.txt";
    private static final String PRIORITY_FILE_PATH = "src/TextFiles/priority.txt";
    private static final String DATA_FILE_PATH = System.getProperty("user.dir") + "/category.ser";

    private ArrayList<String> REQUEST_LIST = new ArrayList();
    private ArrayList<String> ISSUE_LIST = new ArrayList();
    private ArrayList<String> PRIORITY_LIST = new ArrayList();

    public MailCategorizer() {
        restore();
        if (REQUEST_LIST.isEmpty() || ISSUE_LIST.isEmpty()) {
            createRequestArray();
            createIssueArray();
            createPriorityArray();
            save();
        }
    }


    public String getPriority(String body) {
        int highPriority = 0;
        int midPriority = 0;
        if (!body.trim().isEmpty()) {
            for (String priority : PRIORITY_LIST) {
                String[] priorityString = priority.split(",");
                if (body.toLowerCase().contains(priorityString[0].toLowerCase().trim())) {
                    switch (priorityString[1].trim().toLowerCase()) {
                        case "1":
                            highPriority++;
                            break;
                        case "2":
                            midPriority++;
                            break;
                    }
                }
            }
            if (highPriority == 0 && midPriority == 0) {
                return "3";
            }
            return highPriority > midPriority ? "1" : "2";
        }
        return "3";
    }

    private void createPriorityArray() {
        try {
            File file = new File(PRIORITY_FILE_PATH);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                PRIORITY_LIST.add(line.trim().toLowerCase());
            }
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRequestArray() {
        try {
            File file = new File(REQUEST_FILE_PATH);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                REQUEST_LIST.add(line.trim().toLowerCase());
            }
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createIssueArray() {
        try {
            File file = new File(ISSUE_FILE_PATH);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                ISSUE_LIST.add(line.trim().toLowerCase());
            }
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restore() {
        MailCategorizer categorizer = null;
        try {
            FileInputStream fileIn = new FileInputStream(DATA_FILE_PATH);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            categorizer = (MailCategorizer) in.readObject();
            in.close();
            fileIn.close();
            this.REQUEST_LIST = categorizer.REQUEST_LIST;
            this.ISSUE_LIST = categorizer.ISSUE_LIST;
            this.PRIORITY_LIST = categorizer.PRIORITY_LIST;
        } catch (IOException | ClassNotFoundException ignored) {
        }
    }

    private void save() {
        try {
            File file = new File(DATA_FILE_PATH);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public String getCategory(String body, String subject) {
        if (!subject.trim().isEmpty()) {
            String[] textList = subject.split(" ");
            return checkCategory(textList);
        }
        if (!body.trim().isEmpty()) {
            String[] textList = body.split(" ");
            return checkCategory(textList);
        } else {
            return UNKNOWN;
        }
    }

    private String checkCategory(String[] textList) {
        int requestCount = 0;
        int issieCount = 0;
        for (String text : textList) {
            for (String request : REQUEST_LIST) {
                if (text.trim().toLowerCase().startsWith(request.trim())) {
                    requestCount++;
                    break;
                }
            }
            for (String issue : ISSUE_LIST) {
                if (text.trim().toLowerCase().startsWith(issue.trim())) {
                    issieCount++;
                    break;
                }
            }
        }
        if (requestCount == issieCount) {
            return UNKNOWN;
        }
        return requestCount > issieCount ? REQUEST : ISSUE;
    }

    public static void main(String[] args) {
        MailCategorizer mailCategorizer = new MailCategorizer();
        System.out.println(mailCategorizer.getCategory("This is a test email withs a request and issue issue", ""));
        System.out.println(mailCategorizer.getPriority("this is a urgent message."));
    }

}