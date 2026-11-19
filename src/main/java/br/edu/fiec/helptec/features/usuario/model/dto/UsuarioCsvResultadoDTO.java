package br.edu.fiec.helptec.features.usuario.model.dto;

import java.util.List;

/**
 * Resultado da importação: as linhas válidas são criadas e as inválidas
 * voltam em "erros" com o número da linha no arquivo (o cabeçalho é a linha 1).
 */
public record UsuarioCsvResultadoDTO(
        int totalLinhas,
        List<UsuarioResponseDTO> criados,
        List<ErroLinha> erros
) {
    public record ErroLinha(int linha, String email, String motivo) {
    }
}
