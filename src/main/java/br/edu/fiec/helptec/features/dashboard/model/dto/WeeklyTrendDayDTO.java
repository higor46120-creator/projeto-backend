package br.edu.fiec.helptec.features.dashboard.model.dto;

public record WeeklyTrendDayDTO(
        String weekday,        // "Mon", "Tue", ...
        long ticketsOpened,
        long ticketsResolved
) {}
