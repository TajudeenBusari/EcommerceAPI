/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.service.impl;

import com.tjtechy.notification_service.config.TwilioProperties;
import com.tjtechy.notification_service.service.SmsProvider;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class TwilioSmsProvider implements SmsProvider {

  private final TwilioProperties twilioProperties;

  private static final Logger logger = LoggerFactory.getLogger(TwilioSmsProvider.class);

  public TwilioSmsProvider(TwilioProperties twilioProperties) {
    this.twilioProperties = twilioProperties;
  }

  /**
   * Initialize Twilio with account SID and auth token.
   * This method is called after the bean is constructed.
   */
  @PostConstruct
  public void init() {
    //this sets up the authentication credentials for Twilio API
    Twilio.init(twilioProperties.getAccountSid(), twilioProperties.getAuthToken());

    logger.info("Initialized Twilio with Account SID: {}", twilioProperties.getAccountSid());
    logger.info("Using from number: {}", twilioProperties.getFromNumber());
  }

  /**It sends an SMS message using Twilio's API.
   * @param phoneNumber
   * @param message
   * @throws Exception
   */
  @Override
  public void sendSms(String phoneNumber, String message) {
    //FromNumber can be logged, but attackers can use it for spam or reconnaissance.
    logger.info("Using from number : {}", twilioProperties.getFromNumber());
    Message sms = Message.creator(
            new com.twilio.type.PhoneNumber(phoneNumber),
            new com.twilio.type.PhoneNumber(twilioProperties.getFromNumber()),
            message).create();
    //TODO: It is not a good practice to log customer's phone number in production.
    // Remove it or log as ****** later.
    //Message SID can be logged for tracking purpose.
    logger.info("Sent SMS via Twilio to {}: SID={}", phoneNumber, sms.getSid());
  }
}