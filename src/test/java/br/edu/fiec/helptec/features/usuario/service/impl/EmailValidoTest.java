package br.edu.fiec.helptec.features.usuario.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Validação de e-mail do CSV")
class EmailValidoTest {

    @ParameterizedTest
    @ValueSource(strings = {"ana@fiec.edu.br", "a.b-c@dominio.com", "x@y.io"})
    @DisplayName("aceita e-mails no formato usuario@dominio.ext")
    void aceita(String email) {
        //impl + asserts
        assertTrue(UsuarioServiceImpl.emailValido(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"email-invalido", "@fiec.edu.br", "ana@", "ana@fiec", "ana@.br", "ana@fiec.",
            "ana@@fiec.br", "ana@fi@ec.br", "ana souza@fiec.br"})
    @DisplayName("recusa e-mails mal formados")
    void recusa(String email) {
        //impl + asserts
        assertFalse(UsuarioServiceImpl.emailValido(email));
    }
}
