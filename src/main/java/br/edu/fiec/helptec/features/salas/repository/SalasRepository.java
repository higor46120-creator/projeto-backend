package br.edu.fiec.helptec.features.salas.repository;

import br.edu.fiec.helptec.features.salas.Salas; // ✅ DEVE SER ESTE
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalasRepository extends MongoRepository<Salas, Long>, SalasRepositoryCustom {
}