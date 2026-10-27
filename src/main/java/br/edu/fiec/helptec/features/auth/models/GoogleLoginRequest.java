package br.edu.fiec.helptec.features.auth.models;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "O idToken é obrigatório")
        String idToken,

        // Opcional: o app já aproveita para registrar o token de push do aparelho
        String fcmToken
) {}