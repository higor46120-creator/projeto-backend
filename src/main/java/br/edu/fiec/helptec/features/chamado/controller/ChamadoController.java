package br.edu.fiec.helptec.features.chamado.controller;

import br.edu.fiec.helptec.config.UserContext;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoAnexoDTO;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoDTO;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoFilterDTO;
import br.edu.fiec.helptec.features.chamado.model.dto.TriagemRequestDTO;
import br.edu.fiec.helptec.features.chamado.service.ChamadoAnexoService;
import br.edu.fiec.helptec.features.chamado.service.ChamadoService;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private ChamadoAnexoService chamadoAnexoService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<ChamadoDTO>> buscar(
            ChamadoFilterDTO filtro,
            PageRequestDTO pageRequest
    ) {
        return ResponseEntity.ok(chamadoService.buscarComFiltro(filtro, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id));
    }

    // Solicitante abre o chamado. O aprovador é resolvido automaticamente
    // a partir do campo idAprovador do usuário solicitante.
    @PostMapping
    public ResponseEntity<ChamadoDTO> criar(@RequestBody ChamadoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.criar(dto));
    }

    // Aprovador designado aprova o chamado
    @PreAuthorize("hasRole('APROVADOR')")
    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<ChamadoDTO> aprovar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(chamadoService.aprovar(id, UserContext.getUser().getId()));
    }

    // Aprovador designado reprova o chamado
    @PatchMapping("/{id}/reprovar")
    public ResponseEntity<ChamadoDTO> reprovar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioEntity usuarioLogado
    ) {
        return ResponseEntity.ok(chamadoService.reprovar(id, usuarioLogado.getId()));
    }

    // Gerente triado: aloca um usuário de suporte (previamente filtrado por skill
    // via GET /api/skills/{idSkill}/suportes) ao chamado já aprovado
    @PatchMapping("/{id}/triagem")
    public ResponseEntity<ChamadoDTO> triar(
            @PathVariable Long id,
            @RequestBody TriagemRequestDTO request
    ) {
        return ResponseEntity.ok(chamadoService.triar(id, request.getIdSuporte()));
    }

    // Suporte finaliza o atendimento
    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ChamadoDTO> resolver(
            @PathVariable Long id,
            @RequestBody String resolucao
    ) {
        return ResponseEntity.ok(chamadoService.resolver(id, resolucao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        chamadoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // Anexos (imagens/vídeos) do chamado
    // ---------------------------------------------------------

    @PostMapping(value = "/{id}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChamadoAnexoDTO> uploadAnexo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoAnexoService.upload(id, file));
    }

    @GetMapping("/{id}/anexos")
    public ResponseEntity<List<ChamadoAnexoDTO>> listarAnexos(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoAnexoService.listarPorChamado(id));
    }

    @DeleteMapping("/anexos/{idAnexo}")
    public ResponseEntity<Void> deletarAnexo(@PathVariable Long idAnexo) {
        chamadoAnexoService.deletar(idAnexo);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/anexos/thumbnail")
    public ResponseEntity<ChamadoAnexoDTO> uploadAnexoComThumbnail(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoAnexoService.uploadComThumbnail(id, file));
    }
    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ChamadoDTO>> criarViaCsv(
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.importarCsv(file));
    }

}