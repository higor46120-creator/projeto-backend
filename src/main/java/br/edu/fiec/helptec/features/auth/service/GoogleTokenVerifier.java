package br.edu.fiec.helptec.features.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    private final GoogleIdTokenVerifier verifier;
    private final List<String> expectedAudiences;

    // Audiences aceitos:
    //  - client id WEB do login web (propriedade já existente, não muda)
    //  - client ids extras (ex.: Web Client ID usado pelo app mobile), separados por vírgula
    public GoogleTokenVerifier(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String webClientId,
            @Value("${app.google.extra-client-ids:}") String extraClientIds) {

        List<String> audiences = new ArrayList<>();
        adicionar(audiences, webClientId);
        if (extraClientIds != null) {
            for (String id : extraClientIds.split(",")) {
                adicionar(audiences, id);
            }
        }
        this.expectedAudiences = List.copyOf(audiences);

        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(this.expectedAudiences)
                .build();
    }

    private static void adicionar(List<String> lista, String id) {
        if (id != null && !id.isBlank()) {
            lista.add(id.trim());
        }
    }

    /**
     * Valida assinatura, emissor (Google), expiração e audience.
     * Lança 401 se o token for inválido.
     */
    public GoogleIdToken.Payload verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                logarMotivoDaRejeicao(idToken);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "ID token do Google inválido ou expirado");
            }
            return token.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Falha ao validar ID token do Google (rede/certificados?)", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Não foi possível validar o ID token do Google", e);
        }
    }

    /**
     * Diagnóstico: compara o audience esperado pelo backend com o do token recebido.
     * Não loga o token em si, apenas campos não sensíveis.
     */
    private void logarMotivoDaRejeicao(String idToken) {
        try {
            GoogleIdToken.Payload p = GoogleIdToken.parse(GsonFactory.getDefaultInstance(), idToken).getPayload();
            log.warn("ID token do Google rejeitado. aud do token={} | aud esperados={} | iss={} | exp={} | agora={}",
                    p.getAudience(), expectedAudiences, p.getIssuer(),
                    p.getExpirationTimeSeconds(), System.currentTimeMillis() / 1000);
        } catch (Exception e) {
            log.warn("ID token do Google rejeitado e não foi possível decodificá-lo: {}", e.getMessage());
        }
    }
}