/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.twilio")
public class TwilioProperties {
  private String accountSid;
  private String authToken;
  private String fromNumber;

  public String getAccountSid() {
    return accountSid;
  }

  public void setAccountSid(String accountSid) {
    this.accountSid = accountSid;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getFromNumber() {
    return fromNumber;
  }

  public void setFromNumber(String fromNumber) {
    this.fromNumber = fromNumber;
  }
}
