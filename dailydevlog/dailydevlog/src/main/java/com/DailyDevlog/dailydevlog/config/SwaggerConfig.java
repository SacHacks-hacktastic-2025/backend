package com.DailyDevlog.dailydevlog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger (OpenAPI) documentation.
 * This class sets up the API documentation, metadata, and security configurations.
 */
@Configuration // Marks this class as a configuration source for Spring.
public class SwaggerConfig {

  /**
   * Configures the OpenAPI documentation with custom information and security settings.
   *
   * @return an OpenAPI object configured with API metadata and security requirements.
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        // Sets the API metadata such as title, version, and description.
        .info(new Info()
            .title("Daily Devlog API") // The title displayed in Swagger UI.
            .version("1.0.0") // API version.
            .description("Daily Devlog API documentation for the project")) // Description for developers.

        // Adds a security requirement for API endpoints, specifying the need for bearer authentication.
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))

        // Defines the security scheme used for authentication.
        .components(new Components().addSecuritySchemes("Bearer Authentication",
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // Specifies HTTP authentication type.
                .scheme("bearer") // Sets the authentication scheme to "bearer".
                .bearerFormat("JWT") // Indicates that the token format is JWT (JSON Web Token).
                .name("Authorization") // The name of the HTTP header used for the token.
        ));
  }
}
