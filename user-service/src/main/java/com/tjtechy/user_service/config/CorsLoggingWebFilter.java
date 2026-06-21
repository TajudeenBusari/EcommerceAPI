/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of User Service module of the EcommerceMicroservices project.
 */
package com.tjtechy.user_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * CorsLoggingWebFilter is a WebFilter that logs CORS requests.
 * Implements WebFilter because the user service is built using Spring WebFlux (Reactive).
 * NOTE: This is also added to the api-gateway, may be removed from here later if not needed.
 */
@Component
public class CorsLoggingWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(CorsLoggingWebFilter.class);
    /**
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String origin = exchange.getRequest().getHeaders().getFirst("Origin");
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
        String path = exchange.getRequest().getURI().getPath();

        if (origin != null){
            logger.info("[CORS REQUEST] Origin: {}, Method: {}, Path: {}", origin, method, path);
        }
        return chain.filter(exchange)
                .doOnError(error -> logger.error("Request failed for {} {}: {}", method, path, error.getMessage()));
    }
}