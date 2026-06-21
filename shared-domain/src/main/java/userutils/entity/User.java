/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package userutils.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * User entity representing a user in the system.
 * This class is mapped to the "users" table in the database.
 * Because this module is built using Spring WebFlux with R2DBC as against the MVC with JPA/Hibernate,
 * it uses reactive programming paradigms for non-blocking database interactions, and so
 * we avoid using JPA annotations like @Entity (it is a JPA Hibernate annotation).
 * No @Entity
 * No @GeneratedValue
 * No @PrePersist or @PreUpdate lifecycle callbacks
 * No ddl-auto configurations
 */

@Table(name = "users")
public class User implements Serializable {
  @Id
  @Column("user_id")
  //private UUID userId = UUID.randomUUID();
  private UUID userId;

  @Column("user_name")
  @NotBlank(message = "Username is required")
  @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
  private String userName;

  @NotBlank(message = "First name is required")
  @Column("first_name")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Column("last_name")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Column("email")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters long")
  @Column("password")
  private String password;

  @Column("enabled")
  private boolean enabled = true;

  @Column("role")
  private Role role; //ENUM STORED AS STRING BY DEFAULT IN WEbFLUX/r2dbc

  @Column("phone_number")
  @Size(min = 7, max = 15, message = "Phone number must be between 1 and 15 characters")
  @Pattern(
          regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
          message = "Invalid phone number format"
  )
  private String phoneNumber;

  @Column("created_at")
  private LocalDate createdAt = LocalDate.now();
  @Column("updated_at")
  private LocalDate updatedAt = LocalDate.now();

  public User updateTimestamps() {

    if (this.createdAt == null) {
      this.createdAt = LocalDate.now();
    }
    this.updatedAt = LocalDate.now();
    return this;
  }

  /**
   * Automatically sets the createdAt and updatedAt fields before persisting and updating the entity.
   * Commonly used in jpa to initialize timestamp or audit fields.
   */

  public User() {
  }
  public User(UUID userId,
              String userName,
              String firstName,
              String lastName,
              String email,
              String password,
              boolean enabled,
              Role role,
              String phoneNumber,
              LocalDate createdAt,
              LocalDate updatedAt) {
    this.userId = userId;
    this.userName = userName;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.enabled = enabled;
    this.role = role;
    this.phoneNumber = phoneNumber;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {

    this.userName = userName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }
  public LocalDate getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(LocalDate createdAt) {
    this.createdAt = createdAt;
  }
  public LocalDate getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(LocalDate updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public String toString() {
    return "User{" +
            "userId=" + userId +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", userName='" + userName + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", email='" + email + '\'' +
//            ", password='" + password + '\'' + // Avoid logging password
            ", enabled=" + enabled +
            ", role=" + role +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
  }

  public enum Role {
    ADMIN,
    CUSTOMER,
    VENDOR
  }
}
