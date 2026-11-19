package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Promove a SUPER_ADMIN o usuário cujo e-mail está em app.super-admin.email
 * (variável SUPER_ADMIN_EMAIL). Roda na subida da aplicação, em qualquer perfil.
 * O usuário precisa já existir (cadastro, login Google ou seed).
 */
@Component
@Order(100) // depois do seed do perfil local
public class SuperAdminInicializador implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminInicializador.class);

    private final UsuarioRepository usuarioRepository;

    @Value("${app.super-admin.email:}")
    private String superAdminEmail;

    public SuperAdminInicializador(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (superAdminEmail == null || superAdminEmail.isBlank()) {
            return;
        }
        String email = superAdminEmail.trim();
        usuarioRepository.findByEmail(email)
                .map(UsuarioEntity.class::cast)
                .ifPresentOrElse(usuario -> {
                    if (usuario.getRole() != UserRole.SUPER_ADMIN) {
                        usuario.setRole(UserRole.SUPER_ADMIN);
                        usuarioRepository.save(usuario);
                        log.info("[super-admin] {} promovido a SUPER_ADMIN", email);
                    }
                }, () -> log.warn("[super-admin] {} não está cadastrado; faça o cadastro/login e reinicie", email));
    }

    void setSuperAdminEmail(String superAdminEmail) {
        this.superAdminEmail = superAdminEmail;
    }
}
