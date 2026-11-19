package br.edu.fiec.helptec.features.usuario.model.entity;

import br.edu.fiec.helptec.config.mongo.GeneratedId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "usuarios")
public class UsuarioEntity implements UserDetails {

    @Id
    @GeneratedId
    private UUID id;

    private String nome;

    private String area;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String fcmToken;

    private UserRole role;

    // Situação da conta. Registros antigos (sem o campo) contam como CONFIRMED.
    private UserStatus status;

    // UUID do usuário responsável por aprovar os chamados abertos por este usuário.
    // Nulo para quem não possui aprovador definido (ex: o próprio aprovador, admins, etc).


    public UsuarioEntity(String nome, String email, String password, String fcmToken, UserRole role) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.fcmToken = fcmToken;
        this.role = role;
        this.area = area;
    }

    public UsuarioEntity(String nome, String email, String password, String fcmToken, UserRole role, String area) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.fcmToken = fcmToken;
        this.role = role;
        this.area = area;
    }

    // =======================================================
    // Conversão de Roles para Authorization no Spring Security
    // =======================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // SUPER_ADMIN acumula ADMIN e USER; ADMIN acumula USER
        if (this.role == UserRole.SUPER_ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        if (this.role == UserRole.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return List.of(new SimpleGrantedAuthority(this.role.getRole()));
    }

    /** Status da conta; nulo (cadastros anteriores ao campo) vale como CONFIRMED. */
    public UserStatus getStatus() {
        return this.status != null ? this.status : UserStatus.CONFIRMED;
    }

    // =======================================================
    // Demais métodos da interface UserDetails
    // =======================================================

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Conta DELETED não autentica (login, Google e token JWT)
    @Override
    public boolean isEnabled() {
        return getStatus() != UserStatus.DELETED;
    }

}
