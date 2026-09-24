package br.edu.fiec.helptec.features.chamado.repository.impl;

import br.edu.fiec.helptec.features.chamado.Chamado;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoFilterDTO;
import br.edu.fiec.helptec.features.chamado.repository.ChamadoRepositoryCustom;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ChamadoRepositoryCustomImpl implements ChamadoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Chamado> buscarGenerica(ChamadoFilterDTO filtro, PageRequestDTO pageRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Chamado> query = cb.createQuery(Chamado.class);
        Root<Chamado> root = query.from(Chamado.class);

        Predicate[] predicates = criarPredicados(cb, root, filtro);
        query.where(predicates);

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().trim().isEmpty()) {
            Path<Object> sortPath = root.get(pageRequest.getSortBy());
            if ("desc".equalsIgnoreCase(pageRequest.getSortOrder())) {
                query.orderBy(cb.desc(sortPath));
            } else {
                query.orderBy(cb.asc(sortPath));
            }
        }

        TypedQuery<Chamado> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageRequest.getPageNum() * pageRequest.getPageSize());
        typedQuery.setMaxResults(pageRequest.getPageSize());

        List<Chamado> result = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Chamado> countRoot = countQuery.from(Chamado.class);

        Predicate[] countPredicates = criarPredicados(cb, countRoot, filtro);
        countQuery.select(cb.count(countRoot)).where(countPredicates);

        Long totalItens = entityManager.createQuery(countQuery).getSingleResult();

        Sort.Direction direction = "desc".equalsIgnoreCase(pageRequest.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = (pageRequest.getSortBy() != null && !pageRequest.getSortBy().trim().isEmpty()) ? pageRequest.getSortBy() : "idChamado";

        Pageable pageable = PageRequest.of(pageRequest.getPageNum(), pageRequest.getPageSize(), Sort.by(direction, sortBy));

        return new PageImpl<>(result, pageable, totalItens);
    }

    private Predicate[] criarPredicados(CriteriaBuilder cb, Root<Chamado> root, ChamadoFilterDTO filtro) {
        List<Predicate> predicates = new ArrayList<>();

        if (filtro == null) {
            return new Predicate[0];
        }

        if (filtro.getTermo() != null && !filtro.getTermo().trim().isEmpty()) {
            String pattern = "%" + filtro.getTermo().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("descricao")), pattern));
        }

        if (filtro.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filtro.getStatus()));
        }

        if (filtro.getPrioridade() != null && !filtro.getPrioridade().trim().isEmpty()) {
            predicates.add(cb.equal(root.get("prioridade"), filtro.getPrioridade()));
        }

        if (filtro.getCriticidade() != null && !filtro.getCriticidade().trim().isEmpty()) {
            predicates.add(cb.equal(root.get("criticidade"), filtro.getCriticidade()));
        }

        if (filtro.getIdUsuario() != null) {
            predicates.add(cb.equal(root.get("idUsuario"), filtro.getIdUsuario()));
        }

        if (filtro.getIdEquipamento() != null) {
            predicates.add(cb.equal(root.get("idEquipamento"), filtro.getIdEquipamento()));
        }

        if (filtro.getIdSala() != null) {
            predicates.add(cb.equal(root.get("idSala"), filtro.getIdSala()));
        }

        return predicates.toArray(new Predicate[0]);
    }
}
