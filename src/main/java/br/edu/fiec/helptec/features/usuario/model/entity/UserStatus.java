package br.edu.fiec.helptec.features.usuario.model.entity;

/** Situação da conta do usuário. */
public enum UserStatus {
    /** Conta desativada: não consegue mais fazer login nem usar o token. */
    DELETED,
    /** Cadastrada, mas o e-mail ainda não foi confirmado (cadastro próprio, admin ou CSV). */
    NOT_CONFIRMED,
    /** E-mail confirmado (login com Google ou usuários já existentes). */
    CONFIRMED
}
