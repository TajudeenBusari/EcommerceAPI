/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.controller;

import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.user_service.entity.dto.UserRegistrationDto;
import com.tjtechy.user_service.mapper.UserMapper;
import com.tjtechy.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("${api.endpoint.base-url}/user")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  public Mono<Result> addUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
    //map to user entity
    var userEntity = UserMapper.mapFromUserRegistrationDtoToUser(registrationDto);
    return userService.createUser(userEntity)
            .map(savedUser -> {
              //map to dto
              var userDto = UserMapper.mapFromUserToUserDto(savedUser);
              return new Result("User created successfully", true, userDto, StatusCode.SUCCESS);
            });
  }

  @GetMapping("/by-username")
  public Mono<Result> getUserByUsername(@RequestParam String username) {
    return userService.findUserByUsername(username)
            .map(user -> {
              var userDto = UserMapper.mapFromUserToUserDto(user);
              return new Result("User retrieved successfully", true, userDto, StatusCode.SUCCESS);
            });
  }


  @DeleteMapping("/{userId}")
  public Mono<Result> deleteUser(@PathVariable UUID userId) {
    return userService.deleteUser(userId)
            .then(Mono.just(new Result("User deleted successfully", true, StatusCode.SUCCESS)));
  }
}
