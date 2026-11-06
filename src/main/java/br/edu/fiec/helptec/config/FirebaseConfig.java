package br.edu.fiec.helptec.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Inicializa o Firebase procurando a credencial nesta ordem:
 *  1. app.firebase.config-file      (FIREBASE_CONFIG_FILE)      -> caminho de um arquivo, ex.: Secret montado no Kubernetes
 *  2. app.firebase.credentials-json (FIREBASE_CREDENTIALS_JSON) -> conteúdo JSON direto na variável de ambiente
 *  3. serviceAccountKey.json no classpath (src/main/resources)  -> desenvolvimento local
 * Se nada for encontrado, o Firebase fica inativo (anexos e notificações não funcionam).
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase.config-file:}")
    private String configFile;

    @Value("${app.firebase.credentials-json:}")
    private String credentialsJson;

    // Bucket do Storage, ex.: meu-projeto.firebasestorage.app (ou meu-projeto.appspot.com)
    @Value("${app.firebase.storage-bucket:}")
    private String storageBucket;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream credencial = abrirCredencial()) {
            if (credencial == null) {
                log.warn("AVISO: credencial do Firebase não encontrada "
                        + "(FIREBASE_CONFIG_FILE, FIREBASE_CREDENTIALS_JSON ou serviceAccountKey.json). O Firebase ficará inativo.");
                return;
            }

            FirebaseOptions.Builder builder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credencial));

            if (storageBucket != null && !storageBucket.isBlank()) {
                builder.setStorageBucket(storageBucket.trim());
            } else {
                log.warn("AVISO: FIREBASE_STORAGE_BUCKET não definido. Upload de anexos vai falhar.");
            }

            FirebaseApp.initializeApp(builder.build());
            log.info("Firebase inicializado com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao inicializar o Firebase: {}", e.getMessage());
        }
    }

    private InputStream abrirCredencial() throws Exception {
        if (configFile != null && !configFile.isBlank()) {
            Path caminho = Path.of(configFile.trim());
            if (Files.isReadable(caminho)) {
                log.info("Firebase: usando credencial do arquivo {}", caminho);
                return Files.newInputStream(caminho);
            }
            log.warn("Firebase: arquivo {} não encontrado ou sem permissão de leitura.", caminho);
        }

        if (credentialsJson != null && !credentialsJson.isBlank()) {
            log.info("Firebase: usando credencial da variável FIREBASE_CREDENTIALS_JSON");
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }

        ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
        if (resource.exists()) {
            log.info("Firebase: usando serviceAccountKey.json do classpath");
            return resource.getInputStream();
        }

        return null;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (FirebaseApp.getApps().isEmpty()) {
            return null;
        }
        return FirebaseMessaging.getInstance();
    }
}
