package br.edu.fiec.helptec.features.auth.service;

import br.edu.fiec.helptec.config.JwtService;
import br.edu.fiec.helptec.features.auth.models.AuthResponse;
import br.edu.fiec.helptec.features.auth.models.GoogleLoginRequest;
import br.edu.fiec.helptec.features.auth.models.LoginRequest;
import br.edu.fiec.helptec.features.auth.models.RegisterRequest;
import br.edu.fiec.helptec.features.auth.service.ipml.AuthServiceImpl;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import br.edu.fiec.helptec.support.Fabrica;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    private static final String EMAIL = "ana@fiec.edu.br";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @InjectMocks
    private AuthServiceImpl authService;

    private static GoogleIdToken.Payload payloadGoogle(String email, Boolean verificado) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.setEmailVerified(verificado);
        return payload;
    }

    // ---------------- Cadastro ----------------

    @Test
    @DisplayName("cadastro: grava a senha criptografada e o papel USER")
    void deveCadastrarComSenhaCriptografada() {
        //mocks
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("hash-bcrypt");

        //impl
        authService.register(new RegisterRequest("Ana", EMAIL, "123456", null));

        //asserts
        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hash-bcrypt", captor.getValue().getPassword(), "a senha nunca é gravada em texto puro");
        assertEquals(UserRole.USER, captor.getValue().getRole());
    }

    @Test
    @DisplayName("cadastro: recusa e-mail já cadastrado")
    void naoDeveCadastrarEmailRepetido() {
        //mocks
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(Fabrica.usuario(UserRole.USER)));

        //impl
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> authService.register(new RegisterRequest("Ana", EMAIL, "123456", null)));

        //asserts
        assertEquals("E-mail já cadastrado no sistema.", erro.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    // ---------------- Login com e-mail e senha ----------------

    @Test
    @DisplayName("login: credenciais válidas devolvem o JWT")
    void deveLogarEDevolverToken() {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.USER);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(jwtService.generateTokenWithClaims(usuario)).thenReturn("jwt-gerado");

        //impl
        AuthResponse resposta = authService.login(new LoginRequest(EMAIL, "123456"));

        //asserts
        assertEquals("jwt-gerado", resposta.token());
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(EMAIL, "123456"));
    }

    @Test
    @DisplayName("login: senha errada não gera token")
    void naoDeveLogarComSenhaErrada() {
        //mocks
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Credenciais inválidas"));

        //impl + asserts
        assertThrows(BadCredentialsException.class, () -> authService.login(new LoginRequest(EMAIL, "errada")));
        verify(jwtService, never()).generateTokenWithClaims(any());
    }

    // ---------------- Login com Google (app) ----------------

    @Test
    @DisplayName("Google: usuário cadastrado recebe o JWT e o token de push é atualizado")
    void deveLogarComGoogle() {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.APROVADOR);
        when(googleTokenVerifier.verify("id-token")).thenReturn(payloadGoogle(EMAIL, true));
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(jwtService.generateTokenWithClaims(usuario)).thenReturn("jwt-google");

        //impl
        AuthResponse resposta = authService.loginComGoogle(new GoogleLoginRequest("id-token", "fcm-123"));

        //asserts
        assertEquals("jwt-google", resposta.token());
        assertEquals("fcm-123", usuario.getFcmToken());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Google: e-mail não verificado é recusado com 401")
    void naoDeveAceitarEmailNaoVerificado() {
        //mocks
        when(googleTokenVerifier.verify("id-token")).thenReturn(payloadGoogle(EMAIL, false));

        //impl
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> authService.loginComGoogle(new GoogleLoginRequest("id-token", null)));

        //asserts
        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
    }

    @Test
    @DisplayName("Google: e-mail sem cadastro é recusado com 404")
    void naoDeveLogarComGoogleSemCadastro() {
        //mocks
        when(googleTokenVerifier.verify("id-token")).thenReturn(payloadGoogle(EMAIL, true));
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        //impl
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> authService.loginComGoogle(new GoogleLoginRequest("id-token", null)));

        //asserts
        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verify(jwtService, never()).generateTokenWithClaims(any());
    }
}
