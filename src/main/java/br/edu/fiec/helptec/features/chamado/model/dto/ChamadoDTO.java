package br.edu.fiec.helptec.features.chamado.model.dto;

import br.edu.fiec.helptec.features.chamado.StatusChamado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoDTO {
    private Long idChamado;
    private UUID idUsuario;
    private String area;
    private UUID idSuporte;
    private String descricao;
    private StatusChamado status;
    private String prioridade;
    private String criticidade;
    private LocalDate dataAbertura;
    private LocalDate dataFinal;
    private String resolucao;
    private Long idEquipamento;
    private Long idSala;
}
