package br.edu.fiec.helptec.features.chamado.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoAnexoDTO {
    private Long idAnexo;
    private Long idChamado;
    private String url;
    private String tipo;
    private String nomeOriginal;
}