package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("UserContext")
class UserContextTest {

    @AfterEach
    void limpar() {
        UserContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("usa o usuário definido pelo interceptor (/api/**)")
    void usaThreadLocal() {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.USER);
        UserContext.setUser(usuario);

        //impl + asserts
        assertSame(usuario, UserContext.getUser());
    }

    @Test
    @DisplayName("sem interceptor (ex.: /mcp) usa o usuário autenticado pelo JWT")
    void caiNoSecurityContext() {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.SUPORTE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));

        //impl + asserts
        assertSame(usuario, UserContext.getUser());
    }

    @Test
    @DisplayName("sem login devolve null")
    void semLogin() {
        //impl + asserts
        assertNull(UserContext.getUser());
    }
}
