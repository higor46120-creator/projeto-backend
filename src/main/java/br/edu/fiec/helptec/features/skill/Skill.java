package br.edu.fiec.helptec.features.skill;

import br.edu.fiec.helptec.config.mongo.GeneratedId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Catálogo de skills existentes (ex: REDE, HARDWARE, SOFTWARE, IMPRESSORA...)
@Document(collection = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedId
    private Long idSkill;

    @Indexed(unique = true)
    private String nome;
}
