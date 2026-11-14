package br.edu.fiec.helptec.features.usuario.service;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.CreateUsuarioRequestDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import br.edu.fiec.helptec.features.usuario.service.impl.UsuarioServiceImpl;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private static CreateUsuarioRequestDTO request(String nome, String email, String senha) {
        CreateUsuarioRequestDTO request = new CreateUsuarioRequestDTO();
        request.setNome(nome);
        request.setEmail(email);
        request.setSenha(senha);
        return request;
    }

    @Test
    @DisplayName("todo usuário novo entra com o papel USER")
    void deveCriarUsuarioComPapelUser() {
        //mocks
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        //impl
        UsuarioResponseDTO resultado = usuarioService.criar(request("Ana", "ana@fiec.edu.br", "123456"));

        //asserts
        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(UserRole.USER, captor.getValue().getRole());
        assertEquals("ana@fiec.edu.br", resultado.getEmail());
        assertEquals(UserRole.USER, resultado.getTipoPermissao());
    }

    @Test
    @DisplayName("deve devolver a página de usuários com os totais")
    void deveListarPaginado() {
        //mocks
        List<UsuarioEntity> usuarios = List.of(Fabrica.usuario(UserRole.USER), Fabrica.usuario(UserRole.SUPORTE));
        when(usuarioRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(usuarios, PageRequest.of(0, 2), 3));

        //impl
        PageResponseDTO<UsuarioResponseDTO> resultado = usuarioService.listarPaginado(new PageRequestDTO(0, 2));

        //asserts
        assertEquals(2, resultado.getItens().size());
        assertEquals(3, resultado.getTotalItens());
        assertEquals(0, resultado.getPaginaAtual());
        assertEquals(2, resultado.getTotalPaginas());
    }

    @Test
    @DisplayName("ao atualizar com senha em branco, mantém a senha atual")
    void deveManterSenhaQuandoNaoInformada() {
        //mocks
        UsuarioEntity existente = Fabrica.usuario(UserRole.USER);
        when(usuarioRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        //impl
        UsuarioResponseDTO resultado = usuarioService.atualizar(existente.getId(), request("Ana Maria", "ana@fiec.edu.br", " "));

        //asserts
        assertEquals("Ana Maria", resultado.getNome());
        assertEquals("senha-criptografada", existente.getPassword());
    }

    @Test
    @DisplayName("deve falhar ao buscar usuário inexistente")
    void naoDeveBuscarUsuarioInexistente() {
        //mocks
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        //impl
        RuntimeException erro = assertThrows(RuntimeException.class, () -> usuarioService.buscarPorId(id));

        //asserts
        assertEquals("Usuário não encontrado com o ID: " + id, erro.getMessage());
    }

    @Test
    @DisplayName("não deve excluir usuário inexistente")
    void naoDeveDeletarUsuarioInexistente() {
        //mocks
        UUID id = UUID.randomUUID();
        when(usuarioRepository.existsById(id)).thenReturn(false);

        //impl + asserts
        assertThrows(RuntimeException.class, () -> usuarioService.deletar(id));
        verify(usuarioRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("login: e-mail desconhecido gera UsernameNotFoundException")
    void deveFalharLoginComEmailDesconhecido() {
        //mocks
        when(usuarioRepository.findByEmail("x@fiec.edu.br")).thenReturn(Optional.empty());

        //impl + asserts
        assertThrows(UsernameNotFoundException.class, () -> usuarioService.loadUserByUsername("x@fiec.edu.br"));
    }
}
