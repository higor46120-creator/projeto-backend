package br.edu.fiec.helptec.features.skill.service;

import br.edu.fiec.helptec.features.skill.Skill;
import br.edu.fiec.helptec.features.skill.UsuarioSkill;
import br.edu.fiec.helptec.features.skill.model.dto.SkillDTO;
import br.edu.fiec.helptec.features.skill.repository.SkillRepository;
import br.edu.fiec.helptec.features.skill.repository.UsuarioSkillRepository;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UsuarioSkillRepository usuarioSkillRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ---- Catálogo de skills ----

    public List<SkillDTO> listar() {
        return skillRepository.findAll().stream().map(this::toDTO).toList();
    }

    public SkillDTO criar(SkillDTO dto) {
        Skill skill = new Skill(null, dto.getNome());
        return toDTO(skillRepository.save(skill));
    }

    // ---- Associação usuário de suporte <-> skill ----

    public void atribuirSkill(UUID idUsuario, Long idSkill) {
        UsuarioEntity usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getRole() != UserRole.SUPORTE) {
            throw new IllegalStateException("Somente usuários com papel SUPORTE podem receber skills");
        }

        if (!skillRepository.existsById(idSkill)) {
            throw new RuntimeException("Skill não encontrada");
        }

        if (usuarioSkillRepository.existsByIdUsuarioAndIdSkill(idUsuario, idSkill)) {
            return; // já associado, nada a fazer
        }

        usuarioSkillRepository.save(new UsuarioSkill(null, idUsuario, idSkill));
    }

    public void removerSkill(UUID idUsuario, Long idSkill) {
        usuarioSkillRepository.findByIdUsuario(idUsuario).stream()
                .filter(us -> us.getIdSkill().equals(idSkill))
                .forEach(us -> usuarioSkillRepository.deleteById(us.getIdUsuarioSkill()));
    }

    public List<SkillDTO> listarSkillsDoUsuario(UUID idUsuario) {
        List<Long> idsSkills = usuarioSkillRepository.findByIdUsuario(idUsuario)
                .stream().map(UsuarioSkill::getIdSkill).toList();

        return skillRepository.findAllById(idsSkills).stream().map(this::toDTO).toList();
    }

    // ---- Usado na triagem: gerente busca suportes que possuem determinada skill ----

    public List<UsuarioResponseDTO> listarSuportesPorSkill(Long idSkill) {
        List<UUID> idsUsuarios = usuarioSkillRepository.findByIdSkill(idSkill)
                .stream().map(UsuarioSkill::getIdUsuario).toList();

        return usuarioRepository.findAllById(idsUsuarios).stream()
                .filter(u -> u.getRole() == UserRole.SUPORTE)
                .map(u -> new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail(), u.getRole()))
                .toList();
    }

    private SkillDTO toDTO(Skill s) {
        return new SkillDTO(s.getIdSkill(), s.getNome());
    }
}
