/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Custom health indicator to monitor usable disk space.
 * Implements HealthIndicator interface to provide custom health check logic.
 * The health() method checks the usable disk space in the current directory.
 * If the usable space is above a defined threshold (10MB in this case), it reports the system as healthy (UP).
 * Otherwise, it reports the system as unhealthy (DOWN).
 * The health status and relevant details (usable memory and threshold) are included in the Health object returned by the method.
 * This custom health indicator can be accessed via the /actuator/health endpoint.
 */
@Component
public class CustomUsableMemoryHealthIndicator implements HealthIndicator {

  @Override
  public Health health() {
    File path = new File("."); //current directory
    long diskUsableInBytes = path.getUsableSpace();
    boolean isHealthy = diskUsableInBytes >= 10 * 1024 * 1024; // 10MB threshold
    //if there is enough disk space, the system is healthy else unhealthy
    Status status = isHealthy ? Status.UP : Status.DOWN;
    return Health
            .status(status)
            .withDetail("USABLE_MEMORY_IN_BYTES", diskUsableInBytes)
            .withDetail("THRESHOLD_IN_BYTES", 10 * 1024 * 1024)
            .build();
  }
}
