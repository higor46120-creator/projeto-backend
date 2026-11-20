package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContext {
    private static final ThreadLocal<UsuarioEntity> currentUser = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUser(UsuarioEntity user) {
        currentUser.set(user);
    }

    /**
     * Usuário da requisição. Em /api/** vem do interceptor; fora dele (ex.: chamadas
     * de ferramentas MCP em /mcp) cai no usuário autenticado pelo filtro JWT.
     */
    public static UsuarioEntity getUser() {
        UsuarioEntity usuario = currentUser.get();
        if (usuario != null) {
            return usuario;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UsuarioEntity principal) {
            return principal;
        }
        return null;
    }

    public static void clear() {
        currentUser.remove();
    }
}
