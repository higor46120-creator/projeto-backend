package br.edu.fiec.helptec.features.usuario.service.impl;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.CreateUsuarioRequestDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import br.edu.fiec.helptec.features.usuario.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioResponseDTO criar(CreateUsuarioRequestDTO request) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setNome(request.getNome());
        entity.setEmail(request.getEmail());
        entity.setPassword(request.getSenha());
        entity.setRole(UserRole.USER);

        UsuarioEntity salvo = usuarioRepository.save(entity);
        return toDTO(salvo);
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponseDTO<UsuarioResponseDTO> listarPaginado(PageRequestDTO request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<UsuarioEntity> page = usuarioRepository.findAll(pageable);

        List<UsuarioResponseDTO> conteudo = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                conteudo,
                page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages()
        );
    }

    @Override
    public UsuarioResponseDTO buscarPorId(UUID id) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));
        return toDTO(entity);
    }

    @Override
    public UsuarioResponseDTO atualizar(UUID id, CreateUsuarioRequestDTO request) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));

        entity.setNome(request.getNome());
        entity.setEmail(request.getEmail());
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            entity.setPassword(request.getSenha());
        }

        UsuarioEntity atualizado = usuarioRepository.save(entity);
        return toDTO(atualizado);
    }

    @Override
    public void deletar(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com o ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO toDTO(UsuarioEntity entity) {
        return new UsuarioResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getRole()
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // O "username" passado pelo Spring Security corresponde ao e-mail
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + username));
    }
}