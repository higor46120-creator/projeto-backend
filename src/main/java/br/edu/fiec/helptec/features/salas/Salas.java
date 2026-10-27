package br.edu.fiec.helptec.features.salas;

import br.edu.fiec.helptec.features.commons.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_salas")
public class Salas extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long idSala;

    private String nome;
}
