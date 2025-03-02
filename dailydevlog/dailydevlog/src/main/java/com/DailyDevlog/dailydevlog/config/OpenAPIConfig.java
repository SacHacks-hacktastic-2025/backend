package com.DailyDevlog.dailydevlog.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) integration.
 * This class sets up the OpenAPI documentation and security requirements.
 */
@Configuration // Indicates that this class is a source of bean definitions.
@OpenAPIDefinition(
    // Provides metadata for the OpenAPI documentation.
    info = @Info(title = "Daily Devlog API", version = "v1"),
    // Specifies the default security requirement for API endpoints.
    security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
    name = "Bearer Authentication", // The name used in the OpenAPI security scheme.
    type = SecuritySchemeType.HTTP, // Defines the type of security scheme as HTTP.
    scheme = "bearer", // Specifies the HTTP Authorization scheme to be 'bearer'.
    bearerFormat = "JWT" // Indicates that the bearer tokens are in JWT format.
)
public class OpenAPIConfig {
}
