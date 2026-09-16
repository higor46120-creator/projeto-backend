package br.edu.fiec.helptec.features.chamado.controller;

import br.edu.fiec.helptec.features.chamado.ChamadoAnexo;
import br.edu.fiec.helptec.features.chamado.repository.ChamadoAnexoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/images")
public class FileController {

    @Autowired
    private ChamadoAnexoRepository chamadoAnexoRepository;

    private static final String PASTA_UPLOADS = "uploads";

    // GET /images/{id}            -> imagem principal (600x600)
    // GET /images/{id}?thumb=true -> miniatura (150x150, prefixo thumb_)
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> buscarImagem(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean thumb
    ) throws IOException {
        ChamadoAnexo anexo = chamadoAnexoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada para o id: " + id));

        String nomeArquivo = thumb ? "thumb_" + anexo.getUrl() : anexo.getUrl();
        Path caminho = Path.of(PASTA_UPLOADS).resolve(nomeArquivo);

        if (!Files.exists(caminho)) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(caminho);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(360, TimeUnit.SECONDS))
                .contentType(MediaType.IMAGE_JPEG) // sempre jpg, conforme geramos no upload
                .body(bytes);
    }
}