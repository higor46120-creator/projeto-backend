package br.edu.fiec.helptec.features.usuario.controller;

import br.edu.fiec.helptec.config.UserContext;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.CreateUsuarioRequestDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.MeResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.service.UsuarioService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/** Usuários (também expostos como ferramentas MCP para agentes de IA). */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @McpTool(name = "criar_usuario",
            description = "Cadastra um usuário: nome, email, senha e tipoPermissao (perfil numérico).",
            annotations = @McpTool.McpAnnotations(title = "Criar usuário", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = false, openWorldHint = false))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO criar(
            @McpToolParam(description = "Dados do novo usuário") @RequestBody CreateUsuarioRequestDTO request) {
        return usuarioService.criar(request);
    }

    @McpTool(name = "listar_usuarios",
            description = "Lista todos os usuários. Para muitos registros prefira listar_usuarios_paginado.",
            annotations = @McpTool.McpAnnotations(title = "Listar usuários", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioService.listarTodos();
    }

    @McpTool(name = "listar_usuarios_paginado",
            description = "Lista usuários com paginação.",
            annotations = @McpTool.McpAnnotations(title = "Listar usuários (paginado)", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/paginado")
    public PageResponseDTO<UsuarioResponseDTO> listarPaginado(
            @McpToolParam(required = false, description = "Página, começa em 0 (padrão 0)")
            @RequestParam(defaultValue = "0") Integer page,
            @McpToolParam(required = false, description = "Itens por página (padrão 10)")
            @RequestParam(defaultValue = "10") Integer size) {
        PageRequestDTO pageRequest = PageRequestDTO.ouPadrao(
                new PageRequestDTO(page != null ? page : 0, size != null ? size : 0));
        return usuarioService.listarPaginado(pageRequest);
    }

    // Dados do usuário autenticado (identificado pelo JWT)
    @McpTool(name = "meu_usuario",
            description = "Retorna os dados e o perfil (role) do usuário dono do token usado pelo agente.",
            annotations = @McpTool.McpAnnotations(title = "Quem sou eu", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/me")
    public MeResponseDTO me() {
        UsuarioEntity usuarioLogado = UserContext.getUser();
        if (usuarioLogado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return MeResponseDTO.from(usuarioLogado);
    }

    @McpTool(name = "buscar_usuario",
            description = "Retorna um usuário pelo id (UUID).",
            annotations = @McpTool.McpAnnotations(title = "Ver usuário", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarPorId(@McpToolParam(description = "UUID do usuário") @PathVariable String id) {
        return usuarioService.buscarPorId(UUID.fromString(id));
    }

    @McpTool(name = "atualizar_usuario",
            description = "Altera nome, email, senha e/ou tipoPermissao de um usuário.",
            annotations = @McpTool.McpAnnotations(title = "Atualizar usuário", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizar(
            @McpToolParam(description = "UUID do usuário") @PathVariable String id,
            @McpToolParam(description = "Novos dados do usuário") @RequestBody CreateUsuarioRequestDTO request) {
        return usuarioService.atualizar(UUID.fromString(id), request);
    }

    @McpTool(name = "excluir_usuario",
            description = "Exclui um usuário pelo id (UUID). Não pode ser desfeito.",
            annotations = @McpTool.McpAnnotations(title = "Excluir usuário", readOnlyHint = false,
                    destructiveHint = true, idempotentHint = true, openWorldHint = false))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@McpToolParam(description = "UUID do usuário") @PathVariable UUID id) {
        usuarioService.deletar(id);
    }
}
