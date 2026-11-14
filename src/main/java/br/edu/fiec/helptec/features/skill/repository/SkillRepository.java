package br.edu.fiec.helptec.features.skill.repository;

import br.edu.fiec.helptec.features.skill.Skill;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends MongoRepository<Skill, Long> {
}
