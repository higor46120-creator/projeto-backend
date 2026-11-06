package br.edu.fiec.helptec.features.equipamento.repository.impl;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.equipamento.Equipamento;
import br.edu.fiec.helptec.features.equipamento.model.dto.EquipamentoFilterDTO;
import br.edu.fiec.helptec.features.equipamento.repository.EquipamentoRepositoryCustom; // Verifique o pacote correto da interface
import org.springframework.data.domain.Page;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EquipamentoRepositoryCustomImpl implements EquipamentoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Equipamento> buscarGenerica(EquipamentoFilterDTO filtro, PageRequestDTO pageRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 1. Query Principal de Dados
        CriteriaQuery<Equipamento> query = cb.createQuery(Equipamento.class);
        Root<Equipamento> root = query.from(Equipamento.class);

        Predicate[] predicates = criarPredicados(cb, root, filtro);
        query.where(predicates);

        // Ordenação via PageRequestDTO
        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().trim().isEmpty()) {
            Path<Object> sortPath = root.get(pageRequest.getSortBy());
            if ("desc".equalsIgnoreCase(pageRequest.getSortOrder())) {
                query.orderBy(cb.desc(sortPath));
            } else {
                query.orderBy(cb.asc(sortPath));
            }
        }

        TypedQuery<Equipamento> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageRequest.getPageNum() * pageRequest.getPageSize());
        typedQuery.setMaxResults(pageRequest.getPageSize());

        List<Equipamento> result = typedQuery.getResultList();

        // 2. Query de Contagem (Total)
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Equipamento> countRoot = countQuery.from(Equipamento.class);

        Predicate[] countPredicates = criarPredicados(cb, countRoot, filtro);
        countQuery.select(cb.count(countRoot)).where(countPredicates);

        Long totalItens = entityManager.createQuery(countQuery).getSingleResult();

        // Montagem do Pageable do Spring Data
        Sort.Direction direction = "desc".equalsIgnoreCase(pageRequest.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = (pageRequest.getSortBy() != null && !pageRequest.getSortBy().trim().isEmpty()) ? pageRequest.getSortBy() : "id";

        Pageable pageable = PageRequest.of(pageRequest.getPageNum(), pageRequest.getPageSize(), Sort.by(direction, sortBy));

        return new PageImpl<>(result, pageable, totalItens);
    }

    private Predicate[] criarPredicados(CriteriaBuilder cb, Root<Equipamento> root, EquipamentoFilterDTO filtro) {
        List<Predicate> predicates = new ArrayList<>();

        if (filtro == null) {
            return new Predicate[0];
        }

        // Termo genérico
        if (filtro.getTermo() != null && !filtro.getTermo().trim().isEmpty()) {
            String pattern = "%" + filtro.getTermo().toLowerCase() + "%";
            Predicate nomeLike = cb.like(cb.lower(root.get("nome")), pattern);
            Predicate patrimonioLike = cb.like(cb.lower(root.get("patrimonio")), pattern);
            Predicate statusLike = cb.like(cb.lower(root.get("status")), pattern);
            predicates.add(cb.or(nomeLike, patrimonioLike, statusLike));
        }

        // Filtros específicos
        if (filtro.getNome() != null && !filtro.getNome().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("nome")), "%" + filtro.getNome().toLowerCase() + "%"));
        }

        if (filtro.getPatrimonio() != null && !filtro.getPatrimonio().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("patrimonio")), "%" + filtro.getPatrimonio().toLowerCase() + "%"));
        }

        if (filtro.getStatus() != null && !filtro.getStatus().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("status")), "%" + filtro.getStatus().toLowerCase() + "%"));
        }

        if (filtro.getTipo() != null && !filtro.getTipo().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("tipo")), "%" + filtro.getTipo().toLowerCase() + "%"));
        }

        if (filtro.getPrecoMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("preco"), filtro.getPrecoMin()));
        }

        if (filtro.getPrecoMax() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("preco"), filtro.getPrecoMax()));
        }

        return predicates.toArray(new Predicate[0]);
    }
}
