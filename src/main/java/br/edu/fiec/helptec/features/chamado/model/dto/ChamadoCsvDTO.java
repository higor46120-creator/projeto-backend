package br.edu.fiec.helptec.features.chamado.model.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class ChamadoCsvDTO {

    @CsvBindByName(column = "idUsuario")
    private String idUsuario;

    @CsvBindByName(column = "descricao")
    private String descricao;

    @CsvBindByName(column = "prioridade")
    private String prioridade;

    @CsvBindByName(column = "criticidade")
    private String criticidade;

    @CsvBindByName(column = "idEquipamento")
    private Long idEquipamento;

    @CsvBindByName(column = "idSala")
    private Long idSala;
}