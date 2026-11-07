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

}
