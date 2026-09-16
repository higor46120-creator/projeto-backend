package br.edu.fiec.helptec.features.dashboard.model.dto;

import br.edu.fiec.helptec.features.chamado.StatusChamado;

import java.time.LocalDate;

public record ChamadoResumoAprovadorDTO(
        Long idChamado,
        String descricao,
        String prioridade,
        StatusChamado status,
        String nomeSolicitante,
        LocalDate dataAbertura
) {}
