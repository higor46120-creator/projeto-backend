package br.edu.fiec.helptec.features.usuario.service;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.CreateUsuarioRequestDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioCsvResultadoDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.UUID;

public interface UsuarioService extends UserDetailsService {
    UsuarioResponseDTO criar(CreateUsuarioRequestDTO request);
    List<UsuarioResponseDTO> listarTodos();
    PageResponseDTO<UsuarioResponseDTO> listarPaginado(PageRequestDTO pageRequest);
    UsuarioResponseDTO buscarPorId(UUID id);
    UsuarioResponseDTO atualizar(UUID id, CreateUsuarioRequestDTO request);
    void deletar(UUID id);
    UsuarioCsvResultadoDTO importarCsv(MultipartFile file) throws IOException;
}