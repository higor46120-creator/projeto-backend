package br.edu.fiec.helptec.features.chamado.model.dto;

import br.edu.fiec.helptec.features.chamado.StatusChamado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoFilterDTO {
    private String termo;
    private StatusChamado status;
    private String prioridade;
    private String criticidade;
    private UUID idUsuario;
    private Long idEquipamento;
    private Long idSala;
}
