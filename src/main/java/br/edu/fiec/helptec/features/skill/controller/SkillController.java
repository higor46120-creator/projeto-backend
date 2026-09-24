package br.edu.fiec.helptec.features.skill.controller;

import br.edu.fiec.helptec.features.skill.model.dto.SkillDTO;
import br.edu.fiec.helptec.features.skill.service.SkillService;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping
    public ResponseEntity<List<SkillDTO>> listar() {
        return ResponseEntity.ok(skillService.listar());
    }

    @PostMapping
    public ResponseEntity<SkillDTO> criar(@RequestBody SkillDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.criar(dto));
    }

    // Vincula uma skill a um usuário de suporte
    @PostMapping("/{idSkill}/usuarios/{idUsuario}")
    public ResponseEntity<Void> atribuir(@PathVariable Long idSkill, @PathVariable UUID idUsuario) {
        skillService.atribuirSkill(idUsuario, idSkill);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{idSkill}/usuarios/{idUsuario}")
    public ResponseEntity<Void> remover(@PathVariable Long idSkill, @PathVariable UUID idUsuario) {
        skillService.removerSkill(idUsuario, idSkill);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios/{idUsuario}")
    public ResponseEntity<List<SkillDTO>> listarDoUsuario(@PathVariable UUID idUsuario) {
        return ResponseEntity.ok(skillService.listarSkillsDoUsuario(idUsuario));
    }

    // Usado na triagem pelo gerente: lista suportes que possuem a skill informada
    @GetMapping("/{idSkill}/suportes")
    public ResponseEntity<List<UsuarioResponseDTO>> listarSuportesPorSkill(@PathVariable Long idSkill) {
        return ResponseEntity.ok(skillService.listarSuportesPorSkill(idSkill));
    }
}
