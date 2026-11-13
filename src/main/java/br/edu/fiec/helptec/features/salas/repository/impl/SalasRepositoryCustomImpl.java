package br.edu.fiec.helptec.features.salas.repository.impl;

import br.edu.fiec.helptec.features.commons.MongoPaginacao;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.salas.Salas;
import br.edu.fiec.helptec.features.salas.model.dto.SalasFilterDTO;
import br.edu.fiec.helptec.features.salas.repository.SalasRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static br.edu.fiec.helptec.features.commons.MongoPaginacao.contem;
import static br.edu.fiec.helptec.features.commons.MongoPaginacao.preenchido;

@Repository
public class SalasRepositoryCustomImpl implements SalasRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public SalasRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Salas> buscarGenerica(SalasFilterDTO filtro, PageRequestDTO pageRequest) {
        Query query = new Query();

        if (filtro != null) {
            if (preenchido(filtro.getTermo())) {
                query.addCriteria(contem("nome", filtro.getTermo()));
            }
            if (preenchido(filtro.getNome()) && !preenchido(filtro.getTermo())) {
                query.addCriteria(contem("nome", filtro.getNome()));
            }
        }

        return MongoPaginacao.paginar(mongoTemplate, query, pageRequest, Salas.class, "idSala");
    }
}
