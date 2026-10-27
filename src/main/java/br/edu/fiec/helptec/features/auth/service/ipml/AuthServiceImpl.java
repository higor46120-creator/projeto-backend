package br.edu.fiec.helptec.features.auth.service.ipml;

import br.edu.fiec.helptec.config.JwtService;
import br.edu.fiec.helptec.features.auth.models.AuthResponse;
import br.edu.fiec.helptec.features.auth.models.GoogleLoginRequest;
import br.edu.fiec.helptec.features.auth.models.LoginRequest;
import br.edu.fiec.helptec.features.auth.models.RegisterRequest;
import br.edu.fiec.helptec.features.auth.service.AuthService;
import br.edu.fiec.helptec.features.auth.service.GoogleTokenVerifier;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier googleTokenVerifier;
    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager,
                           GoogleTokenVerifier googleTokenVerifier) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Override
    public void register(RegisterRequest request) {
        // Verifica se o e-mail já está cadastrado
        usuarioRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        });

        UsuarioEntity usuarioEntity = new UsuarioEntity();
        usuarioEntity.setNome(request.nome());
        usuarioEntity.setEmail(request.email());
        usuarioEntity.setPassword(passwordEncoder.encode(request.password())); // Criptografa a senha
        usuarioEntity.setFcmToken(request.fcmToken());
        usuarioEntity.setRole(UserRole.USER); // Define o perfil padrão como USER

        usuarioRepository.save(usuarioEntity);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Autentica as credenciais com o AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Busca o usuário no banco para carregar as informações e ID
        UsuarioEntity usuario = usuarioRepository.findByEmail(request.email())
                .map(u -> (UsuarioEntity) u)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // Gera o token JWT contendo ID e e-mail
        String token = jwtService.generateTokenWithClaims(usuario);

        return new AuthResponse(token);
    }

    @Override
    public AuthResponse loginComGoogle(GoogleLoginRequest request) {
        // 1. Valida o token com o Google (401 se inválido)
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.idToken());

        // 2. Só aceita e-mail que o Google confirmou ser da pessoa
        String email = payload.getEmail();
        if (email == null || !Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "E-mail do Google ausente ou não verificado");
        }

        // 3. Confere se existe registro no banco (404 se não existir)
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .map(u -> (UsuarioEntity) u)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuário não cadastrado no HelpTec"));

        // 4. Opcional: atualiza o token de push do aparelho
        if (request.fcmToken() != null && !request.fcmToken().isBlank()) {
            usuario.setFcmToken(request.fcmToken());
            usuarioRepository.save(usuario);
        }

        // 5. Devolve o JWT do HelpTec, igual ao /login
        return new AuthResponse(jwtService.generateTokenWithClaims(usuario));
    }
}
