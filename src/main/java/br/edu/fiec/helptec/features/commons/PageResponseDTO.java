package br.edu.fiec.helptec.features.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO<T> {
    private List<T> itens;
    private long totalItens;
    private int paginaAtual;
    private int totalPaginas;
}
