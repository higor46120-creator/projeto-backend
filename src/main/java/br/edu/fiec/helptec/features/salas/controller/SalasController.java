package br.edu.fiec.helptec.features.salas.controller;

import lombok.RequiredArgsConstructor;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.salas.model.dto.SalasDTO;
import br.edu.fiec.helptec.features.salas.model.dto.SalasFilterDTO;
import br.edu.fiec.helptec.features.salas.service.SalasService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Salas (também expostas como ferramentas MCP para agentes de IA). */
@RestController
@RequestMapping("/api/salas")
@RequiredArgsConstructor
public class SalasController {

    private final SalasService salasService;

    @McpTool(name = "buscar_salas",
            description = "Lista as salas/laboratórios cadastrados, com filtro opcional por nome ou termo e paginação. "
                    + "Use para descobrir o idSala antes de abrir um chamado.",
            annotations = @McpTool.McpAnnotations(title = "Buscar salas", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping
    public PageResponseDTO<SalasDTO> buscar(
            @McpToolParam(required = false, description = "Filtros opcionais: termo, nome") SalasFilterDTO filtro,
            @McpToolParam(required = false, description = "Paginação: page (começa em 0) e size (padrão 10)")
            PageRequestDTO pageRequest
    ) {
        return salasService.buscarComFiltro(filtro != null ? filtro : new SalasFilterDTO(),
                PageRequestDTO.ouPadrao(pageRequest));
    }

    @McpTool(name = "buscar_sala",
            description = "Retorna uma sala pelo idSala.",
            annotations = @McpTool.McpAnnotations(title = "Ver sala", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/{id}")
    public SalasDTO buscarPorId(@McpToolParam(description = "idSala") @PathVariable Long id) {
        return salasService.buscarPorId(id);
    }

    @McpTool(name = "criar_sala",
            description = "Cadastra uma nova sala. Informe apenas o nome; o idSala é gerado pelo sistema.",
            annotations = @McpTool.McpAnnotations(title = "Criar sala", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalasDTO criar(@McpToolParam(description = "Dados da sala (nome)") @RequestBody SalasDTO dto) {
        return salasService.criar(dto);
    }

    @McpTool(name = "atualizar_sala",
            description = "Altera o nome de uma sala existente.",
            annotations = @McpTool.McpAnnotations(title = "Atualizar sala", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @PutMapping("/{id}")
    public SalasDTO atualizar(
            @McpToolParam(description = "idSala") @PathVariable Long id,
            @McpToolParam(description = "Novos dados da sala (nome)") @RequestBody SalasDTO dto) {
        return salasService.atualizar(id, dto);
    }

    @McpTool(name = "excluir_sala",
            description = "Exclui uma sala pelo idSala. Não pode ser desfeito.",
            annotations = @McpTool.McpAnnotations(title = "Excluir sala", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@McpToolParam(description = "idSala") @PathVariable Long id) {
        salasService.deletar(id);
    }
}
