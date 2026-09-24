package br.edu.fiec.helptec.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.config-file:serviceAccountKey.json}")
    private String configFile;

    @PostConstruct
    public void initialize() {
        try {
            String userHome = System.getProperty("user.home");
            FileInputStream serviceAccount = new FileInputStream(
                Paths.get(userHome, "serviceAccountKey.json").toFile()
            );

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao inicializar o Firebase", e);
        }
    }
}
