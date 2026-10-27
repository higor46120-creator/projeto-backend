package br.edu.fiec.helptec.features.skill;

import br.edu.fiec.helptec.features.commons.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

// Associação N:N — um usuário de suporte pode ter várias skills.
@Entity
@Table(name = "tb_usuario_skill", uniqueConstraints = @UniqueConstraint(columnNames = {"idUsuario", "idSkill"}))
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioSkill extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuarioSkill;

    private UUID idUsuario;
    private Long idSkill;
}
