package com.loan_org.gateway_service.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "filters.jwt")
public class JwtConfig {
    private String       header;
    private List<String> excluded;
}
