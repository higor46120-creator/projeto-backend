package br.edu.fiec.helptec.features.chamado.service;

import lombok.RequiredArgsConstructor;
import br.edu.fiec.helptec.config.FirebaseStorageService;
import br.edu.fiec.helptec.config.aws.S3StorageService;
import br.edu.fiec.helptec.features.chamado.ChamadoAnexo;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoAnexoDTO;
import br.edu.fiec.helptec.features.chamado.repository.ChamadoAnexoRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChamadoAnexoService {

    private final ChamadoAnexoRepository chamadoAnexoRepository;

    private final FirebaseStorageService firebaseStorageService;

    private final S3StorageService s3StorageService;

    // true: anexos vão para o bucket S3 da AWS; false: Firebase Storage
    @Value("${cloud.aws.s3.enabled:true}")
    private boolean usarS3;

    private static final String PASTA_UPLOADS = "uploads";
    private static final List<String> TIPOS_IMAGEM = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> TIPOS_VIDEO = List.of("video/mp4", "video/quicktime", "video/webm");

    // ---------------------------------------------------------
    // 1. Upload original — vai para o S3 da AWS (ou Firebase Storage) — imagem OU vídeo
    // ---------------------------------------------------------
    public ChamadoAnexoDTO upload(Long idChamado, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String tipo;

        if (contentType != null && TIPOS_IMAGEM.contains(contentType)) {
            tipo = "IMAGEM";
        } else if (contentType != null && TIPOS_VIDEO.contains(contentType)) {
            tipo = "VIDEO";
        } else {
            throw new IllegalArgumentException("Tipo de arquivo não suportado: " + contentType);
        }

        String pasta = "chamados/" + idChamado;
        String url = usarS3
                ? s3StorageService.upload(file, pasta)
                : firebaseStorageService.upload(file, pasta);

        ChamadoAnexo anexo = new ChamadoAnexo(null, idChamado, url, tipo, file.getOriginalFilename());
        ChamadoAnexo salvo = chamadoAnexoRepository.save(anexo);

        return toDTO(salvo);
    }

    // ---------------------------------------------------------
    // 2. Upload local com thumbnail — gera 600x600 + thumb_ 150x150 em /uploads
    // ---------------------------------------------------------
    public ChamadoAnexoDTO uploadComThumbnail(Long idChamado, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !TIPOS_IMAGEM.contains(contentType)) {
            throw new IllegalArgumentException("Apenas imagens são aceitas (jpeg, png, webp). Tipo recebido: " + contentType);
        }

        Path pastaUploads = Path.of(PASTA_UPLOADS);
        if (!Files.exists(pastaUploads)) {
            Files.createDirectories(pastaUploads);
        }

        String nomeBase = UUID.randomUUID() + ".jpg";
        Path caminhoImagemPrincipal = pastaUploads.resolve(nomeBase);
        Path caminhoThumbnail = pastaUploads.resolve("thumb_" + nomeBase);

        // try-with-resources: o arquivo enviado é fechado mesmo se der erro
        try (InputStream imagem = file.getInputStream()) {
            Thumbnails.of(imagem)
                    .size(600, 600)
                    .outputFormat("jpg")
                    .toFile(caminhoImagemPrincipal.toFile());
        }

        try (InputStream imagem = file.getInputStream()) {
            Thumbnails.of(imagem)
                    .size(150, 150)
                    .outputFormat("jpg")
                    .toFile(caminhoThumbnail.toFile());
        }

        ChamadoAnexo anexo = new ChamadoAnexo(null, idChamado, nomeBase, "IMAGEM", file.getOriginalFilename());
        ChamadoAnexo salvo = chamadoAnexoRepository.save(anexo);

        return toDTO(salvo);
    }

    // ---------------------------------------------------------
    // 3. Listar anexos de um chamado
    // ---------------------------------------------------------
    public List<ChamadoAnexoDTO> listarPorChamado(Long idChamado) {
        return chamadoAnexoRepository.findByIdChamado(idChamado)
                .stream().map(this::toDTO).toList();
    }

    // ---------------------------------------------------------
    // 4. Deletar anexo (remove do S3/Firebase Storage e do banco)
    // ---------------------------------------------------------
    public void deletar(Long idAnexo) {
        ChamadoAnexo anexo = chamadoAnexoRepository.findById(idAnexo)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado"));

        if (S3StorageService.ehDoS3(anexo.getUrl())) {
            s3StorageService.deletar(anexo.getUrl());
        } else if (anexo.getUrl() != null && anexo.getUrl().startsWith("http")) {
            firebaseStorageService.deletar(anexo.getUrl());
        }
        chamadoAnexoRepository.deleteById(idAnexo);
    }

    private ChamadoAnexoDTO toDTO(ChamadoAnexo a) {
        // Arquivo do S3 (privado): devolve um link temporário de 1 hora para abrir
        String url = s3StorageService.linkParaLeitura(a.getUrl());
        return new ChamadoAnexoDTO(a.getIdAnexo(), a.getIdChamado(), url, a.getTipo(), a.getNomeOriginal());
    }
}