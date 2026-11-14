package br.edu.fiec.helptec.features.chamado.repository.impl;

import br.edu.fiec.helptec.features.chamado.Chamado;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoFilterDTO;
import br.edu.fiec.helptec.features.chamado.repository.ChamadoRepositoryCustom;
import br.edu.fiec.helptec.features.commons.MongoPaginacao;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static br.edu.fiec.helptec.features.commons.MongoPaginacao.contem;
import static br.edu.fiec.helptec.features.commons.MongoPaginacao.preenchido;

@Repository
public class ChamadoRepositoryCustomImpl implements ChamadoRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public ChamadoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Chamado> buscarGenerica(ChamadoFilterDTO filtro, PageRequestDTO pageRequest) {
        Query query = new Query();

        if (filtro != null) {
            if (preenchido(filtro.getTermo())) {
                query.addCriteria(contem("descricao", filtro.getTermo()));
            }
            if (filtro.getStatus() != null) {
                query.addCriteria(Criteria.where("status").is(filtro.getStatus()));
            }
            if (preenchido(filtro.getPrioridade())) {
                query.addCriteria(Criteria.where("prioridade").is(filtro.getPrioridade()));
            }
            if (preenchido(filtro.getCriticidade())) {
                query.addCriteria(Criteria.where("criticidade").is(filtro.getCriticidade()));
            }
            if (filtro.getIdUsuario() != null) {
                query.addCriteria(Criteria.where("idUsuario").is(filtro.getIdUsuario()));
            }
            if (filtro.getIdEquipamento() != null) {
                query.addCriteria(Criteria.where("idEquipamento").is(filtro.getIdEquipamento()));
            }
            if (filtro.getIdSala() != null) {
                query.addCriteria(Criteria.where("idSala").is(filtro.getIdSala()));
            }
        }

        return MongoPaginacao.paginar(mongoTemplate, query, pageRequest, Chamado.class, "idChamado");
    }
}
