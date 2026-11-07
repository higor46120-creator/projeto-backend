package br.edu.fiec.helptec.features.skill.repository;

import br.edu.fiec.helptec.features.skill.UsuarioSkill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsuarioSkillRepository extends MongoRepository<UsuarioSkill, Long> {

    List<UsuarioSkill> findByIdUsuario(UUID idUsuario);

    List<UsuarioSkill> findByIdSkill(Long idSkill);

    boolean existsByIdUsuarioAndIdSkill(UUID idUsuario, Long idSkill);
}
