package br.edu.fiec.helptec.features.skill.controller;

import lombok.RequiredArgsConstructor;
import br.edu.fiec.helptec.features.skill.model.dto.SkillDTO;
import br.edu.fiec.helptec.features.skill.service.SkillService;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Skills (competências) dos técnicos de suporte, também expostas como ferramentas MCP. */
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @McpTool(name = "listar_skills",
            description = "Lista todas as skills (competências técnicas) cadastradas.",
            annotations = @McpTool.McpAnnotations(title = "Listar skills", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping
    public List<SkillDTO> listar() {
        return skillService.listar();
    }

    @McpTool(name = "criar_skill",
            description = "Cadastra uma nova skill. Informe apenas o nome; o idSkill é gerado pelo sistema.",
            annotations = @McpTool.McpAnnotations(title = "Criar skill", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillDTO criar(@McpToolParam(description = "Dados da skill (nome)") @RequestBody SkillDTO dto) {
        return skillService.criar(dto);
    }

    // Vincula uma skill a um usuário de suporte
    @McpTool(name = "atribuir_skill",
            description = "Vincula uma skill a um usuário de suporte.",
            annotations = @McpTool.McpAnnotations(title = "Atribuir skill", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @PostMapping("/{idSkill}/usuarios/{idUsuario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atribuir(
            @McpToolParam(description = "idSkill") @PathVariable Long idSkill,
            @McpToolParam(description = "UUID do usuário de suporte") @PathVariable UUID idUsuario) {
        skillService.atribuirSkill(idUsuario, idSkill);
    }

    @McpTool(name = "remover_skill",
            description = "Remove o vínculo de uma skill com um usuário de suporte.",
            annotations = @McpTool.McpAnnotations(title = "Remover skill do usuário", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @DeleteMapping("/{idSkill}/usuarios/{idUsuario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(
            @McpToolParam(description = "idSkill") @PathVariable Long idSkill,
            @McpToolParam(description = "UUID do usuário de suporte") @PathVariable UUID idUsuario) {
        skillService.removerSkill(idUsuario, idSkill);
    }

    @McpTool(name = "listar_skills_do_usuario",
            description = "Lista as skills de um usuário de suporte.",
            annotations = @McpTool.McpAnnotations(title = "Skills do usuário", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/usuarios/{idUsuario}")
    public List<SkillDTO> listarDoUsuario(
            @McpToolParam(description = "UUID do usuário") @PathVariable UUID idUsuario) {
        return skillService.listarSkillsDoUsuario(idUsuario);
    }

    // Usado na triagem pelo gerente: lista suportes que possuem a skill informada
    @McpTool(name = "listar_suportes_por_skill",
            description = "Lista os técnicos de suporte que possuem a skill informada. "
                    + "Use antes de triar_chamado para escolher quem atende.",
            annotations = @McpTool.McpAnnotations(title = "Suportes por skill", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/{idSkill}/suportes")
    public List<UsuarioResponseDTO> listarSuportesPorSkill(
            @McpToolParam(description = "idSkill") @PathVariable Long idSkill) {
        return skillService.listarSuportesPorSkill(idSkill);
    }
}
