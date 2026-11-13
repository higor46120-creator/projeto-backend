package br.edu.fiec.helptec.features.equipamento.repository.impl;

import br.edu.fiec.helptec.features.commons.MongoPaginacao;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.equipamento.Equipamento;
import br.edu.fiec.helptec.features.equipamento.model.dto.EquipamentoFilterDTO;
import br.edu.fiec.helptec.features.equipamento.repository.EquipamentoRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static br.edu.fiec.helptec.features.commons.MongoPaginacao.contem;
import static br.edu.fiec.helptec.features.commons.MongoPaginacao.preenchido;

@Repository
public class EquipamentoRepositoryCustomImpl implements EquipamentoRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public EquipamentoRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Equipamento> buscarGenerica(EquipamentoFilterDTO filtro, PageRequestDTO pageRequest) {
        List<Criteria> filtros = new ArrayList<>();

        if (filtro != null) {
            // Busca geral: nome OU patrimônio OU status
            if (preenchido(filtro.getTermo())) {
                filtros.add(new Criteria().orOperator(
                        contem("nome", filtro.getTermo()),
                        contem("patrimonio", filtro.getTermo()),
                        contem("status", filtro.getTermo())
                ));
            }

            // Filtros específicos
            if (preenchido(filtro.getNome())) {
                filtros.add(contem("nome", filtro.getNome()));
            }
            if (preenchido(filtro.getPatrimonio())) {
                filtros.add(contem("patrimonio", filtro.getPatrimonio()));
            }
            if (preenchido(filtro.getStatus())) {
                filtros.add(contem("status", filtro.getStatus()));
            }
            if (preenchido(filtro.getTipo())) {
                filtros.add(contem("tipo", filtro.getTipo()));
            }

            // Faixa de preço (os dois limites no mesmo critério, como o Mongo exige)
            if (filtro.getPrecoMin() != null || filtro.getPrecoMax() != null) {
                Criteria preco = Criteria.where("preco");
                if (filtro.getPrecoMin() != null) {
                    preco = preco.gte(filtro.getPrecoMin());
                }
                if (filtro.getPrecoMax() != null) {
                    preco = preco.lte(filtro.getPrecoMax());
                }
                filtros.add(preco);
            }
        }

        Query query = new Query();
        if (!filtros.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filtros));
        }

        return MongoPaginacao.paginar(mongoTemplate, query, pageRequest, Equipamento.class, "id");
    }
}
