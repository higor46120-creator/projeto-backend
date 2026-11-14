package br.edu.fiec.helptec.support;

import br.edu.fiec.helptec.features.chamado.Chamado;
import br.edu.fiec.helptec.features.chamado.StatusChamado;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Fábrica de objetos para os testes (padrão "Test Data Builder").
 * Centraliza a criação dos dados para que cada teste mostre só o que importa nele.
 */
public final class Fabrica {

    public static final String AREA = "Informática";

    private Fabrica() {
    }

    public static UsuarioEntity usuario(UserRole role) {
        return usuario(UUID.randomUUID(), role);
    }

    public static UsuarioEntity usuario(UUID id, UserRole role) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(id);
        usuario.setNome("Usuário " + role.name());
        usuario.setEmail(role.name().toLowerCase() + "@helptec.local");
        usuario.setPassword("senha-criptografada");
        usuario.setRole(role);
        usuario.setArea(AREA);
        return usuario;
    }

    public static Chamado chamado(Long id, UUID idSolicitante, StatusChamado status) {
        return Chamado.builder()
                .idChamado(id)
                .idUsuario(idSolicitante)
                .area(AREA)
                .descricao("[Hardware] Computador não liga\nO PC da mesa 3 não liga.")
                .prioridade("ALTA")
                .criticidade("ALTA")
                .status(status)
                .dataAbertura(LocalDate.now().minusDays(1))
                .idSala(1L)
                .idEquipamento(2L)
                .build();
    }
}
