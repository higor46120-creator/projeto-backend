package br.edu.fiec.helptec.features.equipamento.service;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.equipamento.Equipamento; // Mude para .model.Equipamento se mover o arquivo
import br.edu.fiec.helptec.features.equipamento.model.dto.EquipamentoDTO;
import br.edu.fiec.helptec.features.equipamento.model.dto.EquipamentoFilterDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.equipamento.repository.EquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipamentoService {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    // READ (Busca Genérica Paginada)
    public PageResponseDTO<EquipamentoDTO> buscarComFiltro(EquipamentoFilterDTO filtro, PageRequestDTO pageRequest) {
        Page<Equipamento> page = equipamentoRepository.buscarGenerica(filtro, pageRequest);

        List<EquipamentoDTO> dtos = page.getContent().stream().map(this::toDTO).toList();

        return new PageResponseDTO<>(
                dtos,
                page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages()
        );
    }

    // READ (Buscar por ID)
    public EquipamentoDTO buscarPorId(Long id) {
        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipamento não encontrado"));
        return toDTO(equipamento);
    }

    // CREATE
    public EquipamentoDTO criar(EquipamentoDTO dto) {
        Equipamento equipamento = toEntity(dto);
        equipamento.setId(null);
        Equipamento salvo = equipamentoRepository.save(equipamento);
        return toDTO(salvo);
    }

    // UPDATE
    public EquipamentoDTO atualizar(Long id, EquipamentoDTO dto) {
        Equipamento existente = equipamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipamento não encontrado"));

        existente.setNome(dto.getNome());
        existente.setPatrimonio(dto.getPatrimonio());
        existente.setStatus(dto.getStatus());
        existente.setTipo(dto.getTipo());
        existente.setPreco(dto.getPreco());

        Equipamento atualizado = equipamentoRepository.save(existente);
        return toDTO(atualizado);
    }

    // DELETE
    public void deletar(Long id) {
        if (!equipamentoRepository.existsById(id)) {
            throw new RuntimeException("Equipamento não encontrado");
        }
        equipamentoRepository.deleteById(id);
    }

    // METODOS AUXILIARES DE CONVERSÃO (Faltando na sua classe)
    private EquipamentoDTO toDTO(Equipamento e) {
        return new EquipamentoDTO(e.getId(), e.getNome(), e.getPatrimonio(), e.getStatus(), e.getTipo(), e.getPreco());
    }

    private Equipamento toEntity(EquipamentoDTO dto) {
        return new Equipamento(dto.getId(), dto.getNome(), dto.getPatrimonio(), dto.getStatus(), dto.getTipo(), dto.getPreco());
    }
}