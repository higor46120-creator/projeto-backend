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
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

/**
 * Chamados. Os métodos com @McpTool também ficam disponíveis como ferramentas
 * para agentes de IA (servidor MCP em /mcp), com as mesmas permissões (@PreAuthorize)
 * do usuário dono do token JWT enviado pelo agente.
 */
@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private ChamadoAnexoService chamadoAnexoService;

    @McpTool(name = "buscar_chamados",
            description = "Lista chamados de suporte de TI com filtros opcionais e paginação. "
                    + "Status possíveis: AGUARDANDO_APROVACAO, APROVADO, REPROVADO, EM_ATENDIMENTO, RESOLVIDO, CANCELADO.",
            annotations = @McpTool.McpAnnotations(title = "Buscar chamados", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping
    public PageResponseDTO<ChamadoDTO> buscar(
            @McpToolParam(required = false, description = "Filtros opcionais: termo (texto livre), status, prioridade, criticidade, idUsuario (UUID do solicitante), idSala, idEquipamento")
            ChamadoFilterDTO filtro,
            @McpToolParam(required = false, description = "Paginação: page (começa em 0) e size (padrão 10)")
            PageRequestDTO pageRequest
    ) {
        return chamadoService.buscarComFiltro(filtro != null ? filtro : new ChamadoFilterDTO(),
                PageRequestDTO.ouPadrao(pageRequest));
    }

    @McpTool(name = "buscar_chamado",
            description = "Retorna os dados completos de um chamado pelo número (idChamado).",
            annotations = @McpTool.McpAnnotations(title = "Ver chamado", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/{id}")
    public ChamadoDTO buscarPorId(@McpToolParam(description = "Número do chamado (idChamado)") @PathVariable Long id) {
        return chamadoService.buscarPorId(id);
    }

    // Solicitante abre o chamado (o solicitante é sempre o usuário logado)
    @McpTool(name = "abrir_chamado",
            description = "Abre um chamado em nome do usuário logado; ele começa AGUARDANDO_APROVACAO. "
                    + "Informe descricao (formato '[Categoria] Título\\nDetalhes'), prioridade e criticidade "
                    + "(BAIXA, MEDIA ou ALTA). Use idSala/idEquipamento se existirem no cadastro "
                    + "(ferramentas buscar_salas/buscar_equipamentos); senão, salaInformada/equipamentoInformado com o texto. "
                    + "idChamado, idUsuario, status, datas, idSuporte e resolucao são preenchidos pelo sistema.",
            annotations = @McpTool.McpAnnotations(title = "Abrir chamado", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChamadoDTO criar(@McpToolParam(description = "Dados do chamado") @RequestBody ChamadoDTO dto) {
        UsuarioEntity solicitante = usuarioLogadoOuFalhar();
        dto.setIdUsuario(solicitante.getId());
        return chamadoService.criar(dto);
    }

    // Aprovador aprova o chamado
    @McpTool(name = "aprovar_chamado",
            description = "Aprova um chamado que está AGUARDANDO_APROVACAO (papéis APROVADOR ou ADMIN).",
            annotations = @McpTool.McpAnnotations(title = "Aprovar chamado", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PreAuthorize("hasAnyRole('APROVADOR', 'ADMIN')")
    @PatchMapping("/{id}/aprovar")
    public ChamadoDTO aprovar(@McpToolParam(description = "Número do chamado") @PathVariable Long id) {
        return chamadoService.aprovar(id, usuarioLogadoOuFalhar().getId());
    }

    // Aprovador reprova o chamado
    @McpTool(name = "reprovar_chamado",
            description = "Reprova um chamado que está AGUARDANDO_APROVACAO (papéis APROVADOR ou ADMIN).",
            annotations = @McpTool.McpAnnotations(title = "Reprovar chamado", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PreAuthorize("hasAnyRole('APROVADOR', 'ADMIN')")
    @PatchMapping("/{id}/reprovar")
    public ChamadoDTO reprovar(@McpToolParam(description = "Número do chamado") @PathVariable Long id) {
        return chamadoService.reprovar(id, usuarioLogadoOuFalhar().getId());
    }

    // Gerente aloca um usuário de suporte (filtrado por skill em listar_suportes_por_skill)
    @McpTool(name = "triar_chamado",
            description = "Triagem: aloca um técnico de suporte a um chamado APROVADO e o coloca EM_ATENDIMENTO "
                    + "(papéis GERENTE ou ADMIN). Use listar_suportes_por_skill para escolher o técnico.",
            annotations = @McpTool.McpAnnotations(title = "Triar chamado", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    @PatchMapping("/{id}/triagem")
    public ChamadoDTO triar(
            @McpToolParam(description = "Número do chamado") @PathVariable Long id,
            @McpToolParam(description = "idSuporte: UUID do usuário de suporte") @RequestBody TriagemRequestDTO request
    ) {
        return chamadoService.triar(id, request.getIdSuporte());
    }

    // Suporte assume o chamado aprovado ("Iniciar Atendimento")
    @McpTool(name = "iniciar_atendimento",
            description = "O usuário logado (SUPORTE ou ADMIN) assume um chamado APROVADO, que passa a EM_ATENDIMENTO.",
            annotations = @McpTool.McpAnnotations(title = "Iniciar atendimento", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PreAuthorize("hasAnyRole('SUPORTE', 'ADMIN')")
    @PatchMapping("/{id}/iniciar")
    public ChamadoDTO iniciarAtendimento(@McpToolParam(description = "Número do chamado") @PathVariable Long id) {
        return chamadoService.iniciarAtendimento(id, usuarioLogadoOuFalhar());
    }

    // Suporte finaliza o atendimento
    @McpTool(name = "resolver_chamado",
            description = "Finaliza um chamado EM_ATENDIMENTO com a devolutiva do que foi feito (papéis SUPORTE ou ADMIN).",
            annotations = @McpTool.McpAnnotations(title = "Resolver chamado", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PreAuthorize("hasAnyRole('SUPORTE', 'ADMIN')")
    @PatchMapping("/{id}/resolver")
    public ChamadoDTO resolver(
            @McpToolParam(description = "Número do chamado") @PathVariable Long id,
            @McpToolParam(description = "Devolutiva: o que foi feito para resolver") @RequestBody String resolucao
    ) {
        return chamadoService.resolver(id, resolucao);
    }

    @McpTool(name = "excluir_chamado",
            description = "Exclui definitivamente um chamado (somente ADMIN). Não pode ser desfeito.",
            annotations = @McpTool.McpAnnotations(title = "Excluir chamado", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@McpToolParam(description = "Número do chamado") @PathVariable Long id) {
        chamadoService.deletar(id);
    }

    // ---------------------------------------------------------
    // Anexos (imagens/vídeos) do chamado
    // Upload e CSV recebem arquivo (multipart): ficam só na API HTTP, sem @McpTool.
    // ---------------------------------------------------------

    @PostMapping(value = "/{id}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ChamadoAnexoDTO uploadAnexo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return chamadoAnexoService.upload(id, file);
    }

    @McpTool(name = "listar_anexos_do_chamado",
            description = "Lista as imagens/vídeos anexados a um chamado, com link temporário para abrir cada arquivo.",
            annotations = @McpTool.McpAnnotations(title = "Anexos do chamado", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/{id}/anexos")
    public List<ChamadoAnexoDTO> listarAnexos(@McpToolParam(description = "Número do chamado") @PathVariable Long id) {
        return chamadoAnexoService.listarPorChamado(id);
    }

    @McpTool(name = "excluir_anexo",
            description = "Exclui um anexo (arquivo) de um chamado pelo idAnexo. Não pode ser desfeito.",
            annotations = @McpTool.McpAnnotations(title = "Excluir anexo", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @DeleteMapping("/anexos/{idAnexo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarAnexo(@McpToolParam(description = "Id do anexo (idAnexo)") @PathVariable Long idAnexo) {
        chamadoAnexoService.deletar(idAnexo);
    }

    @PostMapping("/{id}/anexos/thumbnail")
    @ResponseStatus(HttpStatus.CREATED)
    public ChamadoAnexoDTO uploadAnexoComThumbnail(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return chamadoAnexoService.uploadComThumbnail(id, file);
    }

    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ChamadoDTO> criarViaCsv(@RequestPart("file") MultipartFile file) throws IOException {
        return chamadoService.importarCsv(file);
    }

    /** Usuário do token JWT; sem login responde 401. */
    private static UsuarioEntity usuarioLogadoOuFalhar() {
        UsuarioEntity usuario = UserContext.getUser();
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return usuario;
    }
}
