/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the EcommerceMicroservices project.
 */
package com.tjtechy.user_service.service.impl;


import com.tjtechy.security.config.MyUserPrincipal;
import com.tjtechy.user_service.service.UserService;
import org.slf4j.Logger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import userutils.dto.LoginResponseDto;
import userutils.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import userutils.mapper.UserMapper;

@Service
public class AuthService {
    /**
     * NOTE:The JwtProvider must be annotated with @Component or
     * @Service for the bean to be created and injected here.
     */
    private final JwtProvider jwtProvider;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    public AuthService(JwtProvider jwtProvider, UserService userService, PasswordEncoder passwordEncoder){

        this.jwtProvider= jwtProvider;
      this.userService = userService;
      this.passwordEncoder = passwordEncoder;
    }

    //Instead of the HashMap<String, Object> we used to return,
    // we can create a DTO class to hold the user info and token,
    // and return that DTO instead. This is more type safe and easier to maintain.
    public Mono<LoginResponseDto> createLoginInfo(String username, String password) {

        return userService.findUserByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found with username: " + username)))
                .flatMap(user -> authenticateAndGenerateToken(user, password));
    }

    private Mono<LoginResponseDto> authenticateAndGenerateToken(User user, String rawPassword) {

        //for debugging why password does not match
        //TODO: Kindly remove this after debugging
        //to use AdminProperties for debugging, kindly inject into the class. Dont create a new instance like this
        //WRONG: var adminProp = new AdminProperties();
        //logger.info("=======Raw Password: {}=======", rawPassword);
        //logger.info("=======Stored password: {}========", user.getPassword());
        //logger.info("==========Expected configured password: '{}'",adminProp.getPassword());

        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        logger.info("=====Password matches: {}==========", matches);

        //validate password
        if (!matches) {
            return Mono.error(new BadCredentialsException("Invalid username or password"));
        }

        //create principal
        MyUserPrincipal principal = new MyUserPrincipal(user);

        //create authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        //generate token and create login info map
        String token = jwtProvider.createToken(authentication);

        //convert user to userDto
        var userDto = UserMapper.mapFromUserToUserDto(user);

        //TODO: save a copy of the token in redis before returning to the client. Key is "whitelist: {userId}", value is token
        //Map<String, Object> loginResultMap = new HashMap<>();
        //loginResultMap.put("userInfo", userDto);
        //loginResultMap.put("token", token);

        //response payload
        var loginResponse = new LoginResponseDto(
                userDto,
                token
        );

        return Mono.just(loginResponse);
    }

}