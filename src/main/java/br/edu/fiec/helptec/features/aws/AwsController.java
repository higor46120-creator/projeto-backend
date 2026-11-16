package br.edu.fiec.helptec.features.aws;

import br.edu.fiec.helptec.config.aws.S3StorageService;
import org.springframework.http.ResponseEntity;
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(s3StorageService.status());
    }
}
