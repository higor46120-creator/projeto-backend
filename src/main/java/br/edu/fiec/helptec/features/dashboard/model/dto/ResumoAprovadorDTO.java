package br.edu.fiec.helptec.features.dashboard.model.dto;

public record ResumoAprovadorDTO(
        long totalPending,
        long totalApproved,
        long totalTickets
) {}
