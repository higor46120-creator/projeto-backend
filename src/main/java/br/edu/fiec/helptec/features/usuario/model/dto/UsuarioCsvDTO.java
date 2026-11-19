package br.edu.fiec.helptec.features.usuario.model.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

/**
 * Uma linha do CSV de importação de usuários (POST /usuarios/csv).
 * Cabeçalho: nome,email,senha,perfil,area
 * <ul>
 *   <li>nome e email: obrigatórios</li>
 *   <li>senha: opcional; vazia = senha aleatória (a pessoa entra pelo Google)</li>
 *   <li>perfil: USER (padrão), APROVADOR, GERENTE, SUPORTE ou ADMIN</li>
 *   <li>area: opcional</li>
 * </ul>
 */
@Data
public class UsuarioCsvDTO {

    @CsvBindByName(column = "nome")
    private String nome;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "senha")
    private String senha;

    @CsvBindByName(column = "perfil")
    private String perfil;

    @CsvBindByName(column = "area")
    private String area;
}
