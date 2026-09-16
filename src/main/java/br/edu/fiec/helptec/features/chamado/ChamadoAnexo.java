package br.edu.fiec.helptec.features.chamado;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_chamado_anexo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnexo;

    private Long idChamado;
    private String url;
    private String tipo;          // "IMAGEM" ou "VIDEO"
    private String nomeOriginal;
}