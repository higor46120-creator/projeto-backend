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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public PageResponseDTO<ChamadoDTO> buscarComFiltro(ChamadoFilterDTO filtro, PageRequestDTO pageRequest) {
        Page<Chamado> page = chamadoRepository.buscarGenerica(filtro, pageRequest);

        List<ChamadoDTO> dtos = page.getContent().stream().map(this::toDTO).toList();

        return new PageResponseDTO<>(
                dtos,
                page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages()
        );
    }

    public ChamadoDTO buscarPorId(Long id) {
        return toDTO(buscarOuFalhar(id));
    }

    // ---------------------------------------------------------
    // 1. Abertura do chamado: auto-popula o aprovador do solicitante
    // ---------------------------------------------------------
    public ChamadoDTO criar(ChamadoDTO dto) {
        UsuarioEntity solicitante = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário solicitante não encontrado"));



        Chamado chamado = toEntity(dto);
        chamado.setIdChamado(null);
        chamado.setArea(solicitante.getArea());
        chamado.setIdSuporte(null);
        chamado.setStatus(StatusChamado.AGUARDANDO_APROVACAO);
        chamado.setDataAbertura(LocalDate.now());
        chamado.setDataFinal(null);
        chamado.setResolucao(null);

        return toDTO(chamadoRepository.save(chamado));
    }

    // ---------------------------------------------------------
    // 2. Aprovador decide: aprova ou reprova
    // ---------------------------------------------------------
    public ChamadoDTO aprovar(Long idChamado, UUID idUsuarioLogado) {
        Chamado chamado = buscarOuFalhar(idChamado);
        validarStatus(chamado, StatusChamado.AGUARDANDO_APROVACAO);
        chamado.setIdUsuario(idUsuarioLogado);
        chamado.setStatus(StatusChamado.APROVADO);
        return toDTO(chamadoRepository.save(chamado));
    }

    public ChamadoDTO reprovar(Long idChamado, UUID idUsuarioLogado) {
        Chamado chamado = buscarOuFalhar(idChamado);
        validarStatus(chamado, StatusChamado.AGUARDANDO_APROVACAO);


        chamado.setStatus(StatusChamado.REPROVADO);
        return toDTO(chamadoRepository.save(chamado));
    }

    // ---------------------------------------------------------
    // 3. Gerente triagem: aloca um usuário de suporte ao chamado aprovado
    // ---------------------------------------------------------
    public ChamadoDTO triar(Long idChamado, UUID idSuporte) {
        Chamado chamado = buscarOuFalhar(idChamado);
        validarStatus(chamado, StatusChamado.APROVADO);

        UsuarioEntity suporte = usuarioRepository.findById(idSuporte)
                .orElseThrow(() -> new RuntimeException("Usuário de suporte não encontrado"));

        if (suporte.getRole() != UserRole.SUPORTE) {
            throw new IllegalStateException("Usuário selecionado não possui papel de suporte");
        }

        chamado.setIdSuporte(idSuporte);
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        return toDTO(chamadoRepository.save(chamado));
    }

    // ---------------------------------------------------------
    // 4. Suporte resolve o chamado
    // ---------------------------------------------------------
    public ChamadoDTO resolver(Long idChamado, String resolucao) {
        Chamado chamado = buscarOuFalhar(idChamado);
        validarStatus(chamado, StatusChamado.EM_ATENDIMENTO);

        chamado.setResolucao(resolucao);
        chamado.setDataFinal(LocalDate.now());
        chamado.setStatus(StatusChamado.RESOLVIDO);
        return toDTO(chamadoRepository.save(chamado));
    }

    public void deletar(Long id) {
        if (!chamadoRepository.existsById(id)) {
            throw new RuntimeException("Chamado não encontrado");
        }
        chamadoRepository.deleteById(id);
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------
    private Chamado buscarOuFalhar(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
    }

    private void validarStatus(Chamado chamado, StatusChamado esperado) {
        if (chamado.getStatus() != esperado) {
            throw new IllegalStateException(
                    "Chamado está em status " + chamado.getStatus() + ", esperado " + esperado
            );
        }
    }


    private ChamadoDTO toDTO(Chamado c) {
        return new ChamadoDTO(
                c.getIdChamado(), c.getIdUsuario(),c.getArea(), c.getIdSuporte(),
                c.getDescricao(), c.getStatus(), c.getPrioridade(), c.getCriticidade(),
                c.getDataAbertura(), c.getDataFinal(), c.getResolucao(), c.getIdEquipamento(), c.getIdSala()
        );
    }

    private Chamado toEntity(ChamadoDTO dto) {
        return Chamado.builder()
                .idChamado(dto.getIdChamado())
                .criticidade(dto.getCriticidade())
                .descricao(dto.getDescricao())
                .idSala(dto.getIdSala())
                .idEquipamento(dto.getIdEquipamento())
                .prioridade(dto.getPrioridade())
                .resolucao(dto.getResolucao())
                .dataAbertura(dto.getDataAbertura())
                .build();

    }
}
