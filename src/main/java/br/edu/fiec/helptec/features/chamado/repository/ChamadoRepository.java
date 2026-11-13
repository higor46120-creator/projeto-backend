package br.edu.fiec.helptec.features.chamado.repository;

import br.edu.fiec.helptec.features.chamado.Chamado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChamadoRepository extends MongoRepository<Chamado, Long>, ChamadoRepositoryCustom {
}
