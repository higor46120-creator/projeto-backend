package br.edu.fiec.helptec.features.equipamento.repository;

import br.edu.fiec.helptec.features.equipamento.Equipamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipamentoRepository extends MongoRepository<Equipamento, Long>, EquipamentoRepositoryCustom {
}
