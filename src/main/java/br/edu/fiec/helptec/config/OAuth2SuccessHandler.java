package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    public OAuth2SuccessHandler(JwtService jwtService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String nome = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new IllegalStateException("Google não retornou e-mail para este login. Verifique o escopo 'email' na configuração do OAuth2.");
        }

        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .map(UsuarioEntity.class::cast)
                .orElseGet(() -> criarUsuarioAPartirDoGoogle(email, nome));

        String jwt = jwtService.generateTokenFromOAuth2User(oAuth2User, usuario);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", jwt)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private UsuarioEntity criarUsuarioAPartirDoGoogle(String email, String nome) {
        UsuarioEntity novoUsuario = new UsuarioEntity();
        novoUsuario.setNome(nome);
        novoUsuario.setEmail(email);
        novoUsuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        novoUsuario.setRole(UserRole.USER);

        return usuarioRepository.save(novoUsuario);
    }
}
