package br.edu.fiec.helptec.features.salas.controller;

import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.salas.model.dto.SalasDTO;
import br.edu.fiec.helptec.features.salas.service.SalasService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa só a camada HTTP (rotas, status e JSON), com o serviço simulado.
 * MockMvc "standalone": sem subir o Spring inteiro nem o banco, como na aula.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SalasController")
class SalasControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SalasService salasService;

    @InjectMocks
    private SalasController salasController;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(salasController).build();
    }

    @Test
    @DisplayName("GET /api/salas/{id} devolve 200 com a sala")
    void deveBuscarSalaPorId() throws Exception {
        //mocks
        when(salasService.buscarPorId(1L)).thenReturn(new SalasDTO(1L, "Sala 101"));

        //impl + asserts
        mockMvc.perform(get("/api/salas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSala").value(1))
                .andExpect(jsonPath("$.nome").value("Sala 101"));
        verify(salasService).buscarPorId(1L);
    }

    @Test
    @DisplayName("GET /api/salas devolve a página")
    void deveListarSalas() throws Exception {
        //mocks
        when(salasService.buscarComFiltro(any(), any()))
                .thenReturn(new PageResponseDTO<>(List.of(new SalasDTO(1L, "Biblioteca")), 1, 0, 1));

        //impl + asserts
        mockMvc.perform(get("/api/salas").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItens").value(1))
                .andExpect(jsonPath("$.itens[0].nome").value("Biblioteca"));
    }

    @Test
    @DisplayName("POST /api/salas devolve 201 com a sala criada")
    void deveCriarSala() throws Exception {
        //mocks
        when(salasService.criar(any(SalasDTO.class))).thenReturn(new SalasDTO(10L, "Laboratório 1"));

        //impl + asserts
        mockMvc.perform(post("/api/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Laboratório 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSala").value(10));
    }

    @Test
    @DisplayName("DELETE /api/salas/{id} devolve 204")
    void deveDeletarSala() throws Exception {
        //impl + asserts
        mockMvc.perform(delete("/api/salas/1"))
                .andExpect(status().isNoContent());
        verify(salasService).deletar(1L);
    }

    @Test
    @DisplayName("erro do serviço não é engolido pelo controller")
    void devePropagarErroDeSalaInexistente() {
        //mocks
        when(salasService.buscarPorId(99L)).thenThrow(new RuntimeException("Sala não encontrada"));

        //impl
        ServletException erro = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/api/salas/99")));

        //asserts
        assertEquals("Sala não encontrada", erro.getCause().getMessage());
    }
}
