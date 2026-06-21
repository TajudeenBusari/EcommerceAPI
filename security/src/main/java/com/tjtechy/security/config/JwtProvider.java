/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of Security module of the EcommerceMicroservices project.
 */
package com.tjtechy.security.config;


import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    /**
     * The JwtEncoder is responsible for encoding and signing JWT tokens.
     * The bean must be created in a configuration class (SecurityConfiguration for example)
     * before it can be injected here.
     * We need the public and private keys to digitally sign this token and verify its authenticity.
     * So in the security configuration class,
     * we will create a JwtEncoder bean that uses the RSA private key to sign the token and the RSA public key to verify the token.
     * SUMMARY: Before encoding, we have to prepare the claims that we want to include in the token, such as the subject,
     * issued at time, expiration time, and any custom claims (like userId and authorities).
     * Then we use the JwtEncoder to encode these claims into a JWT token string that can be sent to the client.
     */
    private final JwtEncoder jwtEncoder;

    public JwtProvider(JwtEncoder jwtEncoder) {

        this.jwtEncoder = jwtEncoder;
    }

    //method to create token
    public String createToken(Authentication authentication) {

        Instant now = Instant.now(); //issued at
        long expiresIn = 2; //expires in 2 hours

        String authorities = authentication
                .getAuthorities()
                .stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(" ")); //must be space delimited

        JwtClaimsSet claims = JwtClaimsSet
                .builder()
                .issuer("self") //means we are not using an external identity provider to issue the token, we are issuing the token ourselves.
                .issuedAt(now)
                .expiresAt(now.plus(expiresIn, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("userId", ((MyUserPrincipal) (authentication.getPrincipal())).user().getUserId())
                .claim("authorities", authorities)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}