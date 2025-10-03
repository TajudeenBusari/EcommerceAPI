/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.service.impl;

import com.tjtechy.notification_service.service.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DummySmsProvider implements SmsProvider {

  private static Logger logger = LoggerFactory.getLogger(DummySmsProvider.class);

  /**
   * @param phoneNumber
   * @param message
   * @throws Exception
   */
  @Override
  public void sendSms(String phoneNumber, String message) {
    // Simulate sending SMS by logging the message
    logger.info("Dummy Sending SMS to {}: {}", phoneNumber, message);
  }
}
