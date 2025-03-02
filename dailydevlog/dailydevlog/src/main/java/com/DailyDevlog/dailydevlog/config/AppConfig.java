package com.DailyDevlog.dailydevlog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Application configuration class.
 * This class is responsible for defining and managing beans in the Spring application context.
 */
@Configuration // Marks this class as a source of bean definitions for the application context.
public class AppConfig {

  /**
   * Creates and registers a RestTemplate bean.
   *
   * RestTemplate is a synchronous client to perform HTTP requests,
   * exposing a simple, template-based API over underlying HTTP client libraries.
   * It is commonly used to communicate with RESTful web services.
   *
   * Example usage:
   * RestTemplate restTemplate = applicationContext.getBean(RestTemplate.class);
   * ResponseEntity<String> response = restTemplate.getForEntity("https://example.com", String.class);
   *
   * @return a new instance of RestTemplate
   */
  @Bean // Indicates that this method produces a bean to be managed by the Spring container.
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}