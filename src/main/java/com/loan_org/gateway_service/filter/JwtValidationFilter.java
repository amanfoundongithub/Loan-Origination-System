package com.loan_org.gateway_service.filter;

import com.loan_org.gateway_service.jwt.JwtConfig;
import com.loan_org.gateway_service.jwt.JwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private final JwtConfig jwtConfig;
    private final JwtValidator jwtValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtValidationFilter(JwtConfig jwtConfig, JwtValidator jwtValidator) {
        this.jwtConfig = jwtConfig;
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getURI().getPath();

        boolean isExcluded = jwtConfig.getExcluded().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));

        if (isExcluded) {
            log.info("Path {} is immune to JWT validation. Passing through.", requestPath);
            return chain.filter(exchange);
        }

        String authHeaderKey = jwtConfig.getHeader();
        if (!request.getHeaders().containsKey(authHeaderKey)) {
            log.error("Authorization header [{}] missing for path: {}", authHeaderKey, requestPath);
            return onError(exchange);
        }

        String authHeader = request.getHeaders().getFirst(authHeaderKey);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Invalid token formatting in header [{}]", authHeaderKey);
            return onError(exchange);
        }

        String token = authHeader.substring(7);

        try {
            jwtValidator.validate(token);
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return onError(exchange);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

}
