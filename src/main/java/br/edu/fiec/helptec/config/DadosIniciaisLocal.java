package br.edu.fiec.helptec.config;

import br.edu.fiec.helptec.features.equipamento.Equipamento;
import br.edu.fiec.helptec.features.equipamento.repository.EquipamentoRepository;
import br.edu.fiec.helptec.features.salas.Salas;
import br.edu.fiec.helptec.features.salas.repository.SalasRepository;
import br.edu.fiec.helptec.features.skill.Skill;
import br.edu.fiec.helptec.features.skill.repository.SkillRepository;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Popula o banco LOCAL na primeira execução, para dar para testar sem cadastrar nada à mão.
 * Só roda com app.seed.enabled=true (ligado no perfil "local", desligado no "professor").
 * Cada parte só é criada se a collection estiver vazia: rodar de novo não duplica nada.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DadosIniciaisLocal implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciaisLocal.class);
    private static final String AREA_TESTE = "Informática";

    private final UsuarioRepository usuarioRepository;
    private final SalasRepository salasRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.senha:helptec123}")
    private String senhaTeste;

    @Value("${app.seed.google-admin-email:}")
    private String googleAdminEmail;

    public DadosIniciaisLocal(UsuarioRepository usuarioRepository,
                              SalasRepository salasRepository,
                              EquipamentoRepository equipamentoRepository,
                              SkillRepository skillRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.salasRepository = salasRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        criarUsuariosDeTeste();
        liberarAdminGoogle();
        criarSalas();
        criarEquipamentos();
        criarSkills();
    }

    /** Um usuário por papel, todos da mesma área (o aprovador vê os chamados da sua área). */
    private void criarUsuariosDeTeste() {
        if (usuarioRepository.count() > 0) return;

        List<UsuarioEntity> usuarios = List.of(
                usuario("Usuário Teste", "usuario@helptec.local", UserRole.USER),
                usuario("Aprovador Teste", "aprovador@helptec.local", UserRole.APROVADOR),
                usuario("Gerente Teste", "gerente@helptec.local", UserRole.GERENTE),
                usuario("Suporte Teste", "suporte@helptec.local", UserRole.SUPORTE),
                usuario("Admin Teste", "admin@helptec.local", UserRole.ADMIN)
        );
        usuarioRepository.saveAll(usuarios);
        log.info("[seed] {} usuários de teste criados (senha: {}). Ex.: admin@helptec.local",
                usuarios.size(), senhaTeste);
    }

    /** O login Google só aceita e-mails cadastrados: libera o e-mail informado como ADMIN. */
    private void liberarAdminGoogle() {
        if (googleAdminEmail == null || googleAdminEmail.isBlank()) return;
        String email = googleAdminEmail.trim();

        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .map(u -> (UsuarioEntity) u)
                .orElseGet(() -> {
                    UsuarioEntity novo = new UsuarioEntity();
                    novo.setNome(email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length()));
                    novo.setEmail(email);
                    // Senha aleatória: esta conta entra pelo Google
                    novo.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    novo.setArea(AREA_TESTE);
                    return novo;
                });
        if (usuario.getRole() != UserRole.ADMIN) {
            usuario.setRole(UserRole.ADMIN);
            usuarioRepository.save(usuario);
            log.info("[seed] {} liberado como ADMIN para o login com Google", email);
        }
    }

    private void criarSalas() {
        if (salasRepository.count() > 0) return;
        List<String> nomes = List.of("Laboratório 1", "Laboratório 2", "Laboratório 3",
                "Sala 101", "Sala 102", "Biblioteca", "Secretaria", "Sala dos Professores");
        salasRepository.saveAll(nomes.stream().map(n -> Salas.builder().nome(n).build()).toList());
        log.info("[seed] {} salas criadas", nomes.size());
    }

    private void criarEquipamentos() {
        if (equipamentoRepository.count() > 0) return;
        List<Equipamento> equipamentos = List.of(
                equipamento("Computador Dell OptiPlex", "PAT-0001", "COMPUTADOR", 3500.0),
                equipamento("Computador Dell OptiPlex", "PAT-0002", "COMPUTADOR", 3500.0),
                equipamento("Notebook Lenovo ThinkPad", "PAT-0003", "NOTEBOOK", 4200.0),
                equipamento("Projetor Epson", "PAT-0004", "PROJETOR", 2800.0),
                equipamento("Impressora HP LaserJet", "PAT-0005", "IMPRESSORA", 1900.0),
                equipamento("Roteador TP-Link", "PAT-0006", "REDE", 450.0)
        );
        equipamentoRepository.saveAll(equipamentos);
        log.info("[seed] {} equipamentos criados", equipamentos.size());
    }

    private void criarSkills() {
        if (skillRepository.count() > 0) return;
        List<String> nomes = List.of("HARDWARE", "SOFTWARE", "REDE", "IMPRESSORA");
        skillRepository.saveAll(nomes.stream().map(n -> {
            Skill s = new Skill();
            s.setNome(n);
            return s;
        }).toList());
        log.info("[seed] {} skills criadas", nomes.size());
    }

    private UsuarioEntity usuario(String nome, String email, UserRole role) {
        return new UsuarioEntity(nome, email, passwordEncoder.encode(senhaTeste), null, role, AREA_TESTE);
    }

    private Equipamento equipamento(String nome, String patrimonio, String tipo, Double preco) {
        return Equipamento.builder()
                .nome(nome)
                .patrimonio(patrimonio)
                .status("ATIVO")
                .tipo(tipo)
                .preco(preco)
                .build();
    }
}
