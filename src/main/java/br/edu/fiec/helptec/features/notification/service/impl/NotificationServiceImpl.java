package br.edu.fiec.helptec.features.notification.service.impl;

import br.edu.fiec.helptec.features.notification.model.dto.NotificationDTO;
import br.edu.fiec.helptec.features.notification.service.NotificationService;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    // Bean criado no FirebaseConfig
    private final FirebaseMessaging firebaseMessaging;

    public NotificationServiceImpl(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public String notifyUser(String token, NotificationDTO notification) {
        if (token == null || token.isBlank()) {
            log.warn("Notificação ignorada: token vazio");
            return null;
        }
        if (notification == null) {
            throw new IllegalArgumentException("A notificação não pode ser nula");
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getMessageBody())
                        .build())
                .build();

        try {
            String messageId = firebaseMessaging.send(message);
            log.info("Notificação enviada. messageId={}", messageId);
            return messageId;

        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();

            // Token que não existe mais (app desinstalado, token rotacionado) ou malformado
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                log.warn("Token FCM inválido ou expirado ({}).", code);
                return null;
            }

            throw new RuntimeException("Falha ao enviar notificação push", e);
        }
    }
}