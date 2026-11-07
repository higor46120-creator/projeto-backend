package br.edu.fiec.helptec.features.salas;

import br.edu.fiec.helptec.config.mongo.GeneratedId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import br.edu.fiec.helptec.features.commons.AuditBaseEntity;
import lombok.*;


@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@Document(collection = "salas")
@NoArgsConstructor
@AllArgsConstructor
public class Salas extends AuditBaseEntity {

    @Id
    @GeneratedId

    private Long idSala;

    private String nome;
}
