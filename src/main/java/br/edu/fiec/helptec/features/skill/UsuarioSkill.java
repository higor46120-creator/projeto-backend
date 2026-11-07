package br.edu.fiec.helptec.features.skill;

import br.edu.fiec.helptec.config.mongo.GeneratedId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import br.edu.fiec.helptec.features.commons.AuditBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

// Associação N:N — um usuário de suporte pode ter várias skills.
@Document(collection = "usuario_skills")
@CompoundIndex(name = "usuario_skill_unico", def = "{'idUsuario': 1, 'idSkill': 1}", unique = true)
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioSkill extends AuditBaseEntity {

    @Id
    @GeneratedId
    private Long idUsuarioSkill;

    private UUID idUsuario;
    private Long idSkill;
}
