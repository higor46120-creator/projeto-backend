package br.edu.fiec.helptec.features.aws;

import br.edu.fiec.helptec.config.aws.S3StorageService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Diagnóstico da integração com a AWS (só para ADMIN). */
@RestController
@RequestMapping("/api/aws")
public class AwsController {

    private final S3StorageService s3StorageService;

    public AwsController(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    // GET /api/aws/status -> { bucket, regiao, conectado, mensagem }
    @McpTool(name = "status_aws",
            description = "Verifica a conexão com o bucket S3 da AWS (bucket, região, conectado, mensagem). Somente ADMIN.",
            annotations = @McpTool.McpAnnotations(title = "Status da AWS", readOnlyHint = true,
                    destructiveHint = false, idempotentHint = true, openWorldHint = true))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status")
    public Map<String, Object> status() {
        return s3StorageService.status();
    }
}
