package br.edu.fiec.helptec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de INTEGRAÇÃO: sobe a aplicação inteira, então precisa do banco
 * (MongoDB) e das variáveis de ambiente (GOOGLE_CLIENT_ID etc.).
 * Por isso fica fora do "./mvnw test" comum. Para rodar:
 *   ./mvnw test -Dintegracao=true
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "integracao", matches = "true")
class HelptecApplicationTests {

	@Test
	void contextLoads() {
	}

}
