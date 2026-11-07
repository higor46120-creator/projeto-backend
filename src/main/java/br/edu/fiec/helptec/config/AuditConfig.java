package br.edu.fiec.helptec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Preenche createdBy/createdDate/lastModifiedBy/lastModifiedDate (AuditBaseEntity)
 * automaticamente nos documentos do MongoDB.
 */
@Configuration
@EnableMongoAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new ApplicationAuditAware();
    }
}
