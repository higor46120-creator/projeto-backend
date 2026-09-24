package br.edu.fiec.helptec.features.chamado.repository;

import br.edu.fiec.helptec.features.chamado.Chamado;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoFilterDTO;
import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import org.springframework.data.domain.Page;

public interface ChamadoRepositoryCustom {
    Page<Chamado> buscarGenerica(ChamadoFilterDTO filtro, PageRequestDTO pageRequest);
}
