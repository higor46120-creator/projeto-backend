package br.edu.fiec.helptec.features.usuario.model.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "tb_usuario")
public class UsuarioEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    private String area;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

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
        // ADMIN acumula a permissão de USER também
        if (this.role == UserRole.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return List.of(new SimpleGrantedAuthority(this.role.getRole()));
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

    @Override
    public boolean isEnabled() {
        return true;
    }

}
