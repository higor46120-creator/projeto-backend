package br.edu.fiec.helptec.features.salas.service;

import lombok.RequiredArgsConstructor;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.salas.Salas;
import br.edu.fiec.helptec.features.salas.model.dto.SalasDTO;
import br.edu.fiec.helptec.features.salas.model.dto.SalasFilterDTO;
import br.edu.fiec.helptec.features.salas.repository.SalasRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SalasService {

    private final SalasRepository salasRepository;

    public PageResponseDTO<SalasDTO> buscarComFiltro(SalasFilterDTO filtro, PageRequestDTO pageRequest) {
        Page<Salas> page = salasRepository.buscarGenerica(filtro, pageRequest);

        List<SalasDTO> dtos = page.getContent().stream().map(this::toDTO).toList();

        return new PageResponseDTO<>(
                dtos,
                page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages()
        );
    }

    public SalasDTO buscarPorId(Long id) {
        Salas sala = salasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
        return toDTO(sala);
    }

    public SalasDTO criar(SalasDTO dto) {
        Salas sala = toEntity(dto);
        sala.setIdSala(null);
        Salas salva = salasRepository.save(sala);
        return toDTO(salva);
    }

    public SalasDTO atualizar(Long id, SalasDTO dto) {
        Salas existente = salasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        existente.setNome(dto.getNome());

        Salas atualizada = salasRepository.save(existente);
        return toDTO(atualizada);
    }

    public void deletar(Long id) {
        if (!salasRepository.existsById(id)) {
            throw new RuntimeException("Sala não encontrada");
        }
        salasRepository.deleteById(id);
    }

    private SalasDTO toDTO(Salas sala) {
        return new SalasDTO(sala.getIdSala(), sala.getNome());
    }

    private Salas toEntity(SalasDTO dto) {
        return new Salas(dto.getIdSala(), dto.getNome());
    }
}