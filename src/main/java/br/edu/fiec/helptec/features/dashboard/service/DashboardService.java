package br.edu.fiec.helptec.features.dashboard.service;

import br.edu.fiec.helptec.features.chamado.Chamado;
import br.edu.fiec.helptec.features.chamado.StatusChamado;
import br.edu.fiec.helptec.features.chamado.repository.ChamadoRepository;
import br.edu.fiec.helptec.features.dashboard.model.dto.ChamadoResumoAprovadorDTO;
import br.edu.fiec.helptec.features.dashboard.model.dto.ManagerDashboardDTO;
import br.edu.fiec.helptec.features.dashboard.model.dto.ResumoAprovadorDTO;
import br.edu.fiec.helptec.features.dashboard.model.dto.WeeklyTrendDayDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Rótulos em inglês retornados nas chaves dos mapas (statusDistribution / ticketsByPriority)
    private static final String[] PRIORITY_LABELS = {"Low", "Medium", "High", "Critical"};
    private static final String[] WEEKDAY_LABELS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    // Prioridade é salva em português no banco (Baixa/Média/Alta/Crítica) — este mapa
    // só serve para bater o rótulo em inglês do relatório com o valor salvo no Chamado.
    private static final Map<String, String> PRIORITY_PT_TO_EN = Map.of(
            "baixa", "Low",
            "média", "Medium",
            "media", "Medium",
            "alta", "High",
            "crítica", "Critical",
            "critica", "Critical"
    );

    // ---------------------------------------------------------
    // Manager dashboard
    // ---------------------------------------------------------

    public ManagerDashboardDTO gerarDashboardGerente(String area) {
        List<Chamado> chamados = buscarChamados(area);

        long inProgress = contarPorStatus(chamados, StatusChamado.EM_ATENDIMENTO);
        long resolved = contarPorStatus(chamados, StatusChamado.RESOLVIDO);
        long awaitingApproval = contarPorStatus(chamados, StatusChamado.AGUARDANDO_APROVACAO);

        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        long open = chamados.stream()
                .filter(c -> c.getStatus() == StatusChamado.AGUARDANDO_APROVACAO || c.getStatus() == StatusChamado.APROVADO)
                .count();
        statusDistribution.put("Open", open);
        statusDistribution.put("In Progress", inProgress);
        statusDistribution.put("Resolved", resolved);

        Map<String, Long> ticketsByPriority = new LinkedHashMap<>();
        for (String label : PRIORITY_LABELS) {
            ticketsByPriority.put(label, 0L);
        }
        chamados.stream()
                .filter(c -> c.getPrioridade() != null)
                .forEach(c -> {
                    String label = PRIORITY_PT_TO_EN.get(c.getPrioridade().toLowerCase());
                    if (label != null) {
                        ticketsByPriority.merge(label, 1L, Long::sum);
                    }
                });

        List<WeeklyTrendDayDTO> weeklyTrend = calcularTendenciaSemanal(chamados);

        return new ManagerDashboardDTO(
                chamados.size(), inProgress, resolved, awaitingApproval,
                statusDistribution, ticketsByPriority, weeklyTrend
        );
    }

    private List<WeeklyTrendDayDTO> calcularTendenciaSemanal(List<Chamado> chamados) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        List<WeeklyTrendDayDTO> trend = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate dia = monday.plusDays(i);

            long ticketsOpened = chamados.stream()
                    .filter(c -> dia.equals(c.getDataAbertura()))
                    .count();

            long ticketsResolved = chamados.stream()
                    .filter(c -> c.getStatus() == StatusChamado.RESOLVIDO && dia.equals(c.getDataFinal()))
                    .count();

            trend.add(new WeeklyTrendDayDTO(WEEKDAY_LABELS[i], ticketsOpened, ticketsResolved));
        }

        return trend;
    }

    private long contarPorStatus(List<Chamado> chamados, StatusChamado status) {
        return chamados.stream().filter(c -> c.getStatus() == status).count();
    }

    // ---------------------------------------------------------
    // Tela "Área do Aprovador"
    // ---------------------------------------------------------

    public ResumoAprovadorDTO gerarResumoAprovador(String area) {
        List<Chamado> chamados = buscarChamados(area);

        long pendentes = contarPorStatus(chamados, StatusChamado.AGUARDANDO_APROVACAO);

        long aprovados = chamados.stream()
                .filter(c -> c.getStatus() != StatusChamado.AGUARDANDO_APROVACAO && c.getStatus() != StatusChamado.REPROVADO)
                .count();

        return new ResumoAprovadorDTO(pendentes, aprovados, chamados.size());
    }

    public List<ChamadoResumoAprovadorDTO> listarChamadosAprovador(String area, boolean apenasPendentes) {
        List<Chamado> chamados = buscarChamados(area);

        if (apenasPendentes) {
            chamados = chamados.stream()
                    .filter(c -> c.getStatus() == StatusChamado.AGUARDANDO_APROVACAO)
                    .toList();
        }

        List<UUID> idsSolicitantes = chamados.stream().map(Chamado::getIdUsuario).distinct().toList();
        Map<UUID, String> nomesPorId = usuarioRepository.findAllById(idsSolicitantes).stream()
                .collect(Collectors.toMap(UsuarioEntity::getId, UsuarioEntity::getNome));

        return chamados.stream()
                .sorted(Comparator.comparing(Chamado::getDataAbertura, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(c -> new ChamadoResumoAprovadorDTO(
                        c.getIdChamado(),
                        c.getDescricao(),
                        c.getPrioridade(),
                        c.getStatus(),
                        nomesPorId.getOrDefault(c.getIdUsuario(), "Desconhecido"),
                        c.getDataAbertura()
                ))
                .toList();
    }

    // ---------------------------------------------------------
    // Helper compartilhado
    // ---------------------------------------------------------

    private List<Chamado> buscarChamados(String area) {
        if (area == null || area.isBlank()) {
            return chamadoRepository.findAll();
        }

        List<UUID> idsUsuariosArea = usuarioRepository.findByArea(area).stream()
                .map(UsuarioEntity::getId)
                .toList();

        return chamadoRepository.findAll().stream()
                .filter(c -> idsUsuariosArea.contains(c.getIdUsuario()))
                .toList();
    }
}
