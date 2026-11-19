package br.edu.fiec.helptec.features.usuario.model;

import br.edu.fiec.helptec.features.usuario.model.dto.MeResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UserStatus;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("UsuarioEntity: papéis e status")
class UsuarioEntityTest {

    private static Set<String> autoridades(UsuarioEntity usuario) {
        return usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("SUPER_ADMIN acumula ADMIN e USER")
    void superAdminAcumulaPapeis() {
        //impl + asserts
        assertEquals(Set.of("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_USER"),
                autoridades(Fabrica.usuario(UserRole.SUPER_ADMIN)));
    }

    @Test
    @DisplayName("ADMIN não recebe SUPER_ADMIN")
    void adminNaoESuperAdmin() {
        //impl + asserts
        assertEquals(Set.of("ROLE_ADMIN", "ROLE_USER"), autoridades(Fabrica.usuario(UserRole.ADMIN)));
    }

    @Test
    @DisplayName("sem status gravado (registro antigo) conta como CONFIRMED")
    void statusNuloViraConfirmed() {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.USER);
        usuario.setStatus(null);

        //impl + asserts
        assertEquals(UserStatus.CONFIRMED, usuario.getStatus());
        assertTrue(usuario.isEnabled());
    }

    @Test
    @DisplayName("conta DELETED fica desabilitada; NOT_CONFIRMED continua entrando")
    void deletedDesabilita() {
        //mocks
        UsuarioEntity deletado = Fabrica.usuario(UserRole.USER);
        deletado.setStatus(UserStatus.DELETED);
        UsuarioEntity naoConfirmado = Fabrica.usuario(UserRole.USER);
        naoConfirmado.setStatus(UserStatus.NOT_CONFIRMED);

        //impl + asserts
        assertFalse(deletado.isEnabled());
        assertTrue(naoConfirmado.isEnabled());
    }

    @Test
    @DisplayName("MeResponseDTO leva o status do usuário")
    void meLevaStatus() {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.SUPORTE);
        usuario.setStatus(UserStatus.NOT_CONFIRMED);

        //impl
        MeResponseDTO me = MeResponseDTO.from(usuario);

        //asserts
        assertEquals(UserStatus.NOT_CONFIRMED, me.status());
        assertEquals(UserRole.SUPORTE, me.role());
    }
}
