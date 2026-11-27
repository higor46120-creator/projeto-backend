package br.edu.fiec.helptec.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    // Mesma URL usada pelo OAuth2SuccessHandler (front: /oauth2/redirect)
    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String frontendRedirectUri;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          AuthenticationProvider authenticationProvider,
                          OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    private static final String[] PUBLIC_MATCHERS = {
            "/api/v1/auth/**",

            // Spring Security 6: excecoes nas rotas publicas sao redirecionadas internamente para /error.
            // Sem liberar essa rota, o erro real (400/500) vira um 401 vazio.
            "/error",

            "/oauth2/**",
            "/login/oauth2/**",

            "/images/**",

            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    // Endpoint do servidor MCP (spring.ai.mcp.server.streamable-http.mcp-endpoint, padrão /mcp)
    private static final String[] MCP_MATCHERS = {"/mcp", "/mcp/**"};

    // java:S4502 (CSRF desativado): seguro aqui. A API é stateless e autentica só pelo JWT no
    // cabeçalho Authorization; não há cookie de sessão que o navegador envie sozinho, então
    // um site de terceiros não consegue forjar uma requisição autenticada.
    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Habilita a configuração de CORS no Spring Security
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Desabilita CSRF (Desnecessário para APIs REST Stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Define a política de sessão como STATELESS (Sem cookies/sessões no servidor)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configura autorização das rotas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        // Servidor MCP (agentes de IA): exige o mesmo JWT da API no cabeçalho
                        // "Authorization: Bearer <token>". As permissões por papel (@PreAuthorize)
                        // de cada ferramenta continuam valendo.
                        .requestMatchers(MCP_MATCHERS).authenticated()
                        .anyRequest().authenticated()
                )

                // Quem não está autenticado recebe uma resposta diferente conforme o cliente:
                //  1) API, mobile, front (fetch com token) e MCP -> 401 com WWW-Authenticate: Bearer
                //  2) Navegador abrindo uma página do backend (ex.: localhost:8080) -> login do Google
                // A primeira regra também é a padrão: o que não casar com nenhuma recebe 401.
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(bearerEntryPoint(), requisicaoDeApi())
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/google"),
                                requisicaoDeNavegador())
                )

                .authenticationProvider(authenticationProvider)

                // Login com Google pelo NAVEGADOR (front web): /oauth2/authorization/google
                // -> Google -> /login/oauth2/code/google -> OAuth2SuccessHandler gera o JWT
                // e redireciona para app.oauth2.redirect-uri?token=...
                // O mobile continua usando POST /api/v1/auth/google com o ID token.
                // As regras de exceptionHandling acima garantem que a API e o mobile recebam 401
                // (e não um redirecionamento para a página do Google).
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        // Em caso de falha, devolve para o front com ?error=... (em vez de /login?error,
                        // que não existe no backend e virava uma página 404)
                        .failureHandler((request, response, exception) -> response.sendRedirect(
                                frontendRedirectUri + "?error=" + URLEncoder.encode(
                                        String.valueOf(exception.getMessage()), StandardCharsets.UTF_8)
                        ))
                )

                // Insere o filtro JWT antes do filtro padrão do Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 401 + WWW-Authenticate: Bearer (o cabeçalho orienta clientes de API e MCP a mandar o JWT). */
    private static AuthenticationEntryPoint bearerEntryPoint() {
        return (request, response, authException) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"helptec\"");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        };
    }

    /**
     * Chamadas de API: rotas da API (inclusive /usuarios, /notifications e /mcp, que não ficam
     * em /api), qualquer requisição com cabeçalho Authorization (mobile e front com token)
     * ou que peça JSON.
     */
    private static RequestMatcher requisicaoDeApi() {
        var path = PathPatternRequestMatcher.withDefaults();
        var pedeJson = new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON);
        pedeJson.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        return new OrRequestMatcher(
                path.matcher("/api/**"),
                path.matcher("/usuarios/**"),
                path.matcher("/notifications/**"),
                path.matcher("/mcp/**"),
                new RequestHeaderRequestMatcher(HttpHeaders.AUTHORIZATION),
                pedeJson
        );
    }

    /** Navegador abrindo uma página: o cabeçalho Accept pede text/html. */
    private static RequestMatcher requisicaoDeNavegador() {
        var pedeHtml = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
        pedeHtml.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        return pedeHtml;
    }

    // Endereços do front que podem chamar a API, separados por vírgula
    // (CORS_ALLOWED_ORIGINS). "*" libera qualquer endereço.
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        // Cabeçalhos do protocolo MCP que clientes no navegador precisam ler
        configuration.setExposedHeaders(List.of("Mcp-Session-Id", "MCP-Protocol-Version", HttpHeaders.WWW_AUTHENTICATE));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}