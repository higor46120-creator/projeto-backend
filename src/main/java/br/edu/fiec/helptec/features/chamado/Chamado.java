package br.edu.fiec.helptec.features.chamado;

import br.edu.fiec.helptec.features.commons.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_chamado")
public class Chamado extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idChamado;

    // Referências a Usuario (UUID, mesmo tipo da PK de UsuarioEntity)
    private UUID idUsuario;      // solicitante, quem abriu o chamado
    private String area;    // area correspondente ao solicitante
    private UUID idSuporte;      // usuário de suporte alocado na triagem

    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusChamado status;

    private String prioridade;
    private String criticidade;
    private LocalDate dataAbertura;
    private LocalDate dataFinal;
    private String resolucao;

    // Referências a Equipamento/Salas (Long, mesmo tipo das PKs dessas entidades)
    private Long idEquipamento;
    private Long idSala;



}
