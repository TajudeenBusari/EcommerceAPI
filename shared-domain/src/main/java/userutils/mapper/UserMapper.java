/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package userutils.mapper;

import userutils.dto.UserDto;
import userutils.dto.UserRegistrationDto;
import userutils.entity.User;

import java.util.List;

public class UserMapper {
  /**
   * Ensure ordering according to the UserDto constructor to avoid mapping errors
   * or mismatches.
   */
  public static UserDto mapFromUserToUserDto(User user){
    return new UserDto(
            user.getUserId(),
            user.getUserName(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.isEnabled(),
            user.getRole()
    );
  }

  public static List<UserDto> mapFromUserListToUserDtoList(List<User> users){
    return users.stream()
            .map(UserMapper::mapFromUserToUserDto)
            .toList();
  }

  /**
   * Ensure ordering according to the User constructor to avoid mapping error
   * or mismatches.
   */
  public static User mapFromUserRegistrationDtoToUser(UserRegistrationDto registrationDto){
    return new User(
            null,
            registrationDto.userName(),
            registrationDto.firstName(),
            registrationDto.lastName(),
            registrationDto.email(),
            registrationDto.password(),
            registrationDto.enabled(),
            registrationDto.role(),
            registrationDto.phoneNumber(),
            null,
            null
    );
  }
}
