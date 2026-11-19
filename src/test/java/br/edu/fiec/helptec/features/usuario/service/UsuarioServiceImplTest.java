package br.edu.fiec.helptec.features.usuario.service;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.CreateUsuarioRequestDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioCsvResultadoDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UserStatus;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("a senha do usuário novo é gravada criptografada")
    void deveCriptografarSenhaAoCriar() {
        //mocks
        when(passwordEncoder.encode("123456")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        //impl
        usuarioService.criar(request("Ana", "ana@fiec.edu.br", "123456"));

        //asserts
        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hash-bcrypt", captor.getValue().getPassword());
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
        assertEquals(UserStatus.NOT_CONFIRMED, captor.getValue().getStatus());
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

    // ---------------------------------------------------------
    // Importação por CSV
    // ---------------------------------------------------------

    private static MockMultipartFile csv(String conteudo) {
        return new MockMultipartFile("file", "usuarios.csv", "text/csv", conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private void salvarDevolveOsMesmos() {
        when(usuarioRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("CSV: cria os usuários válidos como NOT_CONFIRMED, com senha criptografada e perfil")
    void deveImportarCsv() throws Exception {
        //mocks
        salvarDevolveOsMesmos();
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        String conteudo = """
                nome,email,senha,perfil,area
                Ana,ANA@fiec.edu.br,123456,SUPORTE,Informática
                Bruno,bruno@fiec.edu.br,,,
                """;

        //impl
        UsuarioCsvResultadoDTO resultado = usuarioService.importarCsv(csv(conteudo));

        //asserts
        assertEquals(2, resultado.totalLinhas());
        assertEquals(2, resultado.criados().size());
        assertTrue(resultado.erros().isEmpty());
        ArgumentCaptor<List<UsuarioEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(usuarioRepository).saveAll(captor.capture());
        UsuarioEntity ana = captor.getValue().get(0);
        assertEquals("ana@fiec.edu.br", ana.getEmail());
        assertEquals(UserRole.SUPORTE, ana.getRole());
        assertEquals("Informática", ana.getArea());
        assertEquals("hash", ana.getPassword());
        assertEquals(UserStatus.NOT_CONFIRMED, ana.getStatus());
        assertEquals(UserRole.USER, captor.getValue().get(1).getRole());
    }

    @Test
    @DisplayName("CSV: aceita o ';' do Excel em português e o BOM do UTF-8")
    void deveAceitarPontoEVirgulaEBom() throws Exception {
        //mocks
        salvarDevolveOsMesmos();
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        //impl
        UsuarioCsvResultadoDTO resultado = usuarioService.importarCsv(
                csv("\uFEFFnome;email;senha;perfil;area\nAna;ana@fiec.edu.br;123;USER;TI\n"));

        //asserts
        assertEquals(1, resultado.criados().size());
    }

    @Test
    @DisplayName("CSV: linhas inválidas voltam em erros com o número da linha e não são criadas")
    void deveRelatarLinhasInvalidas() throws Exception {
        //mocks
        salvarDevolveOsMesmos();
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("ja@fiec.edu.br"))
                .thenReturn(Optional.of(Fabrica.usuario(UserRole.USER)));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        String conteudo = """
                nome,email,senha,perfil,area
                Ok,ok@fiec.edu.br,1,USER,
                ,semnome@fiec.edu.br,1,USER,
                Invalido,nao-e-email,1,USER,
                Repetido,ok@fiec.edu.br,1,USER,
                Existente,ja@fiec.edu.br,1,USER,
                Perfil,perfil@fiec.edu.br,1,DIRETOR,
                Chefe,chefe@fiec.edu.br,1,SUPER_ADMIN,
                """;

        //impl
        UsuarioCsvResultadoDTO resultado = usuarioService.importarCsv(csv(conteudo));

        //asserts
        assertEquals(7, resultado.totalLinhas());
        assertEquals(1, resultado.criados().size());
        assertEquals(List.of(3, 4, 5, 6, 7, 8),
                resultado.erros().stream().map(UsuarioCsvResultadoDTO.ErroLinha::linha).toList());
        assertEquals("e-mail repetido no arquivo", resultado.erros().get(2).motivo());
        assertEquals("e-mail já cadastrado", resultado.erros().get(3).motivo());
        assertTrue(resultado.erros().get(5).motivo().contains("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("CSV: arquivo vazio gera 400")
    void deveRecusarArquivoVazio() {
        //impl + asserts
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> usuarioService.importarCsv(csv("")));
        assertEquals(400, erro.getStatusCode().value());
        verify(usuarioRepository, never()).saveAll(anyList());
    }
}
