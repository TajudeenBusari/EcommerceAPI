/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the system module of the Ecommerce Microservices project.
 */

package com.tjtechy.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class Meter {
  private final MeterRegistry registry;

  public Meter(MeterRegistry registry) {
    this.registry = registry;
  }

  /**
   * Increment a counter-metric with the given name and tags.
   * @param tags
   * example: meter.incrementCounter("inventory.requests.by.id.total", "id", "123");
   */
  public void incrementCounter(String metricsName, String... tags) {
    registry.counter(metricsName, tags).increment();
  }
}
