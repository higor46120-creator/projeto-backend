package br.edu.fiec.helptec.features.usuario.model.entity;

public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    APROVADOR("ROLE_APROVADOR"),
    GERENTE("ROLE_GERENTE"),
    SUPORTE("ROLE_SUPORTE");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
