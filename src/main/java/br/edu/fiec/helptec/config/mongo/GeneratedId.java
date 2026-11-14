package br.edu.fiec.helptec.config.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca o campo @Id que deve ser gerado automaticamente ao salvar um documento novo.
 * <ul>
 *   <li>{@code Long}: número sequencial por coleção (1, 2, 3...), como o AUTO_INCREMENT do MySQL.
 *       Mantém os IDs numéricos que o front-end e o app já usam.</li>
 *   <li>{@code UUID}: UUID aleatório (usado no usuário).</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeneratedId {
}
