package br.edu.fiec.helptec.features.dashboard.model.dto;

public record TendenciaDiaDTO(
        String theday,       // "Seg", "Ter", ...
        long openTickets,
        long resolvedTickets
) {}