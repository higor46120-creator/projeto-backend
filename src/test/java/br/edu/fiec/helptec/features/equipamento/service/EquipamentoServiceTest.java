package br.edu.fiec.helptec.features.equipamento.service;

import br.edu.fiec.helptec.features.equipamento.Equipamento;
import br.edu.fiec.helptec.features.equipamento.model.dto.EquipamentoDTO;
import br.edu.fiec.helptec.features.equipamento.repository.EquipamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipamentoService")
class EquipamentoServiceTest {

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @InjectMocks
    private EquipamentoService equipamentoService;

    private static Equipamento equipamento(Long id) {
        return new Equipamento(id, "Projetor Epson", "PAT-0004", "ATIVO", "PROJETOR", 2800.0);
    }

    @Test
    @DisplayName("deve criar equipamento com id gerado pelo banco")
    void deveCriarEquipamento() {
        //mocks
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> {
            Equipamento e = inv.getArgument(0);
            assertNull(e.getId(), "o id deve ser gerado pelo banco");
            e.setId(7L);
            return e;
        });
        EquipamentoDTO dto = new EquipamentoDTO(55L, "Notebook", "PAT-9", "ATIVO", "NOTEBOOK", 4200.0);

        //impl
        EquipamentoDTO resultado = equipamentoService.criar(dto);

        //asserts
        assertEquals(Long.valueOf(7L), resultado.getId());
        assertEquals("PAT-9", resultado.getPatrimonio());
    }

    @Test
    @DisplayName("deve atualizar todos os campos do equipamento")
    void deveAtualizarEquipamento() {
        //mocks
        when(equipamentoRepository.findById(4L)).thenReturn(Optional.of(equipamento(4L)));
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(inv -> inv.getArgument(0));
        EquipamentoDTO novo = new EquipamentoDTO(null, "Projetor BenQ", "PAT-0040", "MANUTENCAO", "PROJETOR", 3100.0);

        //impl
        EquipamentoDTO resultado = equipamentoService.atualizar(4L, novo);

        //asserts
        assertAll(
                () -> assertEquals(Long.valueOf(4L), resultado.getId()),
                () -> assertEquals("Projetor BenQ", resultado.getNome()),
                () -> assertEquals("PAT-0040", resultado.getPatrimonio()),
                () -> assertEquals("MANUTENCAO", resultado.getStatus()),
                () -> assertEquals("PROJETOR", resultado.getTipo()),
                () -> assertEquals(Double.valueOf(3100.0), resultado.getPreco())
        );
    }

    @Test
    @DisplayName("deve falhar ao buscar equipamento inexistente")
    void naoDeveBuscarEquipamentoInexistente() {
        //mocks
        when(equipamentoRepository.findById(9L)).thenReturn(Optional.empty());

        //impl
        RuntimeException erro = assertThrows(RuntimeException.class, () -> equipamentoService.buscarPorId(9L));

        //asserts
        assertEquals("Equipamento não encontrado", erro.getMessage());
    }

    @Test
    @DisplayName("não deve excluir equipamento inexistente")
    void naoDeveDeletarEquipamentoInexistente() {
        //mocks
        when(equipamentoRepository.existsById(9L)).thenReturn(false);

        //impl + asserts
        assertThrows(RuntimeException.class, () -> equipamentoService.deletar(9L));
        verify(equipamentoRepository, never()).deleteById(any());
    }
}
