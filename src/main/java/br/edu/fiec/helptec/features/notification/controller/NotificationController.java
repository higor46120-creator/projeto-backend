package br.edu.fiec.helptec.features.notification.controller;

import br.edu.fiec.helptec.features.notification.model.dto.NotificationDTO;
import br.edu.fiec.helptec.features.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Controller APENAS para testes manuais do envio de push.
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Corpo da requisição: o token do dispositivo + a notificação
    public record NotificationTestRequest(String token, NotificationDTO notification) {}

    @PostMapping
    public ResponseEntity<Map<String, String>> enviar(@RequestBody NotificationTestRequest request) {
        String messageId = notificationService.notifyUser(request.token(), request.notification());

        if (messageId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token FCM vazio, inválido ou expirado"));
        }
        return ResponseEntity.ok(Map.of("messageId", messageId));
    }
}
