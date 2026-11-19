package br.edu.fiec.helptec.config.mcp;

import br.edu.fiec.helptec.features.aws.AwsController;
import br.edu.fiec.helptec.features.chamado.controller.ChamadoController;
import br.edu.fiec.helptec.features.dashboard.controller.DashboardController;
import br.edu.fiec.helptec.features.equipamento.controller.EquipamentoController;
import br.edu.fiec.helptec.features.salas.controller.SalasController;
import br.edu.fiec.helptec.features.skill.controller.SkillController;
import br.edu.fiec.helptec.features.usuario.controller.UsuarioController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Garante que as ferramentas MCP expostas aos agentes de IA estão bem descritas:
 * nomes únicos em snake_case, descrição, parâmetros documentados e dicas
 * coerentes com o verbo HTTP (GET = só leitura, DELETE = destrutiva).
 */
@DisplayName("Anotações MCP dos controllers")
class McpToolsAnotacoesTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            ChamadoController.class, SalasController.class, EquipamentoController.class,
            SkillController.class, UsuarioController.class, DashboardController.class, AwsController.class);

    private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9_]{2,63}$");

    private static List<Method> ferramentas() {
        return CONTROLLERS.stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                .filter(m -> m.isAnnotationPresent(McpTool.class))
                .toList();
    }

    @Test
    @DisplayName("todos os controllers expõem pelo menos uma ferramenta")
    void todosOsControllersTemFerramentas() {
        //impl + asserts
        for (Class<?> controller : CONTROLLERS) {
            boolean temFerramenta = Arrays.stream(controller.getDeclaredMethods())
                    .anyMatch(m -> m.isAnnotationPresent(McpTool.class));
            assertTrue(temFerramenta, controller.getSimpleName() + " sem @McpTool");
        }
    }

    @Test
    @DisplayName("nomes são únicos, em snake_case e com descrição")
    void nomesUnicosEDescritos() {
        //impl
        Set<String> nomes = new HashSet<>();

        //asserts
        for (Method m : ferramentas()) {
            McpTool tool = m.getAnnotation(McpTool.class);
            assertTrue(SNAKE_CASE.matcher(tool.name()).matches(), "nome inválido: " + tool.name());
            assertTrue(nomes.add(tool.name()), "nome repetido: " + tool.name());
            assertFalse(tool.description().isBlank(), tool.name() + " sem descrição");
            assertFalse(tool.annotations().title().isBlank(), tool.name() + " sem título");
        }
        assertEquals(ferramentas().size(), nomes.size());
    }

    @Test
    @DisplayName("todo parâmetro tem @McpToolParam com descrição e nenhum é arquivo")
    void parametrosDocumentados() {
        //impl + asserts
        for (Method m : ferramentas()) {
            for (Parameter p : m.getParameters()) {
                McpToolParam param = p.getAnnotation(McpToolParam.class);
                String onde = m.getName() + "(" + p.getName() + ")";
                assertTrue(param != null && !param.description().isBlank(), onde + " sem @McpToolParam");
                assertFalse(MultipartFile.class.isAssignableFrom(p.getType()), onde + " recebe arquivo");
            }
        }
    }

    @Test
    @DisplayName("GET é só leitura e DELETE é destrutivo")
    void dicasCoerentesComOVerbo() {
        //impl + asserts
        for (Method m : ferramentas()) {
            McpTool.McpAnnotations dicas = m.getAnnotation(McpTool.class).annotations();
            if (m.isAnnotationPresent(GetMapping.class)) {
                assertTrue(dicas.readOnlyHint(), m.getName() + " deveria ser readOnly");
                assertFalse(dicas.destructiveHint(), m.getName() + " não é destrutivo");
            }
            if (m.isAnnotationPresent(DeleteMapping.class)) {
                assertFalse(dicas.readOnlyHint(), m.getName() + " altera dados");
                assertTrue(dicas.destructiveHint(), m.getName() + " deveria ser destrutivo");
            }
        }
    }
}
