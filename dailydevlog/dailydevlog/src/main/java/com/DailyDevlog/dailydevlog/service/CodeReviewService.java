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

@Service
public class CodeReviewService {

  @Value("${GPT_API}")
  private String gptApiKey;

  private final RestTemplate restTemplate = new RestTemplate();

  public String analyzeCodeAndSuggestImprovements(String commitMessage, String codeChanges) {
    String prompt = "다음은 GitHub 커밋 메시지와 코드 변경 내역입니다. " +
        "코드 품질을 평가하고, 좋은 습관과 개선할 점을 알려주세요.\n\n" +
        "커밋 메시지: " + commitMessage + "\n\n" +
        "코드 변경 내역: " + codeChanges + "\n\n" +
        "좋은 습관과 나쁜 습관을 구체적인 예시와 함께 설명하고, " +
        "나쁜 습관을 개선할 수 있는 방법을 제안해주세요.";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + gptApiKey);
    headers.set("Content-Type", "application/json");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "gpt-3.5-turbo");
    requestBody.put("messages", List.of(
        Map.of("role", "system", "content", "You are a helpful assistant that reviews code."),
        Map.of("role", "user", "content", prompt)
    ));
    requestBody.put("max_tokens", 500);
    requestBody.put("temperature", 0.7);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<Map> response = restTemplate.exchange(
          "https://api.openai.com/v1/chat/completions",
          HttpMethod.POST,
          entity,
          Map.class
      );

      Map<String, Object> responseBody = response.getBody();
      if (responseBody != null && responseBody.containsKey("choices")) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (!choices.isEmpty()) {
          return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
        }
      }
    } catch (HttpClientErrorException e) {
      return "GPT API 호출 중 오류가 발생했습니다: " + e.getMessage();
    }

    return "코드 리뷰에 실패했습니다. 입력 데이터를 확인해주세요.";
  }
}
