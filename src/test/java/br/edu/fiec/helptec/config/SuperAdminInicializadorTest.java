package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuperAdminInicializador")
class SuperAdminInicializadorTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SuperAdminInicializador inicializador;

    @Test
    @DisplayName("promove a SUPER_ADMIN o e-mail configurado")
    void devePromover() {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.ADMIN);
        inicializador.setSuperAdminEmail("admin@helptec.local");
        when(usuarioRepository.findByEmail("admin@helptec.local")).thenReturn(Optional.of(usuario));

        //impl
        inicializador.run(null);

        //asserts
        assertEquals(UserRole.SUPER_ADMIN, usuario.getRole());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("sem e-mail configurado não faz nada")
    void semEmailNaoFazNada() {
        //mocks
        inicializador.setSuperAdminEmail("");

        //impl
        inicializador.run(null);

        //asserts
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("e-mail não cadastrado só gera aviso")
    void emailDesconhecido() {
        //mocks
        inicializador.setSuperAdminEmail("ninguem@fiec.edu.br");
        when(usuarioRepository.findByEmail("ninguem@fiec.edu.br")).thenReturn(Optional.empty());

        //impl
        inicializador.run(null);

        //asserts
        verify(usuarioRepository, never()).save(any());
    }
}
