# EcommerceAPI
This is an Ecommerce microservices project.
In microservices architecture, each service is a separate entity 
and can be developed, deployed, scaled and maintained independently. 
This project is a simple example of an Ecommerce microservices project. 
It consists of the following services:
-     Product Service, 
-     Order Service, 
-     User Service,
-     Inventory Service,
-     API Gateway.
The choice of communication between services will be RESTful API.
The options are: 
-     RestTemplate (deprecated), 
-     Feign Client(Declarative HTTP Client: Provides a more concise and easier way to call other services)
-     WebClient (Reactive & Recommended: Supports asynchronous, non-blocking calls).
Asynchronous and non-blocking calls are calls  recommended for microservices architecture.

**Asynchronous calls**: These allow a program to initiate a task and move on to other tasks before 
the initial task is completed. This helps in utilizing resources more efficiently and improving 
the overall performance of the application.

**Non-blocking calls**: These ensure that the program does not wait for the task to complete 
before moving on to the next task. This is particularly useful in I/O operations where 
waiting for a response can be time-consuming.  

In the context of microservices, asynchronous and non-blocking calls are recommended 
because they help in handling multiple service requests concurrently without blocking 
the execution of other tasks, leading to better scalability and performance

Eureka Server (Service Discovery: Used to register and discover services).

**IMPORTANT NOTE**: The project will bw implemented using the web client.
When microservices need to call each other dynamically, they register with
Eureka server and use Eureka client with for service discovery.

**API Gateway**: This is a single entry point for all the services.
It is a reverse proxy that routes requests to the appropriate service.

Steps:
- Add dependencies (cloud-starter-netflix-eureka-client, Starter WebFlux (For WebClient), spring actuators) 
  in the pom.xml file for the following services: 
-     PRODUCT-SERVICE, ORDER-SERVICE for now.
-     USER-SERVICE, INVENTORY-SERVICE, API-GATEWAY will be added later.
1. Add the @EnableEurekaClient annotation in the main class of the services.
2. Configure the application.yml file with the Eureka server details.
3. Create a new spring boot project for Eureka server and add the dependency( spring-cloud-starter-netflix-eureka-server) in the pom.xml file.
4. Add the @EnableEurekaServer annotation in the main class of the Eureka server.
5. Configure the application.yml file with the Eureka server details.
6. Implement the WebClient in the order service to call the product service.
6.1 create a config class to initialize the WebClient.Builder bean.
6.2   Modify the OrderService class to call ProductService via Eureka Discovery
6.3 Expose an api in order controller to get the product details.
7. Run the Eureka server and the services.

*TODO: Switch from Spring Data JPA to Spring Data R2DBC by updating your pom.xml*
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>
    <dependency>
    <groupId>io.r2dbc</groupId>
    <artifactId>r2dbc-postgresql</artifactId> <!-- Replace with your database driver -->
    </dependency>