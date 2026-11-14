package br.edu.fiec.helptec.config.mongo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Geração dos _id antes de salvar no MongoDB (substitui o AUTO_INCREMENT do MySQL). */
@ExtendWith(MockitoExtension.class)
@DisplayName("GeneratedIdCallback")
class GeneratedIdCallbackTest {

    // Documentos de exemplo, só para o teste
    static class DocumentoComLong {
        @GeneratedId
        Long id;
    }

    static class DocumentoComUuid {
        @GeneratedId
        UUID id;
    }

    static class DocumentoComString {
        @GeneratedId
        String id;
    }

    @Mock
    private ObjectProvider<SequenceGeneratorService> provider;

    @Mock
    private SequenceGeneratorService sequenceGenerator;

    private GeneratedIdCallback callback;

    @BeforeEach
    void init() {
        lenient().when(provider.getObject()).thenReturn(sequenceGenerator);
        callback = new GeneratedIdCallback(provider);
    }

    @Test
    @DisplayName("Long novo recebe o próximo número da sequência da collection")
    void deveGerarIdNumerico() {
        //mocks
        when(sequenceGenerator.proximoValor("chamados")).thenReturn(42L);
        DocumentoComLong doc = new DocumentoComLong();

        //impl
        callback.onBeforeConvert(doc, "chamados");

        //asserts
        assertEquals(Long.valueOf(42L), doc.id);
    }

    @Test
    @DisplayName("UUID novo recebe um UUID aleatório")
    void deveGerarUuid() {
        //mocks
        DocumentoComUuid doc = new DocumentoComUuid();

        //impl
        callback.onBeforeConvert(doc, "usuarios");

        //asserts
        assertNotNull(doc.id);
        verify(sequenceGenerator, never()).proximoValor(anyString());
    }

    @Test
    @DisplayName("documento que já tem id (atualização) não é alterado")
    void naoDeveTrocarIdExistente() {
        //mocks
        DocumentoComLong doc = new DocumentoComLong();
        doc.id = 7L;

        //impl
        callback.onBeforeConvert(doc, "chamados");

        //asserts
        assertEquals(Long.valueOf(7L), doc.id);
        verify(sequenceGenerator, never()).proximoValor(anyString());
    }

    @Test
    @DisplayName("tipo de id não suportado gera erro claro")
    void deveRecusarTipoNaoSuportado() {
        //impl + asserts
        assertThrows(IllegalStateException.class,
                () -> callback.onBeforeConvert(new DocumentoComString(), "x"));
    }
}
