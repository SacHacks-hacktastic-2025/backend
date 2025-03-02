package com.DailyDevlog.dailydevlog.controller;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/github")
@Tag(name = "GitHub OAuth", description = "GitHub 로그인 및 사용자 정보 API")
public class GitHubController {

  @Value("${github.client.id}")
  private String clientId;

  @Value("${github.client.secret}")
  private String clientSecret;

  @Value("${github.redirect.uri}")
  private String redirectUri;

  @Operation(summary = "GitHub 로그인", description = "GitHub OAuth 인증을 시작합니다.")
  @GetMapping("/login")
  public String login() {
    String githubLoginUrl = String.format(
        "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=user",
        clientId, redirectUri
    );
    return "redirect:" + githubLoginUrl;
  }

  @Operation(summary = "GitHub 인증 콜백", description = "GitHub 인증 후 호출되는 콜백 URL")
  @GetMapping("/callback")
  public String callback(@RequestParam(name = "code") String code) {
    String accessTokenUrl = String.format(
        "https://github.com/login/oauth/access_token?client_id=%s&client_secret=%s&code=%s",
        clientId, clientSecret, code
    );

    RestTemplate restTemplate = new RestTemplate();
    Map<String, String> response = restTemplate.postForObject(accessTokenUrl, null, Map.class);

    String accessToken = response.get("access_token");
    return "Access Token: " + accessToken;
  }

  @Autowired
  private RestTemplate restTemplate;
  @Operation(summary = "사용자 정보 조회", security = {@SecurityRequirement(name = "Bearer Authentication")})
  @GetMapping("/api/v1/github/user")
  public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String accessToken) {
    System.out.println("Authorization Header: " + accessToken);
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", accessToken);

      HttpEntity<String> entity = new HttpEntity<>(headers);
      ResponseEntity<String> response = restTemplate.exchange(
          "https://api.github.com/user",
          HttpMethod.GET,
          entity,
          String.class
      );

      if (response.getStatusCode() == HttpStatus.OK) {
        return ResponseEntity.ok(response.getBody());
      } else {
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보를 가져오는 중 오류 발생: " + e.getMessage());
    }
  }
}
