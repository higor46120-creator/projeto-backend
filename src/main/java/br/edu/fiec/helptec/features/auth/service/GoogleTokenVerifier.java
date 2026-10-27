package br.edu.fiec.helptec.features.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    // O "audience" esperado é o client id WEB (o mesmo GOOGLE_CLIENT_ID do login web)
    public GoogleTokenVerifier(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Valida assinatura, emissor (Google), expiração e audience.
     * Lança 401 se o token for inválido.
     */
    public GoogleIdToken.Payload verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "ID token do Google inválido ou expirado");
            }
            return token.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Não foi possível validar o ID token do Google", e);
        }
    }
}