package br.edu.fiec.helptec.features.salas.service;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.salas.Salas;
import br.edu.fiec.helptec.features.salas.model.dto.SalasDTO;
import br.edu.fiec.helptec.features.salas.model.dto.SalasFilterDTO;
import br.edu.fiec.helptec.features.salas.repository.SalasRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalasService")
class SalasServiceTest {

    @Mock
    private SalasRepository salasRepository;

    @InjectMocks
    private SalasService salasService;

    @Test
    @DisplayName("deve criar sala ignorando o id enviado pelo cliente")
    void deveCriarSala() {
        //mocks
        when(salasRepository.save(any(Salas.class))).thenAnswer(inv -> {
            Salas sala = inv.getArgument(0);
            assertNull(sala.getIdSala(), "o id deve ser gerado pelo banco");
            sala.setIdSala(10L);
            return sala;
        });

        //impl
        SalasDTO resultado = salasService.criar(new SalasDTO(99L, "Laboratório 1"));

        //asserts
        assertEquals(Long.valueOf(10L), resultado.getIdSala());
        assertEquals("Laboratório 1", resultado.getNome());
    }

    @Test
    @DisplayName("deve buscar sala pelo id")
    void deveBuscarSalaPorId() {
        //mocks
        when(salasRepository.findById(1L)).thenReturn(Optional.of(new Salas(1L, "Sala 101")));

        //impl
        SalasDTO resultado = salasService.buscarPorId(1L);

        //asserts
        assertEquals("Sala 101", resultado.getNome());
    }

    @Test
    @DisplayName("deve falhar ao buscar sala inexistente")
    void naoDeveBuscarSalaInexistente() {
        //mocks
        when(salasRepository.findById(1L)).thenReturn(Optional.empty());

        //impl
        RuntimeException erro = assertThrows(RuntimeException.class, () -> salasService.buscarPorId(1L));

        //asserts
        assertEquals("Sala não encontrada", erro.getMessage());
    }

    @Test
    @DisplayName("deve atualizar o nome da sala")
    void deveAtualizarSala() {
        //mocks
        when(salasRepository.findById(1L)).thenReturn(Optional.of(new Salas(1L, "Sala antiga")));
        when(salasRepository.save(any(Salas.class))).thenAnswer(inv -> inv.getArgument(0));

        //impl
        SalasDTO resultado = salasService.atualizar(1L, new SalasDTO(null, "Sala nova"));

        //asserts
        assertEquals(Long.valueOf(1L), resultado.getIdSala());
        assertEquals("Sala nova", resultado.getNome());
    }

    @Test
    @DisplayName("não deve atualizar sala inexistente")
    void naoDeveAtualizarSalaInexistente() {
        //mocks
        when(salasRepository.findById(1L)).thenReturn(Optional.empty());

        //impl + asserts
        assertThrows(RuntimeException.class, () -> salasService.atualizar(1L, new SalasDTO(null, "X")));
        verify(salasRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve excluir sala existente e recusar inexistente")
    void deveDeletarSomenteSalaExistente() {
        //mocks
        when(salasRepository.existsById(1L)).thenReturn(true);
        when(salasRepository.existsById(2L)).thenReturn(false);

        //impl
        salasService.deletar(1L);

        //asserts
        verify(salasRepository).deleteById(1L);
        assertThrows(RuntimeException.class, () -> salasService.deletar(2L));
        verify(salasRepository, never()).deleteById(2L);
    }

    @Test
    @DisplayName("deve paginar a busca de salas")
    void deveBuscarComFiltro() {
        //mocks
        SalasFilterDTO filtro = new SalasFilterDTO();
        PageRequestDTO pagina = new PageRequestDTO(1, 1);
        when(salasRepository.buscarGenerica(filtro, pagina))
                .thenReturn(new PageImpl<>(List.of(new Salas(2L, "Biblioteca")), PageRequest.of(1, 1), 3));

        //impl
        PageResponseDTO<SalasDTO> resultado = salasService.buscarComFiltro(filtro, pagina);

        //asserts
        assertEquals(1, resultado.getItens().size());
        assertEquals(3, resultado.getTotalItens());
        assertEquals(1, resultado.getPaginaAtual());
        assertEquals(3, resultado.getTotalPaginas());
    }
}
