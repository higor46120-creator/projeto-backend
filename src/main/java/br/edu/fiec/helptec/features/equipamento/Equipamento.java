package br.edu.fiec.helptec.features.equipamento;

import br.edu.fiec.helptec.config.mongo.GeneratedId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import br.edu.fiec.helptec.features.commons.AuditBaseEntity;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "equipamentos")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipamento extends AuditBaseEntity {
    @Id
    @GeneratedId
    private Long id;

    private String nome;
    private String patrimonio;//patrimonio
    private String status;//status
    private String tipo;//tipo
    private Double preco;

    // Campos da tela de equipamentos do front (Suporte)
    private String area;      // setor/área onde o equipamento fica
    private Long idSala;      // sala onde o equipamento está
    private String descricao;
}
