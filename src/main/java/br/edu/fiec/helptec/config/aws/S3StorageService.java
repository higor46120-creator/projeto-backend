package br.edu.fiec.helptec.config.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Guarda e busca arquivos no bucket S3 do HelpTec.
 *
 * No banco fica só a referência "s3://bucket/caminho". Como o bucket é privado,
 * para abrir o arquivo é gerado um link temporário (válido por 1 hora).
 */
@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);
    static final String PREFIXO = "s3://";
    private static final Duration VALIDADE_LINK = Duration.ofHours(1);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket:fiec-tcc-helptec}")
    private String bucket;

    @Value("${cloud.aws.region.static:us-east-1}")
    private String regiao;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /** Envia o arquivo para o bucket, dentro da "pasta" informada. Devolve a referência s3://... */
    public String upload(MultipartFile arquivo, String pasta) throws IOException {
        String chave = pasta + "/" + UUID.randomUUID() + "_" + nomeSeguro(arquivo.getOriginalFilename());

        PutObjectRequest requisicao = PutObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .contentType(arquivo.getContentType())
                .build();

        try (InputStream conteudo = arquivo.getInputStream()) {
            s3Client.putObject(requisicao, RequestBody.fromInputStream(conteudo, arquivo.getSize()));
        }
        return PREFIXO + bucket + "/" + chave;
    }

    /** Link para abrir o arquivo. Referências que não são do S3 (ex.: Firebase) voltam como estão. */
    public String linkParaLeitura(String referencia) {
        if (!ehDoS3(referencia)) {
            return referencia;
        }
        String[] partes = separar(referencia);
        try {
            GetObjectPresignRequest pedido = GetObjectPresignRequest.builder()
                    .signatureDuration(VALIDADE_LINK)
                    .getObjectRequest(r -> r.bucket(partes[0]).key(partes[1]))
                    .build();
            return s3Presigner.presignGetObject(pedido).url().toString();
        } catch (RuntimeException e) {
            log.warn("Não foi possível gerar o link do arquivo {}: {}", referencia, e.getMessage());
            return referencia;
        }
    }

    /** Apaga o arquivo do bucket. */
    public void deletar(String referencia) {
        String[] partes = separar(referencia);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(partes[0]).key(partes[1]).build());
    }

    /** Confere se as credenciais funcionam e se o bucket existe (rota /api/aws/status). */
    public Map<String, Object> status() {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("bucket", bucket);
        resposta.put("regiao", regiao);
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            resposta.put("conectado", true);
            resposta.put("mensagem", "Credenciais válidas e bucket acessível.");
        } catch (RuntimeException e) {
            resposta.put("conectado", false);
            resposta.put("mensagem", e.getMessage());
        }
        return resposta;
    }

    public static boolean ehDoS3(String referencia) {
        return referencia != null && referencia.startsWith(PREFIXO);
    }

    /** "s3://bucket/caminho/arquivo" -> ["bucket", "caminho/arquivo"] */
    static String[] separar(String referencia) {
        if (!ehDoS3(referencia)) {
            throw new IllegalArgumentException("Referência não é do S3: " + referencia);
        }
        String semPrefixo = referencia.substring(PREFIXO.length());
        int barra = semPrefixo.indexOf('/');
        if (barra <= 0 || barra == semPrefixo.length() - 1) {
            throw new IllegalArgumentException("Referência do S3 inválida: " + referencia);
        }
        return new String[]{semPrefixo.substring(0, barra), semPrefixo.substring(barra + 1)};
    }

    /** Tira caminhos e caracteres estranhos do nome enviado pelo usuário. */
    static String nomeSeguro(String nomeOriginal) {
        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            return "arquivo";
        }
        String nome = nomeOriginal.replace('\\', '/');
        nome = nome.substring(nome.lastIndexOf('/') + 1);
        nome = nome.replaceAll("[^A-Za-z0-9._-]", "_");
        return nome.isBlank() ? "arquivo" : nome;
    }
}
