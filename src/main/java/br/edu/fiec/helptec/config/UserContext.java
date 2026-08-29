package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;

public class UserContext {
    private static final ThreadLocal<UsuarioEntity> currentUser = new ThreadLocal<>();

    public static void setUser(UsuarioEntity user) {
        currentUser.set(user);
    }

    public static UsuarioEntity getUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}