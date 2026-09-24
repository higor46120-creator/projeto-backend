package br.edu.fiec.helptec.features.chamado.repository;

import br.edu.fiec.helptec.features.chamado.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long>, ChamadoRepositoryCustom {
}
