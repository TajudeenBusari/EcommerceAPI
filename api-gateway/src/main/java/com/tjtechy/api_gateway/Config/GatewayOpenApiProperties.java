package com.tjtechy.api_gateway.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.openapi")
public class GatewayOpenApiProperties {
  private String gatewayUrl;

  public String getGatewayUrl() {
    return gatewayUrl;
  }

  public void setGatewayUrl(String gatewayUrl) {
    this.gatewayUrl = gatewayUrl;
  }

}
