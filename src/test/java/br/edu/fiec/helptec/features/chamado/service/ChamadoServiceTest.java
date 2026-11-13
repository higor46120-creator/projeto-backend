package br.edu.fiec.helptec.features.chamado.service;

import br.edu.fiec.helptec.features.chamado.Chamado;
import br.edu.fiec.helptec.features.chamado.StatusChamado;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoDTO;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoFilterDTO;
import br.edu.fiec.helptec.features.chamado.repository.ChamadoRepository;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regras do fluxo do chamado:
 * AGUARDANDO_APROVACAO -> APROVADO/REPROVADO -> (triagem) EM_ATENDIMENTO -> RESOLVIDO.
 * Os repositórios são simulados (Mockito): o teste não precisa de banco.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChamadoService")
class ChamadoServiceTest {

    private static final Long ID_CHAMADO = 1L;
    private static final UUID ID_SOLICITANTE = UUID.randomUUID();

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    /** Faz o save "devolver" o próprio objeto salvo, como o banco faria. */
    private void saveDevolveOMesmoObjeto() {
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Chamado chamadoSalvo() {
        ArgumentCaptor<Chamado> captor = ArgumentCaptor.forClass(Chamado.class);
        verify(chamadoRepository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("Abertura")
    class Abertura {

        @Test
        @DisplayName("deve abrir o chamado aguardando aprovação, com a área do solicitante")
        void deveCriarChamadoAguardandoAprovacao() {
            //mocks
            UsuarioEntity solicitante = Fabrica.usuario(ID_SOLICITANTE, UserRole.USER);
            when(usuarioRepository.findById(ID_SOLICITANTE)).thenReturn(Optional.of(solicitante));
            saveDevolveOMesmoObjeto();

            // Campos que o cliente NÃO pode definir: o serviço tem que ignorar
            ChamadoDTO dto = new ChamadoDTO();
            dto.setIdChamado(99L);
            dto.setIdUsuario(ID_SOLICITANTE);
            dto.setDescricao("Projetor sem imagem");
            dto.setPrioridade("MEDIA");
            dto.setStatus(StatusChamado.RESOLVIDO);
            dto.setIdSuporte(UUID.randomUUID());
            dto.setResolucao("já resolvido");
            dto.setIdSala(3L);

            //impl
            ChamadoDTO resultado = chamadoService.criar(dto);

            //asserts
            Chamado salvo = chamadoSalvo();
            assertNull(salvo.getIdChamado(), "o id é gerado pelo banco");
            assertEquals(StatusChamado.AGUARDANDO_APROVACAO, salvo.getStatus());
            assertEquals(Fabrica.AREA, salvo.getArea());
            assertEquals(LocalDate.now(), salvo.getDataAbertura());
            assertNull(salvo.getIdSuporte());
            assertNull(salvo.getResolucao());
            assertNull(salvo.getDataFinal());
            assertEquals("Projetor sem imagem", resultado.getDescricao());
            assertEquals(Long.valueOf(3L), resultado.getIdSala());
            assertEquals(ID_SOLICITANTE, resultado.getIdUsuario());
        }

        @Test
        @DisplayName("não deve abrir chamado de solicitante inexistente")
        void naoDeveCriarChamadoSemSolicitante() {
            //mocks
            when(usuarioRepository.findById(ID_SOLICITANTE)).thenReturn(Optional.empty());
            ChamadoDTO dto = new ChamadoDTO();
            dto.setIdUsuario(ID_SOLICITANTE);

            //impl
            RuntimeException erro = assertThrows(RuntimeException.class, () -> chamadoService.criar(dto));

            //asserts
            assertEquals("Usuário solicitante não encontrado", erro.getMessage());
            verify(chamadoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Aprovação")
    class Aprovacao {

        @Test
        @DisplayName("deve aprovar sem trocar o solicitante pelo aprovador")
        void deveAprovarChamado() {
            //mocks
            UUID idAprovador = UUID.randomUUID();
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.AGUARDANDO_APROVACAO)));
            saveDevolveOMesmoObjeto();

            //impl
            ChamadoDTO resultado = chamadoService.aprovar(ID_CHAMADO, idAprovador);

            //asserts
            assertEquals(StatusChamado.APROVADO, resultado.getStatus());
            assertEquals(ID_SOLICITANTE, resultado.getIdUsuario(), "o solicitante não pode ser sobrescrito");
        }

        @Test
        @DisplayName("deve reprovar chamado aguardando aprovação")
        void deveReprovarChamado() {
            //mocks
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.AGUARDANDO_APROVACAO)));
            saveDevolveOMesmoObjeto();

            //impl
            ChamadoDTO resultado = chamadoService.reprovar(ID_CHAMADO, UUID.randomUUID());

            //asserts
            assertEquals(StatusChamado.REPROVADO, resultado.getStatus());
            assertEquals(ID_SOLICITANTE, resultado.getIdUsuario());
        }

        @ParameterizedTest(name = "status {0}")
        @EnumSource(value = StatusChamado.class, names = "AGUARDANDO_APROVACAO", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("não deve aprovar chamado fora de 'aguardando aprovação'")
        void naoDeveAprovarForaDoStatus(StatusChamado status) {
            //mocks
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, status)));

            //impl + asserts
            assertThrows(IllegalStateException.class, () -> chamadoService.aprovar(ID_CHAMADO, UUID.randomUUID()));
            verify(chamadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("não deve reprovar chamado já aprovado")
        void naoDeveReprovarChamadoAprovado() {
            //mocks
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.APROVADO)));

            //impl + asserts
            assertThrows(IllegalStateException.class, () -> chamadoService.reprovar(ID_CHAMADO, UUID.randomUUID()));
            verify(chamadoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Triagem")
    class Triagem {

        @Test
        @DisplayName("deve alocar um usuário de suporte e colocar em atendimento")
        void deveTriarChamadoAprovado() {
            //mocks
            UsuarioEntity suporte = Fabrica.usuario(UserRole.SUPORTE);
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.APROVADO)));
            when(usuarioRepository.findById(suporte.getId())).thenReturn(Optional.of(suporte));
            saveDevolveOMesmoObjeto();

            //impl
            ChamadoDTO resultado = chamadoService.triar(ID_CHAMADO, suporte.getId());

            //asserts
            assertEquals(StatusChamado.EM_ATENDIMENTO, resultado.getStatus());
            assertEquals(suporte.getId(), resultado.getIdSuporte());
        }

        @Test
        @DisplayName("não deve alocar usuário que não é do suporte")
        void naoDeveTriarParaQuemNaoESuporte() {
            //mocks
            UsuarioEntity aprovador = Fabrica.usuario(UserRole.APROVADOR);
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.APROVADO)));
            when(usuarioRepository.findById(aprovador.getId())).thenReturn(Optional.of(aprovador));

            //impl
            IllegalStateException erro = assertThrows(IllegalStateException.class,
                    () -> chamadoService.triar(ID_CHAMADO, aprovador.getId()));

            //asserts
            assertEquals("Usuário selecionado não possui papel de suporte", erro.getMessage());
            verify(chamadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("não deve triar quando o usuário de suporte não existe")
        void naoDeveTriarComSuporteInexistente() {
            //mocks
            UUID idInexistente = UUID.randomUUID();
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.APROVADO)));
            when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

            //impl + asserts
            RuntimeException erro = assertThrows(RuntimeException.class,
                    () -> chamadoService.triar(ID_CHAMADO, idInexistente));
            assertEquals("Usuário de suporte não encontrado", erro.getMessage());
        }

        @Test
        @DisplayName("não deve triar chamado que ainda não foi aprovado")
        void naoDeveTriarChamadoNaoAprovado() {
            //mocks
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.AGUARDANDO_APROVACAO)));

            //impl + asserts
            assertThrows(IllegalStateException.class, () -> chamadoService.triar(ID_CHAMADO, UUID.randomUUID()));
            verify(usuarioRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Resolução")
    class Resolucao {

        @Test
        @DisplayName("deve resolver com devolutiva e data de encerramento")
        void deveResolverChamadoEmAtendimento() {
            //mocks
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.EM_ATENDIMENTO)));
            saveDevolveOMesmoObjeto();

            //impl
            ChamadoDTO resultado = chamadoService.resolver(ID_CHAMADO, "Fonte trocada");

            //asserts
            assertEquals(StatusChamado.RESOLVIDO, resultado.getStatus());
            assertEquals("Fonte trocada", resultado.getResolucao());
            assertEquals(LocalDate.now(), resultado.getDataFinal());
        }

        @Test
        @DisplayName("não deve resolver chamado que não está em atendimento")
        void naoDeveResolverForaDeAtendimento() {
            //mocks
            when(chamadoRepository.findById(ID_CHAMADO))
                    .thenReturn(Optional.of(Fabrica.chamado(ID_CHAMADO, ID_SOLICITANTE, StatusChamado.APROVADO)));

            //impl + asserts
            assertThrows(IllegalStateException.class, () -> chamadoService.resolver(ID_CHAMADO, "ok"));
            verify(chamadoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consulta e exclusão")
    class ConsultaEExclusao {

        @Test
        @DisplayName("deve converter a página do repositório no DTO de paginação")
        void deveBuscarComFiltro() {
            //mocks
            ChamadoFilterDTO filtro = new ChamadoFilterDTO();
            PageRequestDTO pagina = new PageRequestDTO(0, 2);
            List<Chamado> chamados = List.of(
                    Fabrica.chamado(1L, ID_SOLICITANTE, StatusChamado.APROVADO),
                    Fabrica.chamado(2L, ID_SOLICITANTE, StatusChamado.RESOLVIDO));
            when(chamadoRepository.buscarGenerica(filtro, pagina))
                    .thenReturn(new PageImpl<>(chamados, PageRequest.of(0, 2), 5));

            //impl
            PageResponseDTO<ChamadoDTO> resultado = chamadoService.buscarComFiltro(filtro, pagina);

            //asserts
            assertEquals(2, resultado.getItens().size());
            assertEquals(5, resultado.getTotalItens());
            assertEquals(0, resultado.getPaginaAtual());
            assertEquals(3, resultado.getTotalPaginas());
            assertEquals(Long.valueOf(1L), resultado.getItens().get(0).getIdChamado());
        }

        @Test
        @DisplayName("deve falhar ao buscar chamado inexistente")
        void naoDeveBuscarChamadoInexistente() {
            //mocks
            when(chamadoRepository.findById(ID_CHAMADO)).thenReturn(Optional.empty());

            //impl + asserts
            RuntimeException erro = assertThrows(RuntimeException.class, () -> chamadoService.buscarPorId(ID_CHAMADO));
            assertEquals("Chamado não encontrado", erro.getMessage());
        }

        @Test
        @DisplayName("deve excluir chamado existente")
        void deveDeletarChamado() {
            //mocks
            when(chamadoRepository.existsById(ID_CHAMADO)).thenReturn(true);

            //impl
            chamadoService.deletar(ID_CHAMADO);

            //asserts
            verify(chamadoRepository).deleteById(ID_CHAMADO);
        }

        @Test
        @DisplayName("não deve excluir chamado inexistente")
        void naoDeveDeletarChamadoInexistente() {
            //mocks
            when(chamadoRepository.existsById(ID_CHAMADO)).thenReturn(false);

            //impl + asserts
            assertThrows(RuntimeException.class, () -> chamadoService.deletar(ID_CHAMADO));
            verify(chamadoRepository, never()).deleteById(any());
        }
    }
}
