package br.edu.fiec.helptec.config.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Cliente da AWS S3. Região e chaves vêm do application.yaml (cloud.aws.*),
 * que por sua vez lê as variáveis AWS_REGION, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
 * e AWS_SESSION_TOKEN (esta só nas chaves temporárias da AWS Academy).
 *
 * Sem chaves no yaml, usa a cadeia padrão do SDK (~/.aws/credentials, variáveis do sistema...).
 * Criar o cliente não acessa a internet: o backend sobe mesmo sem credenciais.
 */
@Configuration
public class AwsS3Config {

    private static final Logger log = LoggerFactory.getLogger(AwsS3Config.class);

    @Value("${cloud.aws.region.static:us-east-1}")
    private String regiao;

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Value("${cloud.aws.credentials.session-token:}")
    private String sessionToken;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (preenchido(accessKey) && preenchido(secretKey)) {
            if (preenchido(sessionToken)) {
                log.info("AWS: usando chaves temporárias (com session token) das variáveis de ambiente");
                return StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(accessKey.trim(), secretKey.trim(), sessionToken.trim()));
            }
            log.info("AWS: usando chaves das variáveis de ambiente");
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey.trim(), secretKey.trim()));
        }
        log.info("AWS: chaves não definidas no yaml; usando a cadeia padrão do SDK (~/.aws/credentials etc.)");
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credenciais) {
        return S3Client.builder()
                .region(Region.of(regiao.trim()))
                .credentialsProvider(credenciais)
                // Cliente HTTP explícito: o aws-sdk-java traz vários e o SDK não sabe qual escolher
                .httpClientBuilder(ApacheHttpClient.builder())
                .build();
    }

    /** Gera links temporários para abrir arquivos privados do bucket. */
    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credenciais) {
        return S3Presigner.builder()
                .region(Region.of(regiao.trim()))
                .credentialsProvider(credenciais)
                .build();
    }

    private static boolean preenchido(String valor) {
        return valor != null && !valor.isBlank();
    }
}
