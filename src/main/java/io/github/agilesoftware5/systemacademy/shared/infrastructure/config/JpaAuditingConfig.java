package io.github.agilesoftware5.systemacademy.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Aparte de la clase de arranque para que los tests web (@WebMvcTest) no exijan un modelo JPA. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
