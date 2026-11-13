package br.edu.fiec.helptec.features.skill.service;

import br.edu.fiec.helptec.features.skill.Skill;
import br.edu.fiec.helptec.features.skill.UsuarioSkill;
import br.edu.fiec.helptec.features.skill.model.dto.SkillDTO;
import br.edu.fiec.helptec.features.skill.repository.SkillRepository;
import br.edu.fiec.helptec.features.skill.repository.UsuarioSkillRepository;
import br.edu.fiec.helptec.features.usuario.model.dto.UsuarioResponseDTO;
import br.edu.fiec.helptec.features.usuario.model.entity.UserRole;
import br.edu.fiec.helptec.features.usuario.model.entity.UsuarioEntity;
import br.edu.fiec.helptec.features.usuario.repository.UsuarioRepository;
import br.edu.fiec.helptec.support.Fabrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillService")
class SkillServiceTest {

    private static final Long ID_SKILL = 3L;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private UsuarioSkillRepository usuarioSkillRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SkillService skillService;

    private static UsuarioSkill vinculo(UUID idUsuario, Long idSkill) {
        UsuarioSkill us = new UsuarioSkill();
        us.setIdUsuario(idUsuario);
        us.setIdSkill(idSkill);
        return us;
    }

    @Test
    @DisplayName("deve atribuir skill a um usuário de suporte")
    void deveAtribuirSkillAoSuporte() {
        //mocks
        UsuarioEntity suporte = Fabrica.usuario(UserRole.SUPORTE);
        when(usuarioRepository.findById(suporte.getId())).thenReturn(Optional.of(suporte));
        when(skillRepository.existsById(ID_SKILL)).thenReturn(true);
        when(usuarioSkillRepository.existsByIdUsuarioAndIdSkill(suporte.getId(), ID_SKILL)).thenReturn(false);

        //impl
        skillService.atribuirSkill(suporte.getId(), ID_SKILL);

        //asserts
        ArgumentCaptor<UsuarioSkill> captor = ArgumentCaptor.forClass(UsuarioSkill.class);
        verify(usuarioSkillRepository).save(captor.capture());
        assertEquals(suporte.getId(), captor.getValue().getIdUsuario());
        assertEquals(ID_SKILL, captor.getValue().getIdSkill());
    }

    @Test
    @DisplayName("não deve duplicar uma skill já atribuída")
    void naoDeveDuplicarSkill() {
        //mocks
        UsuarioEntity suporte = Fabrica.usuario(UserRole.SUPORTE);
        when(usuarioRepository.findById(suporte.getId())).thenReturn(Optional.of(suporte));
        when(skillRepository.existsById(ID_SKILL)).thenReturn(true);
        when(usuarioSkillRepository.existsByIdUsuarioAndIdSkill(suporte.getId(), ID_SKILL)).thenReturn(true);

        //impl
        skillService.atribuirSkill(suporte.getId(), ID_SKILL);

        //asserts
        verify(usuarioSkillRepository, never()).save(any());
    }

    @Test
    @DisplayName("não deve atribuir skill a quem não é do suporte")
    void naoDeveAtribuirSkillParaQuemNaoESuporte() {
        //mocks
        UsuarioEntity usuario = Fabrica.usuario(UserRole.USER);
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        //impl
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> skillService.atribuirSkill(usuario.getId(), ID_SKILL));

        //asserts
        assertEquals("Somente usuários com papel SUPORTE podem receber skills", erro.getMessage());
        verify(usuarioSkillRepository, never()).save(any());
    }

    @Test
    @DisplayName("não deve atribuir skill inexistente")
    void naoDeveAtribuirSkillInexistente() {
        //mocks
        UsuarioEntity suporte = Fabrica.usuario(UserRole.SUPORTE);
        when(usuarioRepository.findById(suporte.getId())).thenReturn(Optional.of(suporte));
        when(skillRepository.existsById(ID_SKILL)).thenReturn(false);

        //impl + asserts
        RuntimeException erro = assertThrows(RuntimeException.class,
                () -> skillService.atribuirSkill(suporte.getId(), ID_SKILL));
        assertEquals("Skill não encontrada", erro.getMessage());
    }

    @Test
    @DisplayName("na triagem, deve listar só usuários de suporte com a skill")
    void deveListarSomenteSuportesComASkill() {
        //mocks
        UsuarioEntity suporte = Fabrica.usuario(UserRole.SUPORTE);
        UsuarioEntity exSuporte = Fabrica.usuario(UserRole.USER); // perdeu o papel, mas ficou o vínculo
        when(usuarioSkillRepository.findByIdSkill(ID_SKILL))
                .thenReturn(List.of(vinculo(suporte.getId(), ID_SKILL), vinculo(exSuporte.getId(), ID_SKILL)));
        when(usuarioRepository.findAllById(List.of(suporte.getId(), exSuporte.getId())))
                .thenReturn(List.of(suporte, exSuporte));

        //impl
        List<UsuarioResponseDTO> resultado = skillService.listarSuportesPorSkill(ID_SKILL);

        //asserts
        assertEquals(1, resultado.size());
        assertEquals(suporte.getId(), resultado.get(0).getIdUsuario());
    }

    @Test
    @DisplayName("deve listar as skills de um usuário")
    void deveListarSkillsDoUsuario() {
        //mocks
        UUID idUsuario = UUID.randomUUID();
        when(usuarioSkillRepository.findByIdUsuario(idUsuario)).thenReturn(List.of(vinculo(idUsuario, ID_SKILL)));
        when(skillRepository.findAllById(List.of(ID_SKILL))).thenReturn(List.of(new Skill(ID_SKILL, "REDE")));

        //impl
        List<SkillDTO> resultado = skillService.listarSkillsDoUsuario(idUsuario);

        //asserts
        assertEquals(1, resultado.size());
        assertEquals("REDE", resultado.get(0).getNome());
    }
}
