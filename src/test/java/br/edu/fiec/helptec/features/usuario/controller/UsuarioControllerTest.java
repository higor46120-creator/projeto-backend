package br.edu.fiec.helptec.features.usuario.controller;

import br.edu.fiec.helptec.config.UserContext;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioCsvResultadoDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UserStatus;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.service.UsuarioService;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Camada HTTP de usuários. O @PreAuthorize não roda no MockMvc standalone: é conferido por reflexão. */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController")
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
    }

    @AfterEach
    void limpar() {
        UserContext.clear();
    }

    @Test
    @DisplayName("POST /usuarios/csv é exclusivo do SUPER_ADMIN")
    void csvSomenteSuperAdmin() throws Exception {
        //impl
        PreAuthorize regra = UsuarioController.class
                .getMethod("criarViaCsv", MultipartFile.class)
                .getAnnotation(PreAuthorize.class);

        //asserts
        assertEquals("hasRole('SUPER_ADMIN')", regra.value());
    }

    @Test
    @DisplayName("POST /usuarios/csv devolve criados e erros")
    void deveImportarCsv() throws Exception {
        //mocks
        UsuarioResponseDTO criado = new UsuarioResponseDTO(UUID.randomUUID(), "Ana", "ana@fiec.edu.br", UserRole.USER);
        when(usuarioService.importarCsv(any())).thenReturn(new UsuarioCsvResultadoDTO(2, List.of(criado),
                List.of(new UsuarioCsvResultadoDTO.ErroLinha(3, "x", "e-mail inválido"))));
        MockMultipartFile arquivo = new MockMultipartFile("file", "usuarios.csv", "text/csv",
                "nome,email\nAna,ana@fiec.edu.br\nX,x\n".getBytes());

        //impl + asserts
        mockMvc.perform(multipart("/usuarios/csv").file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLinhas").value(2))
                .andExpect(jsonPath("$.criados[0].email").value("ana@fiec.edu.br"))
                .andExpect(jsonPath("$.erros[0].linha").value(3));
    }

    @Test
    @DisplayName("GET /usuarios/me traz o status do usuário logado")
    void meTrazStatus() throws Exception {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.USER);
        usuario.setStatus(UserStatus.NOT_CONFIRMED);
        UserContext.setUser(usuario);

        //impl + asserts
        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_CONFIRMED"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
