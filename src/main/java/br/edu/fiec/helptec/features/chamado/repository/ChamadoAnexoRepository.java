package br.edu.fiec.helptec.features.chamado.repository;

import br.edu.fiec.helptec.features.chamado.ChamadoAnexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoAnexoRepository extends JpaRepository<ChamadoAnexo, Long> {
    List<ChamadoAnexo> findByIdChamado(Long idChamado);
}