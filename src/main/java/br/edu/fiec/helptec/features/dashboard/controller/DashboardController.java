package br.edu.fiec.helptec.features.dashboard.controller;

import br.edu.fiec.helptec.features.dashboard.model.dto.ChamadoResumoAprovadorDTO;
import br.edu.fiec.helptec.features.dashboard.model.dto.ManagerDashboardDTO;
import br.edu.fiec.helptec.features.dashboard.model.dto.ResumoAprovadorDTO;
import br.edu.fiec.helptec.features.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @PreAuthorize("hasRole('GERENTE')")
    @GetMapping("/gerente")
    public ResponseEntity<ManagerDashboardDTO> gerente(@RequestParam(required = false) String area) {
        return ResponseEntity.ok(dashboardService.gerarDashboardGerente(area));
    }

    @PreAuthorize("hasRole('APROVADOR')")
    @GetMapping("/aprovador/resumo")
    public ResponseEntity<ResumoAprovadorDTO> resumoAprovador(@RequestParam(required = false) String area) {
        return ResponseEntity.ok(dashboardService.gerarResumoAprovador(area));
    }

    @PreAuthorize("hasRole('APROVADOR')")
    @GetMapping("/aprovador/chamados")
    public ResponseEntity<List<ChamadoResumoAprovadorDTO>> chamadosAprovador(
            @RequestParam(required = false) String area,
            @RequestParam(defaultValue = "false") boolean apenasPendentes
    ) {
        return ResponseEntity.ok(dashboardService.listarChamadosAprovador(area, apenasPendentes));
    }
}
