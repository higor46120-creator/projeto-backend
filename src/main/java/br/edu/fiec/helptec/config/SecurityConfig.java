package br.edu.fiec.helptec.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
                        .anyRequest().authenticated()
                )

                // Trata exceções de não autorizado para retornar HTTP 401 em JSON em vez de redirecionar para HTML do Google
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                .authenticationProvider(authenticationProvider)

                // Login com Google pelo NAVEGADOR (front web): /oauth2/authorization/google
                // -> Google -> /login/oauth2/code/google -> OAuth2SuccessHandler gera o JWT
                // e redireciona para app.oauth2.redirect-uri?token=...
                // O mobile continua usando POST /api/v1/auth/google com o ID token.
                // Como o authenticationEntryPoint acima é explícito, a API continua respondendo
                // 401 em JSON (e não redireciona para a página do Google).
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}