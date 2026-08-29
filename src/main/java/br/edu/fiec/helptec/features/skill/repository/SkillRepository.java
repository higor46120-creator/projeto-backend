package br.edu.fiec.helptec.features.skill.repository;

import br.edu.fiec.helptec.features.skill.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
}
