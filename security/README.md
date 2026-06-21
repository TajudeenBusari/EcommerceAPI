security flow:
client ---> API gateway --->Security filter ---> microservice
For the security logic for this microservices project, there are two possibilities:

Using Keycloak (IAM) as an external identity provider.
---------------------------------------------------------
Auth Responsibility: Delegate to  keycloak (Identity provider)
Who Issue Tokens: keycloak issues amd sign JWTs (OAuth2 / OIDC)
Validate Tokens: Spring boot services validate tokens using keycloak public keys(JWKs)
Token format: OAuth2-compliant JWTs (RS256 signed)
Security features: Built-in OAuths, OIDC, SSO, roles, scopes, password policies, MFA, refresh tokens, etc.
Revocation/Expiration: Built-in token revocation and expiration handled by keycloak
Microservices integration: via spring-boot-starter-oauth2-resource-server and keycloak to secure microservices.
API Gateway Role: validate JWTs, optionally offload authentication to keycloak, route requests to microservices.
Set up complexity: need Keycloak Docker container image, realm config, client setup.
Best for: Enterprise applications need robust, standardized security with minimal custom code.
Learning curve: Steeper requires understanding OAuth2/OIDC and Keycloak concepts.
Local development: Keycloak can be run locally via Docker for development and testing.
Example of token endpoint: https://<keycloak-8080>/auth/realms/<realm-name>/protocol/openid-connect/token
Flexibility: Limited to Keycloak's features and configurations.
Security risks: very low (battle-tested solution).
Dependencies: 
    spring-boot-starter-security,
    spring-boot-starter-oauth2-resource-server, 
    spring-boot-starter-oauth2-client (API Gateway->Reactive security),
    WebFlux Gateway for reactive support.
Documentation: https://www.keycloak.org/documentation


Implementing custom security logic within the security module.
-------------------------------------------------------------
Auth Responsibility: Handled by your user service + security module
Who Issue Tokens: app issues JWT via JwtService
Validate Tokens: Each service validates tokens using shared secret or public key
Token format: Custom JWTs (HS256 or RS256 signed).
Security features: Custom roles, scopes, password policies, MFA, refresh tokens, etc. are implemented as needed.
Revocation/Expiration: Custom token revocation and expiration logic implemented as needed.
Microservices integration: via spring-boot-starter-oauth2-resource-server and custom security module to secure microservices.
API Gateway Role: validate JWTs issued by User/Auth service.
Set up complexity: need to implement user service, token issuance, validation, and security logic from scratch.
Best for: Lightweight Applications need highly customized security logic not covered by standard IAM solutions.
Learning curve: Moderate, requires understanding JWTs and security best practices, but less complex than full IAM solutions.
Local development: Works standalone, no external auth server.
Example of token endpoint: POST /auth/login.
Flexibility: Full control over claims, structure, expiration, roles.
Security risks: higher (custom implementation may have vulnerabilities if not done carefully).
Dependencies: 
    spring-boot-starter-security,
    spring-boot-starter-oauth2-resource-server, 
    spring-boot-starter-webflux (API Gateway→Reactive security).

The standard security flow to generate and validate JWTs is as follows:
-----------------------------------------------------------------------
1. User registers via the /register endpoint of the user-module (Auth service must call user service to fetch user info).
   * The password is hashed, and it is saved with user details in the user database.
2. User logs in via the /auth/login with username and password.
3. The AuthController calls the AuthenticationManager to authenticate the user/validate credentials.
   * On success, the ReactiveUserDetailsService is used to find the user.
   * A JWT is generated via the AuthService using JwtService and returned to the client.
4. The client stores this token (e.g., in local storage, cookie, or memory).
5. All subsequent requests from the client will include Authorization: Bearer <token>.

**Summary Table**:
| Step | Service                                                     | Action                                                         |
|------|-------------------------------------------------------------|----------------------------------------------------------------|
| 1️⃣  | Client → `/auth/register` (User Service)                    | Creates user, returns 201 Created                              |
| 2️⃣  | Client → `/auth/login` (Auth Controller in Security Module) | Validates credentials                                          |
| 3️⃣  |                                                             | Calls `ReactiveUserDetailsService.findByUsername()` internally |
| 4️⃣  |                                                             | If valid → issues JWT token                                    |
| 5️⃣  | Client → `/api/v1/orders` (via API Gateway)                 | Includes JWT in header                                         |
| 6️⃣  | API Gateway                                                 | Validates JWT, sets authentication in context                  |
| 7️⃣  | Gateway → Order Service                                     | Adds user info to request header                               |
| 8️⃣  | Order Service                                               | Processes request knowing the authenticated user               |
