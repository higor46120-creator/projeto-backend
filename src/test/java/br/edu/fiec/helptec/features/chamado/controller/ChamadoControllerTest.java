package br.edu.fiec.helptec.features.chamado.controller;

import br.edu.fiec.helptec.config.UserContext;
import br.edu.fiec.helptec.features.chamado.StatusChamado;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoDTO;
import br.edu.fiec.helptec.features.chamado.service.ChamadoAnexoService;
import br.edu.fiec.helptec.features.chamado.service.ChamadoService;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Camada HTTP dos chamados. As permissões por papel (@PreAuthorize) não são
 * avaliadas no MockMvc standalone: aqui o foco é rota, corpo e status.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChamadoController")
class ChamadoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChamadoService chamadoService;

    @Mock
    private ChamadoAnexoService chamadoAnexoService;

    @InjectMocks
    private ChamadoController chamadoController;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(chamadoController).build();
    }

    @AfterEach
    void limparUsuarioLogado() {
        UserContext.clear();
    }

    private static ChamadoDTO dto(Long id, StatusChamado status) {
        ChamadoDTO dto = new ChamadoDTO();
        dto.setIdChamado(id);
        dto.setStatus(status);
        dto.setDescricao("PC não liga");
        return dto;
    }

    @Test
    @DisplayName("GET /api/chamados/{id} devolve 200 com o chamado")
    void deveBuscarChamado() throws Exception {
        //mocks
        when(chamadoService.buscarPorId(1L)).thenReturn(dto(1L, StatusChamado.APROVADO));

        //impl + asserts
        mockMvc.perform(get("/api/chamados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idChamado").value(1))
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    @DisplayName("POST sem usuário logado devolve 401 e não cria nada")
    void naoDeveCriarSemUsuarioLogado() throws Exception {
        //impl + asserts
        mockMvc.perform(post("/api/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"PC não liga\"}"))
                .andExpect(status().isUnauthorized());
        verify(chamadoService, never()).criar(any());
    }

    @Test
    @DisplayName("POST usa o usuário logado como solicitante, ignorando o corpo")
    void deveCriarComUsuarioLogado() throws Exception {
        //mocks
        UsuarioEntity logado = Fabrica.usuario(UserRole.USER);
        UserContext.setUser(logado);
        when(chamadoService.criar(any(ChamadoDTO.class))).thenReturn(dto(5L, StatusChamado.AGUARDANDO_APROVACAO));
        String corpo = "{\"descricao\":\"PC não liga\",\"idUsuario\":\"" + UUID.randomUUID() + "\"}";

        //impl
        mockMvc.perform(post("/api/chamados").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idChamado").value(5));

        //asserts
        ArgumentCaptor<ChamadoDTO> captor = ArgumentCaptor.forClass(ChamadoDTO.class);
        verify(chamadoService).criar(captor.capture());
        assertEquals(logado.getId(), captor.getValue().getIdUsuario());
    }

    @Test
    @DisplayName("PATCH /aprovar usa o id do aprovador logado")
    void deveAprovarComUsuarioLogado() throws Exception {
        //mocks
        UsuarioEntity aprovador = Fabrica.usuario(UserRole.APROVADOR);
        UserContext.setUser(aprovador);
        when(chamadoService.aprovar(1L, aprovador.getId())).thenReturn(dto(1L, StatusChamado.APROVADO));

        //impl + asserts
        mockMvc.perform(patch("/api/chamados/1/aprovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    @DisplayName("PATCH /triagem repassa o suporte escolhido")
    void deveTriar() throws Exception {
        //mocks
        UUID idSuporte = UUID.randomUUID();
        when(chamadoService.triar(1L, idSuporte)).thenReturn(dto(1L, StatusChamado.EM_ATENDIMENTO));

        //impl + asserts
        mockMvc.perform(patch("/api/chamados/1/triagem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSuporte\":\"" + idSuporte + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
    }

    @Test
    @DisplayName("PATCH /resolver recebe a devolutiva em texto puro")
    void deveResolver() throws Exception {
        //mocks
        when(chamadoService.resolver(1L, "Fonte trocada")).thenReturn(dto(1L, StatusChamado.RESOLVIDO));

        //impl + asserts
        mockMvc.perform(patch("/api/chamados/1/resolver")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Fonte trocada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDO"));
    }

    @Test
    @DisplayName("DELETE /api/chamados/{id} devolve 204")
    void deveDeletar() throws Exception {
        //impl + asserts
        mockMvc.perform(delete("/api/chamados/1"))
                .andExpect(status().isNoContent());
        verify(chamadoService).deletar(1L);
    }
}
