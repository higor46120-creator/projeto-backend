package br.edu.fiec.helptec.features.dashboard.controller;

import lombok.RequiredArgsConstructor;
import br.edu.fiec.helptec.features.dashboard.model.dto.ChamadoResumoAprovadorDTO;
import br.edu.fiec.helptec.features.dashboard.model.dto.ManagerDashboardDTO;
import br.edu.fiec.helptec.features.dashboard.model.dto.ResumoAprovadorDTO;
import br.edu.fiec.helptec.features.dashboard.service.DashboardService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Indicadores dos painéis (também expostos como ferramentas MCP para agentes de IA). */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @McpTool(name = "dashboard_gerente",
            description = "Indicadores do painel do gerente (totais por status, prioridade, etc.). Somente GERENTE.",
            annotations = @McpTool.McpAnnotations(title = "Dashboard do gerente", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('GERENTE')")
    @GetMapping("/gerente")
    public ManagerDashboardDTO gerente(
            @McpToolParam(required = false, description = "Área para filtrar (opcional)")
            @RequestParam(required = false) String area) {
        return dashboardService.gerarDashboardGerente(area);
    }

    @McpTool(name = "resumo_aprovador",
            description = "Resumo do painel do aprovador (pendentes, aprovados, reprovados). Somente APROVADOR.",
            annotations = @McpTool.McpAnnotations(title = "Resumo do aprovador", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('APROVADOR')")
    @GetMapping("/aprovador/resumo")
    public ResumoAprovadorDTO resumoAprovador(
            @McpToolParam(required = false, description = "Área para filtrar (opcional)")
            @RequestParam(required = false) String area) {
        return dashboardService.gerarResumoAprovador(area);
    }

    @McpTool(name = "chamados_do_aprovador",
            description = "Lista os chamados do painel do aprovador; apenasPendentes=true traz só os que aguardam aprovação. "
                    + "Somente APROVADOR.",
            annotations = @McpTool.McpAnnotations(title = "Chamados do aprovador", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('APROVADOR')")
    @GetMapping("/aprovador/chamados")
    public List<ChamadoResumoAprovadorDTO> chamadosAprovador(
            @McpToolParam(required = false, description = "Área para filtrar (opcional)")
            @RequestParam(required = false) String area,
            @McpToolParam(required = false, description = "true = apenas pendentes de aprovação (padrão false)")
            @RequestParam(defaultValue = "false") Boolean apenasPendentes
    ) {
        return dashboardService.listarChamadosAprovador(area, Boolean.TRUE.equals(apenasPendentes));
    }
}
