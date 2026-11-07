package br.edu.fiec.helptec.config.mongo;

import org.springframework.core.Ordered;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Antes de salvar, preenche os campos anotados com {@link GeneratedId} que ainda estão nulos.
 * Roda depois da auditoria (ordem maior), para que o documento ainda seja
 * reconhecido como "novo" e receba createdDate/createdBy.
 */
@Component
public class GeneratedIdCallback implements BeforeConvertCallback<Object>, Ordered {

    private final SequenceGeneratorService sequenceGenerator;

    public GeneratedIdCallback(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Object onBeforeConvert(Object entity, String collection) {
        ReflectionUtils.doWithFields(entity.getClass(), field -> preencher(entity, collection, field),
                field -> field.isAnnotationPresent(GeneratedId.class));
        return entity;
    }

    private void preencher(Object entity, String collection, Field field) {
        ReflectionUtils.makeAccessible(field);
        if (ReflectionUtils.getField(field, entity) != null) {
            return; // já tem ID: é uma atualização
        }

        if (Long.class.equals(field.getType())) {
            ReflectionUtils.setField(field, entity, sequenceGenerator.proximoValor(collection));
        } else if (UUID.class.equals(field.getType())) {
            ReflectionUtils.setField(field, entity, UUID.randomUUID());
        } else {
            throw new IllegalStateException("@GeneratedId só suporta Long ou UUID: " + field);
        }
    }

    @Override
    public int getOrder() {
        // A auditoria do Spring Data usa ordem 100; este callback roda depois dela.
        return 200;
    }
}
