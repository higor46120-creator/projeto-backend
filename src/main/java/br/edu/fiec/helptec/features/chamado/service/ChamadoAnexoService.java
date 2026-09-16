package br.edu.fiec.helptec.features.chamado.service;

import br.edu.fiec.helptec.config.FirebaseStorageService;
import br.edu.fiec.helptec.features.chamado.ChamadoAnexo;
import br.edu.fiec.helptec.features.chamado.model.dto.ChamadoAnexoDTO;
import br.edu.fiec.helptec.features.chamado.repository.ChamadoAnexoRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class ChamadoAnexoService {

    @Autowired
    private ChamadoAnexoRepository chamadoAnexoRepository;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    private static final String PASTA_UPLOADS = "uploads";
    private static final List<String> TIPOS_IMAGEM = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> TIPOS_VIDEO = List.of("video/mp4", "video/quicktime", "video/webm");

    // ---------------------------------------------------------
    // 1. Upload original — vai para o Firebase Storage (imagem OU vídeo)
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

        String url = firebaseStorageService.upload(file, "chamados/" + idChamado);

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

        Thumbnails.of(file.getInputStream())
                .size(600, 600)
                .outputFormat("jpg")
                .toFile(caminhoImagemPrincipal.toFile());

        Thumbnails.of(file.getInputStream())
                .size(150, 150)
                .outputFormat("jpg")
                .toFile(caminhoThumbnail.toFile());

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
    // 4. Deletar anexo (remove do Firebase Storage e do banco)
    // ---------------------------------------------------------
    public void deletar(Long idAnexo) {
        ChamadoAnexo anexo = chamadoAnexoRepository.findById(idAnexo)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado"));

        firebaseStorageService.deletar(anexo.getUrl());
        chamadoAnexoRepository.deleteById(idAnexo);
    }

    private ChamadoAnexoDTO toDTO(ChamadoAnexo a) {
        return new ChamadoAnexoDTO(a.getIdAnexo(), a.getIdChamado(), a.getUrl(), a.getTipo(), a.getNomeOriginal());
    }
}