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
  -     PRODUCT-SERVICE: provides the product details (product id, name, description, price, etc.)
        to the inventory service and order service. When new products are added,
        the product service will notify the inventory service to initialize the stock for the product.
  

  -     ORDER-SERVICE: provides the order details (order id, product id, user id, quantity, etc.) and 
        calls the product service to get the product details. It will also call the inventory service to 
        check if the product is available in stock and update the stock after placing the order.
        It will also call the user service to get the user details.
        POTENTIAL ISSUE:
        When an order is updated, the original ordered qauntity is not returned to the inventory service.
        The new quantity is simply deducted without considering the previous deduction.
        This leads to an incorrect stock count/level.
        SOLUTION:
        Track the original ordered quantity in the order service and return it to the inventory service
        when updating the stock. This way, the inventory service can correctly adjust the stock level.
        

  -     USER-SERVICE: 

  -     INVENTORY-SERVICE: Manages the stock of the products, tracks available inventory and ensures
  -     orders can be fulfilled. It will call the product service to get the product details and update the stock.
        It will also call the order service to get the order details and update the stock.
         
  -     API-GATEWAY: will be added later.

  -     EUREKA-SERVER:
        -     This is a service discovery server that allows microservices to register themselves and discover other services.
        -     It will be used to register the product service, order service, user service and inventory service.
        -     It will also be used to discover the product service, order service, user service and inventory service.

  -     CONFIGURATION-SERVER:
        -     This is a service that provides configuration properties to the microservices.
        -     It will be used to provide configuration properties to the product service, order service, user service and inventory service.
        -     It will also be used to provide configuration properties to the API gateway.


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

maven command for cleaning and building the project:
mvn clean install -DskipTests
To highlight text in the editor:
File -> Settings -> Editor -> TODO-> Add a new pattern and chose a color.
- POTENTIAL MEMORY LEAKS:
  -     When using WebClient, ensure that you are not creating a new instance of WebClient for each request.
        Instead, create a single instance of WebClient and reuse it for all requests.
        This will help in reducing memory leaks and improving performance.
        Use the WebClient.Builder to create a single instance of WebClient and reuse it for all requests.
        This will help in reducing memory leaks and improving performance.
        #What was observed during integration testing:
        The web application [ROOT] appears to have started a thread named [reactor-http-nio-*] but has failed to stop it. 
        This is very likely to create a memory leak:
        is a very common issue when using Netty-based servers (like Spring Webflux or reactive cleint like WebClient)
        in integration tests, espcially when tests are run inside Spring boot + Tomcat(Servlet-based) environment.
        For now, it is been suppressed by setting the following property in the application-test.yml file
        with: logging:
          level:
          org.apache.catalina.loader.WebappClassLoaderBase: ERROR
        