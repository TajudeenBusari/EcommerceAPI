package com.tjtechy.user_service.service.impl;

import com.tjtechy.modelNotFoundException.UserNotFoundException;
import com.tjtechy.user_service.entity.User;
import com.tjtechy.user_service.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @InjectMocks
  private UserServiceImpl userService;

  private List<User> users;

  @BeforeEach
  void setUp() {
    users = new ArrayList<>();
    var user1 = new User();
    user1.setUserId(UUID.randomUUID());
    user1.setUserName("john_doe");
    user1.setPassword("password123");
    user1.setFirstName("John");
    user1.setLastName("Doe");
    user1.setEmail("john@doe.com");
    user1.setPhoneNumber("1234567890");
    user1.setRole(User.Role.CUSTOMER);
    user1.setEnabled(true);
    user1.setCreatedAt(LocalDate.now());
    user1.setUpdatedAt(LocalDate.now());
    users.add(user1);

    var user2 = new User();
    user2.setUserId(UUID.randomUUID());
    user2.setUserName("jane_smith");
    user2.setPassword("password456");
    user2.setFirstName("Jane");
    user2.setLastName("Smith");
    user2.setEmail("jane@smith.com");
    user2.setPhoneNumber("0987654321");
    user2.setRole(User.Role.CUSTOMER);
    user2.setEnabled(true);
    user2.setCreatedAt(LocalDate.now());
    user2.setUpdatedAt(LocalDate.now());
    users.add(user2);

    //USER WITH INVALID PASSWORD LENGTH
    var user3 = new User();
    user3.setUserId(UUID.randomUUID());
    user3.setUserName("invalid_user");
    user3.setPassword("123"); // Invalid password length
    user3.setFirstName("Invalid");
    user3.setLastName("User");
    user3.setEmail("invalid@invalid.com");
    user3.setPhoneNumber("1112223333");
    user3.setRole(User.Role.CUSTOMER);
    user3.setEnabled(true);
    user3.setCreatedAt(LocalDate.now());
    user3.setUpdatedAt(LocalDate.now());
    users.add(user3);

    //USER WITH INVALID EMAIL FORMAT
    var user4 = new User();
    user4.setUserId(UUID.randomUUID());
    user4.setUserName("bad_email_user");
    user4.setPassword("validPassword1");
    user4.setFirstName("BadEmail");
    user4.setLastName("User");
    user4.setEmail("bademail.com"); // Invalid email format
    user4.setPhoneNumber("4445556666");
    user4.setRole(User.Role.CUSTOMER);
    user4.setEnabled(true);
    user4.setCreatedAt(LocalDate.now());
    user4.setUpdatedAt(LocalDate.now());
    users.add(user4);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void createUserSuccess() {
    //Given
    given(passwordEncoder.encode(users.get(0).getPassword())).willReturn("encodedPassword123");
    given(userRepository.save(users.get(0))).willReturn(Mono.just(users.get(0)));

    //When
    var createdUserMono = userService.createUser(users.get(0));

    //Then
    /**
     * StepVerifier is a utility from Project Reactor used for testing reactive streams.
     * It allows you to create a test scenario where you can define expectations about the sequence of
     * events (like emitted items, completion signals, or errors) that a Mono or Flux should produce.
     * expectNext(users.get(0)) checks that the next item emitted by the Mono is equal to users.get(0).
     * verifyComplete() verifies that the Mono completes successfully after emitting the expected item.
     */
    StepVerifier.create(createdUserMono)
            .expectNext(users.get(0))
            .verifyComplete();
  }

  @Test
  void createUserWithInvalidPasswordLength() {
    //Given
    given(passwordEncoder.encode(users.get(2).getPassword())).willReturn("encodedPassword123");
    given(userRepository.save(users.get(2))).willReturn(Mono.error(new IllegalArgumentException("Password must be at least 6 characters long")));

    //When
    var createdUserMono = userService.createUser(users.get(2));
    //Then
    StepVerifier.create(createdUserMono)
            .expectErrorSatisfies(throwable -> {
              assertTrue(throwable instanceof IllegalArgumentException);
              assertEquals("Password must be at least 6 characters long", throwable.getMessage());
            });

  }

  @Test
  void createUserWithInvalidEmail() {
    //Given
    given(passwordEncoder.encode(users.get(3).getPassword())).willReturn("encodedPassword123");
    given(userRepository.save(users.get(3))).willReturn(Mono.error(new IllegalArgumentException("Email should be valid")));

    //When
    var createdUserMono = userService.createUser(users.get(3));
    //Then
    StepVerifier.create(createdUserMono)
            .expectErrorSatisfies(throwable -> {
              assertTrue(throwable instanceof IllegalArgumentException);
              assertEquals("Email should be valid", throwable.getMessage());
            });
  }

  @Test
  void findUserByUsernameSuccess() {
    //Given
    given(userRepository.findByUserName(users.get(0).getUserName())).willReturn(Mono.just(users.get(0)));

    //When
    var foundUserMono = userService.findUserByUsername(users.get(0).getUserName());

    //Then
    StepVerifier.create(foundUserMono)
            .expectNext(users.get(0))
            .verifyComplete();
  }

  @Test
  void findUserByUsernameNotFound() {
    //Given
    given(userRepository.findByUserName("non_existent_user")).willReturn(Mono.empty());

    //When
    var foundUserMono = userService.findUserByUsername("non_existent_user");

    //Then
    StepVerifier.create(foundUserMono)
            .verifyComplete(); // Expecting completion without any emitted item
  }



  @Test
  void findUserById() {
  }

  @Test
  void findAllUsers() {
  }

  @Test
  void updateUser() {
  }

  @Test
  void deleteUserSuccess() {
    //Given
    given(userRepository.findById(users.get(0).getUserId())).willReturn(Mono.just(users.get(0)));
    given(userRepository.save(users.get(0))).willReturn(Mono.just(users.get(0)));
    given(userRepository.delete(users.get(0))).willReturn(Mono.empty());
    //When
    var deleteUserMono = userService.deleteUser(users.get(0).getUserId());
    //Then
    StepVerifier.create(deleteUserMono)
            .verifyComplete();
  }

  @Test
  void deleteUserNotFound() {
    //Given
    UUID nonExistentUserId = UUID.randomUUID();
    given(userRepository.findById(nonExistentUserId)).willReturn(Mono.empty());

    //When
    var deleteUserMono = userService.deleteUser(nonExistentUserId);

    //Then
    StepVerifier.create(deleteUserMono)
            .expectError(UserNotFoundException.class)
            .verify();
  }

  @Test
  void deleteAllUsersByIds() {
  }

  @Test
  void updateAllUsers() {
  }
}