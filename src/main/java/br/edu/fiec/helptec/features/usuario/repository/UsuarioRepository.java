package br.edu.fiec.helptec.features.usuario.repository;

import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends MongoRepository<UsuarioEntity, UUID> {

    Optional<UserDetails> findByEmail(String email);

    List<UsuarioEntity> findByArea(String area);

}