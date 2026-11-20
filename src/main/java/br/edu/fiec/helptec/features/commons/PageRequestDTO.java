package br.edu.fiec.helptec.features.commons;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDTO {

    private static final int TAMANHO_PADRAO = 10;

    private int page;
    private int size;

    /** Paginação recebida ou o padrão (página 0, 10 itens) quando ausente/inválida. */
    public static PageRequestDTO ouPadrao(PageRequestDTO pageRequest) {
        if (pageRequest == null) {
            return new PageRequestDTO(0, TAMANHO_PADRAO);
        }
        return new PageRequestDTO(Math.max(pageRequest.getPage(), 0),
                pageRequest.getSize() > 0 ? pageRequest.getSize() : TAMANHO_PADRAO);
    }

    public String getSortBy() {
        return "";
    }

    public int getPageNum() {
        return page;
    }

    public int getPageSize() {
        return size;
    }

    public String getSortOrder() {
        return "";
    }
}
