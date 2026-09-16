package br.edu.fiec.helptec.features.chamado;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_chamado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idChamado;

    private UUID idUsuario;   // solicitante
    private String area;      // copiado do solicitante na criação
    private UUID idSuporte;   // usuário de suporte alocado na triagem

    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusChamado status;

    private String prioridade;
    private String criticidade;
    private LocalDate dataAbertura;
    private LocalDate dataFinal;
    private String resolucao;

    private Long idEquipamento;
    private Long idSala;
}