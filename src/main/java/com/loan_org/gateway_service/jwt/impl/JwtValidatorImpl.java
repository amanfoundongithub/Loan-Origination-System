package com.loan_org.gateway_service.jwt.impl;

import com.loan_org.gateway_service.jwt.JwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtValidatorImpl implements JwtValidator {

    @Override
    public void validate(String jwtToken) {
        // will do this...
    }

}
