package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.support.Fabrica;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtService sem Spring: a chave e a validade são injetadas direto nos campos
 * (ReflectionTestUtils), como o @Value faria.
 */
@DisplayName("JwtService")
class JwtServiceTest {

    private static final String CHAVE_TESTE = base64("chave-de-teste-do-helptec-com-mais-de-48-bytes-para-o-hmac");
    private static final String OUTRA_CHAVE = base64("outra-chave-qualquer-do-helptec-com-mais-de-48-bytes-hmac!");

    private JwtService jwtService;
    private UsuarioEntity usuario;

    private static String base64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private void configurar(String chave, long validadeMs) {
        ReflectionTestUtils.setField(jwtService, "secretKey", chave);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", validadeMs);
    }

    @BeforeEach
    void init() {
        jwtService = new JwtService();
        configurar(CHAVE_TESTE, 60_000L);
        usuario = Fabrica.usuario(UserRole.SUPORTE);
    }

    @Test
    @DisplayName("o token leva o e-mail como subject e o id do usuário")
    void deveGerarTokenComEmailEId() {
        //impl
        String token = jwtService.generateTokenWithClaims(usuario);

        //asserts
        assertEquals(usuario.getEmail(), jwtService.extractUsername(token));
        assertEquals(usuario.getId().toString(), jwtService.extractClaim(token, c -> c.get("id", String.class)));
    }

    @Test
    @DisplayName("token é válido para o próprio usuário e inválido para outro")
    void deveValidarDonoDoToken() {
        //mocks
        String token = jwtService.generateTokenWithClaims(usuario);
        UsuarioEntity outro = Fabrica.usuario(UserRole.ADMIN);

        //impl + asserts
        assertTrue(jwtService.isTokenValid(token, usuario));
        assertFalse(jwtService.isTokenValid(token, outro));
    }

    @Test
    @DisplayName("token expirado é recusado")
    void deveRecusarTokenExpirado() {
        //mocks
        configurar(CHAVE_TESTE, -1_000L); // já nasce vencido
        String token = jwtService.generateTokenWithClaims(usuario);

        //impl + asserts
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, usuario));
    }

    @Test
    @DisplayName("token assinado com outra chave é recusado")
    void deveRecusarTokenComAssinaturaInvalida() {
        //mocks
        String token = jwtService.generateTokenWithClaims(usuario);
        configurar(OUTRA_CHAVE, 60_000L);

        //impl + asserts
        assertThrows(JwtException.class, () -> jwtService.extractUsername(token));
    }
}
