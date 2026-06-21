package com.tjtechy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;import org.springframework.stereotype.Component;

/**
 * This class is used to configure the OpenAPI documentation.
 * It provides properties for the service URL.
 * 1. serviceUrl: The URL of the service.
 * 2. It is annotated with @ConfigurationProperties to bind the properties from the application.properties file.
 * 3. The prefix for the properties is "app.openapi".
 * 4. This class is enabled in the OpenApiConfigConfiguration class using @EnableConfigurationProperties annotation.
 */

@ConfigurationProperties(prefix = "app.openapi")
public class OpenApiProperties {

  private String serviceUrl;
  private String gatewayUrl;

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public String getGatewayUrl() {
    return gatewayUrl;
  }
  public void setGatewayUrl(String gatewayUrl) {
    this.gatewayUrl = gatewayUrl;
  }

}
