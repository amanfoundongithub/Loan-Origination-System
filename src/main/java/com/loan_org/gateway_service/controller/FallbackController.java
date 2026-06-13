package com.loan_org.gateway_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/fallback")
public class FallbackController {

    private static final String SERVICE_UNAVAILABLE_TEMPLATE =
            "The %s is currently unavailable or taking too long to respond. Please try again later.";

    @RequestMapping("/docs")
    public Mono<ResponseEntity<Map<String, Object>>> docsServiceFallback(ServerWebExchange exchange) {
        return Mono.just(createServiceUnavailableResponse("Document Service", exchange));
    }

    @RequestMapping("/iam")
    public Mono<ResponseEntity<Map<String, Object>>> iamServiceFallback(ServerWebExchange exchange) {
        return Mono.just(createServiceUnavailableResponse("IAM Service", exchange));
    }

    private ResponseEntity<Map<String, Object>> createServiceUnavailableResponse(String service, ServerWebExchange exchange) {

        Throwable exception = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        if (exception != null) {
            log.error("Fallback triggered for {} due to exception: {}", service, exception.getMessage());
        } else {
            log.warn("Fallback triggered for {} with no associated exception context.", service);
        }

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        Map.of(
                                "timestamp", Instant.now().toString(),
                                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                                "error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                                "message", String.format(SERVICE_UNAVAILABLE_TEMPLATE, service)
                        )
                );
    }
}