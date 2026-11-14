package br.edu.fiec.helptec.features.commons;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilitários para as buscas filtradas e paginadas no MongoDB
 * (substituem o CriteriaBuilder do JPA nos *RepositoryCustomImpl).
 */
public final class MongoPaginacao {

    private static final int TAMANHO_PADRAO = 20;

    private MongoPaginacao() {
    }

    /** Equivalente ao LIKE '%texto%' sem diferenciar maiúsculas/minúsculas. */
    public static Criteria contem(String campo, String texto) {
        return Criteria.where(campo).regex(Pattern.quote(texto.trim()), "i");
    }

    public static boolean preenchido(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    /**
     * Executa a consulta com os filtros já aplicados, contando o total e devolvendo a página pedida.
     *
     * @param ordenacaoPadrao campo usado quando o PageRequestDTO não informa ordenação
     */
    public static <T> Page<T> paginar(MongoTemplate mongoTemplate, Query query, PageRequestDTO pageRequest,
                                      Class<T> tipo, String ordenacaoPadrao) {
        int pagina = Math.max(pageRequest.getPageNum(), 0);
        int tamanho = pageRequest.getPageSize() > 0 ? pageRequest.getPageSize() : TAMANHO_PADRAO;

        Sort.Direction direcao = "desc".equalsIgnoreCase(pageRequest.getSortOrder())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String campoOrdenacao = preenchido(pageRequest.getSortBy()) ? pageRequest.getSortBy() : ordenacaoPadrao;

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by(direcao, campoOrdenacao));

        // Conta antes de aplicar página/ordenação, depois busca só a página pedida
        long total = mongoTemplate.count(query, tipo);
        List<T> itens = mongoTemplate.find(query.with(pageable), tipo);

        return new PageImpl<>(itens, pageable, total);
    }
}
