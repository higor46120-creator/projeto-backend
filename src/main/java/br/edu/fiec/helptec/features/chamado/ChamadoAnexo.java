package br.edu.fiec.helptec.features.chamado;

import br.edu.fiec.helptec.config.mongo.GeneratedId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "chamado_anexos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoAnexo {

    @Id
    @GeneratedId
    private Long idAnexo;

    @Indexed
    private Long idChamado;
    private String url;
    private String tipo;          // "IMAGEM" ou "VIDEO"
    private String nomeOriginal;
}