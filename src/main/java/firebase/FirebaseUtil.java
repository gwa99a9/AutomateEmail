package firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseUtil {
    private static FirebaseOptions options;

    static {
        init();
    }

    private static void init() {
        InputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream(System.getProperty("user.dir") + "//firebase.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(serviceAccount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .build();
    }

    public static void initializeApp() {
        FirebaseApp.initializeApp(options);
    }

}
