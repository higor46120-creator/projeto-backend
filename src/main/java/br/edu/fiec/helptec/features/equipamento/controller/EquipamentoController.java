package br.edu.fiec.helptec.features.equipamento.controller;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.equipamento.model.dto.EquipamentoDTO;
import br.edu.fiec.helptec.features.equipamento.model.dto.EquipamentoFilterDTO;
import br.edu.fiec.helptec.features.equipamento.service.EquipamentoService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Equipamentos/patrimônios (também expostos como ferramentas MCP para agentes de IA). */
@RestController
@RequestMapping("/api/equipamentos")
public class EquipamentoController {

    @Autowired
    private EquipamentoService equipamentoService;

    @McpTool(name = "buscar_equipamentos",
            description = "Lista equipamentos/patrimônios com filtros opcionais e paginação. "
                    + "Use para descobrir o idEquipamento antes de abrir um chamado.",
            annotations = @McpTool.McpAnnotations(title = "Buscar equipamentos", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping
    public PageResponseDTO<EquipamentoDTO> buscar(
            @McpToolParam(required = false, description = "Filtros opcionais: termo, nome, patrimonio, status, tipo, "
                    + "precoMin, precoMax, area, idSala") EquipamentoFilterDTO filtro,
            @McpToolParam(required = false, description = "Paginação: page (começa em 0) e size (padrão 10)")
            PageRequestDTO pageRequest
    ) {
        return equipamentoService.buscarComFiltro(filtro != null ? filtro : new EquipamentoFilterDTO(),
                PageRequestDTO.ouPadrao(pageRequest));
    }

    @McpTool(name = "buscar_equipamento",
            description = "Retorna um equipamento pelo idEquipamento.",
            annotations = @McpTool.McpAnnotations(title = "Ver equipamento", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/{id}")
    public EquipamentoDTO buscarPorId(@McpToolParam(description = "idEquipamento") @PathVariable Long id) {
        return equipamentoService.buscarPorId(id);
    }

    @McpTool(name = "criar_equipamento",
            description = "Cadastra um equipamento: nome, patrimonio, tipo, preco, area, idSala, descricao "
                    + "e status (padrão OPERACIONAL). O id é gerado pelo sistema.",
            annotations = @McpTool.McpAnnotations(title = "Criar equipamento", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipamentoDTO criar(@McpToolParam(description = "Dados do equipamento") @RequestBody EquipamentoDTO dto) {
        return equipamentoService.criar(dto);
    }

    @McpTool(name = "atualizar_equipamento",
            description = "Altera os dados de um equipamento existente (substitui os campos enviados).",
            annotations = @McpTool.McpAnnotations(title = "Atualizar equipamento", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @PutMapping("/{id}")
    public EquipamentoDTO atualizar(
            @McpToolParam(description = "idEquipamento") @PathVariable Long id,
            @McpToolParam(description = "Novos dados do equipamento") @RequestBody EquipamentoDTO dto) {
        return equipamentoService.atualizar(id, dto);
    }

    @McpTool(name = "excluir_equipamento",
            description = "Exclui um equipamento pelo idEquipamento. Não pode ser desfeito.",
            annotations = @McpTool.McpAnnotations(title = "Excluir equipamento", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@McpToolParam(description = "idEquipamento") @PathVariable Long id) {
        equipamentoService.deletar(id);
    }
}
