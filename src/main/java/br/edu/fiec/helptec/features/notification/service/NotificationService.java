package br.edu.fiec.helptec.features.notification.service;

import br.edu.fiec.helptec.features.notification.model.dto.NotificationDTO;

public interface NotificationService {

    /**
     * Envia uma notificação push para o dispositivo dono do token.
     *
     * @return o ID da mensagem no FCM, ou null se o token for inválido/expirado
     */
    String notifyUser(String token, NotificationDTO notification);
}
