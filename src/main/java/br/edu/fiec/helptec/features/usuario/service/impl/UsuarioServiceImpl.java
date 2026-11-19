package br.edu.fiec.helptec.features.usuario.service.impl;

import br.edu.fiec.helptec.features.commons.PageRequestDTO;
import br.edu.fiec.helptec.features.commons.PageResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.CreateUsuarioRequestDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioCsvDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioCsvResultadoDTO;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioCsvResultadoDTO.ErroLinha;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UserStatus;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import br.edu.fiec.helptec.features.usuario.service.UsuarioService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponseDTO criar(CreateUsuarioRequestDTO request) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setNome(request.getNome());
        entity.setEmail(request.getEmail());
        entity.setPassword(passwordEncoder.encode(request.getSenha()));
        entity.setRole(UserRole.USER);
        entity.setStatus(UserStatus.NOT_CONFIRMED);

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
            entity.setPassword(passwordEncoder.encode(request.getSenha()));
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

    // ---------------------------------------------------------
    // Importação em lote por CSV (somente SUPER_ADMIN, ver UsuarioController)
    // ---------------------------------------------------------
    @Override
    public UsuarioCsvResultadoDTO importarCsv(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo CSV vazio");
        }
        String conteudo = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (conteudo.startsWith("\uFEFF")) {
            conteudo = conteudo.substring(1); // BOM do Excel ("CSV UTF-8")
        }

        List<UsuarioCsvDTO> linhas;
        try {
            CsvToBean<UsuarioCsvDTO> csvToBean = new CsvToBeanBuilder<UsuarioCsvDTO>(new StringReader(conteudo))
                    .withType(UsuarioCsvDTO.class)
                    .withSeparator(separador(conteudo))
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            linhas = csvToBean.parse();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "CSV inválido. Cabeçalho esperado: nome,email,senha,perfil,area", e);
        }

        List<UsuarioEntity> novos = new ArrayList<>();
        List<ErroLinha> erros = new ArrayList<>();
        Set<String> emailsDoArquivo = new HashSet<>();

        for (int i = 0; i < linhas.size(); i++) {
            int numeroLinha = i + 2; // linha 1 = cabeçalho
            UsuarioCsvDTO linha = linhas.get(i);
            String email = texto(linha.getEmail()) == null ? null : linha.getEmail().trim().toLowerCase(Locale.ROOT);
            try {
                novos.add(validarLinha(linha, email, emailsDoArquivo));
                emailsDoArquivo.add(email);
            } catch (IllegalArgumentException e) {
                erros.add(new ErroLinha(numeroLinha, email, e.getMessage()));
            }
        }

        List<UsuarioResponseDTO> criados = usuarioRepository.saveAll(novos).stream()
                .map(this::toDTO)
                .toList();
        return new UsuarioCsvResultadoDTO(linhas.size(), criados, erros);
    }

    private UsuarioEntity validarLinha(UsuarioCsvDTO linha, String email, Set<String> emailsDoArquivo) {
        String nome = texto(linha.getNome());
        if (nome == null) {
            throw new IllegalArgumentException("nome é obrigatório");
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("e-mail inválido");
        }
        if (emailsDoArquivo.contains(email)) {
            throw new IllegalArgumentException("e-mail repetido no arquivo");
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("e-mail já cadastrado");
        }

        UsuarioEntity entity = new UsuarioEntity();
        entity.setNome(nome);
        entity.setEmail(email);
        entity.setArea(texto(linha.getArea()));
        entity.setRole(perfil(linha.getPerfil()));
        // Sem senha: senha aleatória, a pessoa entra pelo login com Google
        String senha = texto(linha.getSenha());
        entity.setPassword(passwordEncoder.encode(senha != null ? senha : UUID.randomUUID().toString()));
        entity.setStatus(UserStatus.NOT_CONFIRMED);
        return entity;
    }

    private static UserRole perfil(String valor) {
        String perfil = texto(valor);
        if (perfil == null) {
            return UserRole.USER;
        }
        UserRole role;
        try {
            role = UserRole.valueOf(perfil.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("perfil inválido: " + perfil
                    + " (use USER, APROVADOR, GERENTE, SUPORTE ou ADMIN)");
        }
        if (role == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("perfil SUPER_ADMIN não pode ser criado por CSV");
        }
        return role;
    }

    /** Excel em português salva o CSV com ";" em vez de ",". */
    private static char separador(String conteudo) {
        int fimDaLinha = conteudo.indexOf('\n');
        String cabecalho = fimDaLinha >= 0 ? conteudo.substring(0, fimDaLinha) : conteudo;
        return cabecalho.contains(";") && !cabecalho.contains(",") ? ';' : ',';
    }

    private static String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
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