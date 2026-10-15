package br.edu.fiec.helptec.features.usuario.model.dto;

import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;

import java.util.UUID;

public record MeResponseDTO(
        UUID idUsuario,
        String nome,
        String email,
        UserRole role,
        String area
) {
    public static MeResponseDTO from(UsuarioEntity usuario) {
        return new MeResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getArea()
        );
    }
}
