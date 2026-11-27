package br.edu.fiec.helptec.config.aws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** S3 simulado (Mockito): o teste não acessa a AWS nem precisa de credenciais. */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3StorageService")
class S3StorageServiceTest {

    private static final String BUCKET = "fiec-tcc-helptec";

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3StorageService s3StorageService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(s3StorageService, "bucket", BUCKET);
        ReflectionTestUtils.setField(s3StorageService, "regiao", "us-east-1");
    }

    @Test
    @DisplayName("upload grava no bucket, na pasta do chamado, e devolve a referência s3://")
    void deveEnviarArquivoParaOBucket() throws Exception {
        //mocks
        MockMultipartFile foto = new MockMultipartFile("file", "foto do PC.jpg", "image/jpeg", new byte[]{1, 2, 3});

        //impl
        String referencia = s3StorageService.upload(foto, "chamados/7");

        //asserts
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        PutObjectRequest enviado = captor.getValue();
        assertEquals(BUCKET, enviado.bucket());
        assertEquals("image/jpeg", enviado.contentType());
        assertTrue(enviado.key().startsWith("chamados/7/"));
        assertTrue(enviado.key().endsWith("_foto_do_PC.jpg"), "espaços viram _ no nome");
        assertEquals("s3://" + BUCKET + "/" + enviado.key(), referencia);
    }

    @Test
    @DisplayName("link de arquivo que não é do S3 (ex.: Firebase) volta igual")
    void deveManterLinkQueNaoEDoS3() {
        //impl
        String link = s3StorageService.linkParaLeitura("https://storage.googleapis.com/b/a.jpg");

        //asserts
        assertEquals("https://storage.googleapis.com/b/a.jpg", link);
        verifyNoInteractions(s3Presigner);
    }

    @Test
    @DisplayName("deletar apaga o objeto certo do bucket")
    void deveDeletarObjeto() {
        //impl
        s3StorageService.deletar("s3://" + BUCKET + "/chamados/7/abc_foto.jpg");

        //asserts
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("chamados/7/abc_foto.jpg", captor.getValue().key());
    }

    @Test
    @DisplayName("referência inválida não chega a chamar a AWS")
    void deveRecusarReferenciaInvalida() {
        //impl + asserts
        assertThrows(IllegalArgumentException.class, () -> s3StorageService.deletar("s3://so-o-bucket"));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("status informa quando o bucket não existe")
    void deveInformarBucketInexistente() {
        //mocks
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(NoSuchBucketException.builder().message("bucket não existe").build());

        //impl
        Map<String, Object> status = s3StorageService.status();

        //asserts
        assertEquals(false, status.get("conectado"));
        assertEquals(BUCKET, status.get("bucket"));
    }

    @Test
    @DisplayName("nome do arquivo perde caminhos e caracteres especiais")
    void deveLimparNomeDoArquivo() {
        assertEquals("relatorio_final.pdf", S3StorageService.nomeSeguro("C:\\Users\\x\\relatorio final.pdf"));
        assertEquals("arquivo", S3StorageService.nomeSeguro(null));
    }
}
