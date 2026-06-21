/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of User Service module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.controller;

import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import userutils.dto.UserRegistrationDto;
import userutils.mapper.UserMapper;
import com.tjtechy.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

  @Operation(summary = "Register a new user",
          description = "Endpoint to register a new user in the system.",
  responses = {@ApiResponse(responseCode = "200", description = "User Registration Success"),
          @ApiResponse(responseCode = "400", description = "Invalid Input Data")})
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

  @Operation(summary = "Get user by username",
          description = "Endpoint to retrieve user details by username.",
          responses = {@ApiResponse(responseCode = "200", description = "User Retrieval Success"),
                  @ApiResponse(responseCode = "404", description = "User Not Found")})
  @GetMapping("/by-username")
  public Mono<Result> getUserByUsername(@RequestParam String username) {
    return userService.findUserByUsername(username)
            .map(user -> {
              var userDto = UserMapper.mapFromUserToUserDto(user);
              return new Result("User retrieved successfully", true, userDto, StatusCode.SUCCESS);
            });
  }

    @Operation(summary = "Delete user by ID",
            description = "Endpoint to delete a user by their unique ID.",
            responses = {@ApiResponse(responseCode = "200", description = "User Deletion Success"),
                    @ApiResponse(responseCode = "404", description = "User Not Found")})
  @DeleteMapping("/{userId}")
  public Mono<Result> deleteUser(@PathVariable UUID userId) {
    return userService.deleteUser(userId)
            .then(Mono.just(new Result("User deleted successfully", true, StatusCode.SUCCESS)));
  }
}
