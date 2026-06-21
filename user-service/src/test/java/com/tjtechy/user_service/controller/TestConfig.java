package com.tjtechy.user_service.controller;


import com.tjtechy.user_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import userutils.entity.User;

@TestConfiguration
public class TestConfig {

//  @Bean
//  public AdminUserInitializer adminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, AdminProperties adminProperties) {
//    return new AdminUserInitializer(userRepository, passwordEncoder, adminProperties);
//  }
  @Bean
  public CommandLineRunner commandLineRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      userRepository.findByUserName("admin")
              //
              .switchIfEmpty(Mono.defer(()->{
                //if not found, create a new admin user
                User adminUser = new User();
                adminUser.setUserName("admin");
                adminUser.setFirstName("AdminUserTest");
                adminUser.setLastName("AdministratorTest");
                adminUser.setEmail("test@email.com");
                adminUser.setRole(User.Role.ADMIN);
                adminUser.setEnabled(true);
                adminUser.setPhoneNumber("1234567890");
                adminUser.setPassword(passwordEncoder.encode("Admin@123"));
                return userRepository.save(adminUser);
              })).doOnSuccess(savedAdmin -> System.out.println("======Test Admin user created with username: admin======="))
              .block();

    };
  }
}
