package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class ApplicationAuditAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of("SYSTEM");
        }

        // Handles standard Spring Security User details
        if (authentication.getPrincipal() instanceof UsuarioEntity user) {
            return Optional.of(user.getUsername());
        }

        // Fallback to principal string representation
        return Optional.of(authentication.getName());
    }
}