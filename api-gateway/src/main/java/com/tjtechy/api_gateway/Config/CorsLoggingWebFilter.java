/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the Api-gateway Service module of the EcommerceMicroservices project.
 */
package com.tjtechy.api_gateway.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
/**
 * CorsLoggingWebFilter is a WebFilter that logs CORS requests.
 * Implements WebFilter because the API Gateway is built using Spring WebFlux (Reactive).
 * //NOTE: MAY BE NEEDED IN THE SUB SERVICES LATER TO LOG CORS REQUESTS IF THEY ARE
 * ACCESSED DIRECTLY FROM THOSE SERVICES RATHER THAN THROUGH THE API GATEWAY.
 */

@Component
public class CorsLoggingWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(CorsLoggingWebFilter.class);

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