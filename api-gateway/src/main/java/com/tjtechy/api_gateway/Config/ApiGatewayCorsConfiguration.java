package com.tjtechy.api_gateway.Config;

/**
 * Configuration class for CORS (Cross-Origin Resource Sharing) settings in the API Gateway.
 * This class can be used to define CORS policies that will be applied to all incoming requests to the API Gateway.
 * By configuring CORS at the API Gateway level, we can ensure that all microservices behind the gateway inherit the same CORS settings,
 * allowing for consistent cross-origin request handling across the entire system.
 * Note: The actual CORS settings are already defined in the application.yml file of the API Gateway,
 * and this class can be used to further customize CORS behavior if needed.
 * This is especially important for allowing requests from the Swagger UI, which may be served from a different origin than the API Gateway.
 */
public class ApiGatewayCorsConfiguration {
}
