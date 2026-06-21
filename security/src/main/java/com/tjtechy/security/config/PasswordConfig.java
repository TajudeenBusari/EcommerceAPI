package com.tjtechy.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
  /**
   * Password encoder bean using BCrypt hashing algorithm.
   * note: BCrypt is a strong hashing algorithm that is widely used for securely storing passwords.
   * This bean is needed when using the PasswordEncoder interface in the application.
   * It is a one-way hashing function, meaning that once a password is hashed,
   * it cannot be reversed back to its original form. This makes it ideal for password storage,
   * as even if the database is compromised, the original passwords cannot be easily retrieved.
   * private key is used to encode and public key (for decoding) is used to very if it is ok.
   * public key can be distributed to a public key server or other services
   * that need to verify the authenticity of the JWT tokens issued by this service.
   * private is kept as secrete for example in auth service.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder(12); //12 is the strength parameter
  }
}
