package br.edu.fiec.helptec.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseServiceAccountJson;

    @Value("${app.firebase.config-file:/tmp/serviceAccountKey.json}")
    private String configFile;

    @PostConstruct
    public void initialize() {
        try {
            Path configPath = Paths.get(configFile);

            if (firebaseServiceAccountJson != null
                    && !firebaseServiceAccountJson.isBlank()) {

                Path parent = configPath.getParent();

                if (parent != null) {
                    Files.createDirectories(parent);
                }

                Files.writeString(
                        configPath,
                        firebaseServiceAccountJson,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            }

            if (!Files.exists(configPath)) {
                throw new IOException(
                        "Arquivo de configuração do Firebase não encontrado: "
                                + configPath
                );
            }

            try (InputStream serviceAccount =
                         new FileInputStream(configPath.toFile())) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(
                                GoogleCredentials.fromStream(serviceAccount)
                        )
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao inicializar o Firebase",
                    e
            );
        }
    }
}
