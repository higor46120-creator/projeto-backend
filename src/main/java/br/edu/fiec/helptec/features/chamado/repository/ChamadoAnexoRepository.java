package br.edu.fiec.helptec.features.chamado.repository;

import br.edu.fiec.helptec.features.chamado.ChamadoAnexo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoAnexoRepository extends MongoRepository<ChamadoAnexo, Long> {
    List<ChamadoAnexo> findByIdChamado(Long idChamado);
}