/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.inventory_service.config;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * During testing, we don't want to export spans to an external tracing system.
 * Therefore, this configuration is excluded when the "test" profile is active.
 */
@Configuration
@Profile("!test") //this ensures that this config is not loaded during tests, since integration test loads the whole application context
public class TracingConfig {
  @Bean
  public OtlpHttpSpanExporter otlpHttpSpanExporter(@Value("${tracing.url}") String url) {
    return OtlpHttpSpanExporter.builder()
        .setEndpoint(url)
            .build();
  }
}

