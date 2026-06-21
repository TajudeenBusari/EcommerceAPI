/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of Security module of the EcommerceMicroservices project.
 */
package com.tjtechy.user_service.controller;

import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.user_service.service.impl.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import userutils.dto.LoginRequestDto;

@RestController
@RequestMapping("${api.endpoint.base-url}/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {

        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<Result> getLoginInfo(@RequestBody LoginRequestDto loginRequest) {
        logger.debug("Authenticated user : '{}'", loginRequest.username());
        return authService.createLoginInfo(loginRequest.username(), loginRequest.password())
                .map(loginInfoMap ->
                        new Result("User Info and JSON Web Token", true, loginInfoMap, StatusCode.SUCCESS));
    }
}