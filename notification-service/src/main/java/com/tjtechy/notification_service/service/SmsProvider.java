/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.service;

/**
 * Interface for sending SMS messages.
 * Several implementations can be provided, such as TwilioSmsProvider, AwsSnsSmsProvider, NexmoSmsProvider  or DummySmsProvider.
 */
public interface SmsProvider {
  void sendSms(String phoneNumber, String message) throws Exception;
}
