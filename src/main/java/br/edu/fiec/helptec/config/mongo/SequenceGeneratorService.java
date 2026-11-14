package br.edu.fiec.helptec.config.mongo;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Gera o próximo número de uma sequência de forma atômica ($inc com upsert),
 * seguro mesmo com várias requisições ao mesmo tempo.
 */
@Service
public class SequenceGeneratorService {

    private final MongoOperations mongoOperations;

    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public long proximoValor(String nomeSequencia) {
        DatabaseSequence contador = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(nomeSequencia)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                DatabaseSequence.class
        );
        return contador != null ? contador.getSeq() : 1L;
    }
}
