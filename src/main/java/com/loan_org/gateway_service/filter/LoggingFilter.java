package com.loan_org.gateway_service.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        // Record metrics
        String requestPath = exchange.getRequest().getURI().getPath();
        String requestMethod = exchange.getRequest().getMethod().name();

        // Log the request received
        log.info("Received request for {} with {} method", requestPath, requestMethod);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(
                        () -> {
                            int statusCode =
                                    exchange.getResponse().getStatusCode() != null ?
                                            exchange.getResponse().getStatusCode().value(): 0;

                            log.info("Response status code: {}", statusCode);
                        }
                ));

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
