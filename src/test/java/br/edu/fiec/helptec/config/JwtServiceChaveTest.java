package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JwtService sem JWT_SECRET")
class JwtServiceChaveTest {

    @Test
    @DisplayName("sem JWT_SECRET gera uma chave aleatória e continua funcionando")
    void deveGerarChaveAleatoriaQuandoNaoConfigurada() {
        //mocks
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 60_000L);
        UsuarioEntity usuario = Fabrica.usuario(UserRole.USER);

        //impl
        jwtService.validarChave();
        String token = jwtService.generateTokenWithClaims(usuario);

        //asserts
        assertEquals(usuario.getEmail(), jwtService.extractUsername(token));
    }
}
