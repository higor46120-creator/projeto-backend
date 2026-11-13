package br.edu.fiec.helptec.features.chamado;

import br.edu.fiec.helptec.config.mongo.GeneratedId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "chamados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chamado {

    @Id
    @GeneratedId
    private Long idChamado;

    private UUID idUsuario;   // solicitante
    private String area;      // copiado do solicitante na criação
    private UUID idSuporte;   // usuário de suporte alocado na triagem

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