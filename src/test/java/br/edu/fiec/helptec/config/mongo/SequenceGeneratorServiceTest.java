package br.edu.fiec.helptec.config.mongo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SequenceGeneratorService")
class SequenceGeneratorServiceTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private SequenceGeneratorService sequenceGenerator;

    private static DatabaseSequence contador(long valor) {
        DatabaseSequence seq = new DatabaseSequence();
        seq.setId("chamados");
        seq.setSeq(valor);
        return seq;
    }

    @Test
    @DisplayName("devolve o valor já incrementado pelo banco")
    void deveDevolverProximoValor() {
        //mocks
        when(mongoOperations.findAndModify(any(Query.class), any(UpdateDefinition.class),
                any(FindAndModifyOptions.class), eq(DatabaseSequence.class))).thenReturn(contador(8));

        //impl + asserts
        assertEquals(8L, sequenceGenerator.proximoValor("chamados"));
    }

    @Test
    @DisplayName("sem contador ainda, começa em 1")
    void deveComecarEmUm() {
        //mocks
        when(mongoOperations.findAndModify(any(Query.class), any(UpdateDefinition.class),
                any(FindAndModifyOptions.class), eq(DatabaseSequence.class))).thenReturn(null);

        //impl + asserts
        assertEquals(1L, sequenceGenerator.proximoValor("chamados"));
    }
}
