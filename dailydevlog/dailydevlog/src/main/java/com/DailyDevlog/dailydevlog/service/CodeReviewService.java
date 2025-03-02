package com.DailyDevlog.dailydevlog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CodeReviewService is a service class that interacts with the GPT API
 * to analyze code quality and provide suggestions for improvement.
 * It uses GitHub commit messages and code changes as input for the analysis.
 */
@Service // Marks this class as a service component in the Spring application context.
public class CodeReviewService {

  // Retrieves the GPT API key from application properties or environment variables.
  @Value("${GPT_API}")
  private String gptApiKey;

  // RestTemplate is used to make HTTP requests to external APIs (e.g., GPT API).
  private final RestTemplate restTemplate = new RestTemplate();

  /**
   * Sends commit messages and code changes to the GPT API for analysis.
   *
   * @param commitMessage The commit messages from a GitHub repository.
   * @param codeChanges The code changes (diff) associated with the commits.
   * @return A review from GPT suggesting good practices and areas for improvement.
   */
  public String analyzeCodeAndSuggestImprovements(String commitMessage, String codeChanges) {
    // Builds the prompt to send to the GPT model for code review analysis.
    String prompt = "Here are the GitHub commit messages and code changes. " +
        "Please evaluate code quality, and let me know good habits and things to improve.\n\n" +
        "Commit message: " + commitMessage + "\n\n" +
        "Code change history: " + codeChanges + "\n\n" +
        "Explain good and bad habits with specific examples, " +
        "Please suggest a way to improve your bad habits.";

    // Sets up the HTTP request headers, including authorization and content type.
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + gptApiKey); // Sets the GPT API key as a Bearer token.
    headers.set("Content-Type", "application/json"); // Specifies JSON as the request body format.

    // Prepares the request body for the GPT API in the expected format.
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "gpt-3.5-turbo"); // Specifies the GPT model version.
    requestBody.put("messages", List.of(
        Map.of("role", "system", "content", "You are a helpful assistant that reviews code."),
        Map.of("role", "user", "content", prompt)
    ));
    requestBody.put("max_tokens", 500); // Limits the maximum length of the response.
    requestBody.put("temperature", 0.7); // Controls the randomness of the response (0.0 to 1.0).

    // Wraps the headers and body into an HttpEntity for the HTTP request.
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    try {
      // Sends a POST request to the GPT API and receives the response.
      ResponseEntity<Map> response = restTemplate.exchange(
          "https://api.openai.com/v1/chat/completions",
          HttpMethod.POST, // Specifies the HTTP method (POST).
          entity, // The request entity containing headers and body.
          Map.class // Specifies the expected response type as a Map.
      );

      // Extracts and processes the GPT API response.
      Map<String, Object> responseBody = response.getBody();
      if (responseBody != null && responseBody.containsKey("choices")) {
        // Retrieves the 'choices' array from the API response.
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (!choices.isEmpty()) {
          // Returns the 'content' of the first choice message from GPT.
          return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
        }
      }
    } catch (HttpClientErrorException e) {
      // Returns an error message if the GPT API call fails.
      return "Error calling GPT API: " + e.getMessage();
    }

    // Fallback response if no valid output is obtained from the GPT API.
    return "Code review failed, please check the input data.";
  }
}
