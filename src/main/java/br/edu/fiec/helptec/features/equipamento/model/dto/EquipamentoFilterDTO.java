package br.edu.fiec.helptec.features.equipamento.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipamentoFilterDTO {

    private String termo;
    private String nome;
    private String patrimonio;
    private String status;
    private String tipo;
    private Double precoMin;
    private Double precoMax;
}
