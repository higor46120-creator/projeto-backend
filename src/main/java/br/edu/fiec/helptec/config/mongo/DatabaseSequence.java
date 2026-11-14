package br.edu.fiec.helptec.config.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Contador usado para gerar IDs numéricos sequenciais (um documento por coleção). */
@Data
@NoArgsConstructor
@Document(collection = "database_sequences")
public class DatabaseSequence {

    @Id
    private String id;   // nome da coleção, ex.: "chamados"
    private long seq;
}
