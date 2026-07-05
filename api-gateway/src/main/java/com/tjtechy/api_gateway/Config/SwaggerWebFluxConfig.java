package com.tjtechy.api_gateway.Config;



import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class SwaggerWebFluxConfig implements WebFluxConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    //serve Swagger UI static resources
    registry.addResourceHandler("/swagger-ui/**", "/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/")
            .resourceChain(false);


    registry.addResourceHandler("/v3/api-docs/**")
            .addResourceLocations("classpath:/META-INF/resources/")
            .resourceChain(false);
  }
}
