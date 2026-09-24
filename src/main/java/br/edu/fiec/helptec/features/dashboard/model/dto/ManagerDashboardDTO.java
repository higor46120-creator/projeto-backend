package br.edu.fiec.helptec.features.dashboard.model.dto;

import java.util.List;
import java.util.Map;

public record ManagerDashboardDTO(
        long totalTickets,
        long inProgress,
        long resolved,
        long awaitingApproval,
        Map<String, Long> statusDistribution,   // "Open", "In Progress", "Resolved"
        Map<String, Long> ticketsByPriority,    // "Low", "Medium", "High", "Critical"
        List<WeeklyTrendDayDTO> weeklyTrend
) {}
