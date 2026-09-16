package br.edu.fiec.helptec.features.dashboard.model.dto;

import java.util.List;
import java.util.Map;

public record DashboardGerenteDTO(
        long totalTickets,
        long inProgress,
        long resolvidos,
        long awaitingApproval,
        Map<String, Long> statusDistribution,   // "Aberto", "Em Andamento", "Resolvida"
        Map<String, Long> ticketsByPriority,   // "Baixa", "Média", "Alta", "Crítica" (zero-filled)
        List<TendenciaDiaDTO> weeklyTrend
) {}