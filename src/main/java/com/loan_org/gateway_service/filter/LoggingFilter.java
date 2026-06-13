package com.loan_org.gateway_service.filter;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Value("${filters.logging.header}")
    private String correlationHeader;

    @Value("${filters.logging.start-time}")
    private String startTimeAttribute;

    private final Tracer tracer;

    public LoggingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        // Log the request received from the exchange
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getURI().getPath();
        String requestMethod = request.getMethod().name();

        // Traceability: Extract or generate a Correlation ID
        String correlationId = request.getHeaders().getFirst(correlationHeader);

        // Micrometer
        String traceId = (tracer.currentSpan() != null) ? Objects.requireNonNull(tracer.currentSpan()).context().traceId() : null;


        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = (traceId != null) ? traceId : java.util.UUID.randomUUID().toString();
            log.warn("The request does not have any Correlation ID assigned to it, so assigning a correlationId from micrometer: {}",
                    correlationId);
        }

        // Mutate the request to pass the header downstream to IAM/Docs services
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request.mutate().header(correlationHeader, correlationId).build())
                .build();

        // Performance Tracking: Save the start timestamp in exchange attributes
        mutatedExchange.getAttributes().put(startTimeAttribute, System.currentTimeMillis());

        // Log the acknowledgment for the request
        log.info("[ID: {}] Received {} request for {}", correlationId, requestMethod, requestPath);

        // Add to response header
        final String finalCorrelationId = correlationId;
        mutatedExchange.getResponse().beforeCommit(() -> {
            mutatedExchange.getResponse().getHeaders().add(correlationHeader, finalCorrelationId);
            return Mono.empty();
        });

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(
                        () -> {
                            Long startTime = mutatedExchange.getAttribute(startTimeAttribute);
                            long duration = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0;

                            HttpStatusCode statusCode = mutatedExchange.getResponse().getStatusCode();
                            int codeValue = (statusCode != null) ? statusCode.value() : 500;

                            log.info("[ID: {}] Responded with status {} in {} ms", finalCorrelationId, codeValue, duration);
                        }
                ));

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
