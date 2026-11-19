package br.edu.fiec.helptec.features.commons;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PageRequestDTO.ouPadrao")
class PageRequestDTOTest {

    @Test
    @DisplayName("sem paginação usa página 0 com 10 itens")
    void nuloViraPadrao() {
        //impl
        PageRequestDTO resultado = PageRequestDTO.ouPadrao(null);

        //asserts
        assertEquals(0, resultado.getPage());
        assertEquals(10, resultado.getSize());
    }

    @Test
    @DisplayName("tamanho zero/negativo vira 10 e página negativa vira 0")
    void corrigeValoresInvalidos() {
        //impl
        PageRequestDTO resultado = PageRequestDTO.ouPadrao(new PageRequestDTO(-1, 0));

        //asserts
        assertEquals(0, resultado.getPage());
        assertEquals(10, resultado.getSize());
    }

    @Test
    @DisplayName("mantém a paginação válida recebida")
    void mantemValida() {
        //impl
        PageRequestDTO resultado = PageRequestDTO.ouPadrao(new PageRequestDTO(2, 25));

        //asserts
        assertEquals(2, resultado.getPage());
        assertEquals(25, resultado.getSize());
    }
}
