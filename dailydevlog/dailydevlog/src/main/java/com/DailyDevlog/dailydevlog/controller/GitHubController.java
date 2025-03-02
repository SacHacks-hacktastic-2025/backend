package com.DailyDevlog.dailydevlog.controller;

import com.DailyDevlog.dailydevlog.service.CodeReviewService;
import com.DailyDevlog.dailydevlog.service.GitHubService;
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
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
  @GetMapping("/user")
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
  @Autowired
  private GitHubService gitHubService;

  @Operation(summary = "GitHub 커밋 조회", description = "특정 레포지토리의 커밋 목록을 조회합니다.")
  @GetMapping("/commits")
  public List<Map<String, Object>> getGitHubCommits(
      @Parameter(description = "GitHub 레포지토리 소유자", required = true)
      @RequestParam String owner,
      @Parameter(description = "GitHub 레포지토리 이름", required = true)
      @RequestParam String repo,
      @Parameter(description = "커밋 작성자 GitHub ID", required = false)
      @RequestParam(required = false) String author,
      @Parameter(description = "브랜치 이름 (기본값: main)", required = false)
      @RequestParam(required = false, defaultValue = "main") String branch) {

    return gitHubService.getUserCommits(owner, repo, author, branch);
  }

  @Operation(summary = "GitHub 이슈 간소화된 조회", description = "특정 레포지토리의 이슈 목록을 간결하게 조회합니다.")
  @GetMapping("/issues")
  public List<Map<String, Object>> getSimplifiedRepositoryIssues(
      @Parameter(description = "GitHub 레포지토리 소유자", required = true)
      @RequestParam String owner,
      @Parameter(description = "GitHub 레포지토리 이름", required = true)
      @RequestParam String repo) {

    return gitHubService.getRepositoryIssues(owner, repo);
  }

  @Autowired
  private CodeReviewService codeReviewService;
  @Operation(summary = "자동 GPT 코드 리뷰", description = "GitHub 레포지토리의 커밋과 코드 변경 내역을 자동으로 가져와 GPT를 통해 코드 리뷰를 수행합니다.")
  @GetMapping("/auto-code-review")
  public String autoCodeReview(
      @Parameter(description = "GitHub 레포지토리 소유자", required = true) @RequestParam String owner,
      @Parameter(description = "GitHub 레포지토리 이름", required = true) @RequestParam String repo,
      @Parameter(description = "커밋 작성자 GitHub ID", required = false) @RequestParam(required = false) String author,
      @Parameter(description = "브랜치 이름 (기본값: main)", required = false) @RequestParam(required = false, defaultValue = "main") String branch
  ) {
    List<Map<String, Object>> commits = gitHubService.getUserCommits(owner, repo, author, branch);
    StringBuilder commitMessages = new StringBuilder();
    StringBuilder codeChanges = new StringBuilder();

    for (Map<String, Object> commit : commits) {
      // 'commit.get("commit")'의 결과를 'Map<String, Object>' 타입으로 변환
      Map<String, Object> commitData = (Map<String, Object>) commit.get("commit");

      // 이제 안전하게 "message" 필드에 접근할 수 있음
      String message = (String) commitData.get("message");
      commitMessages.append(message).append("\n");

      // SHA 값을 가져와서 커밋 변경 내역 조회
      String sha = (String) commit.get("sha");
      Map<String, Object> commitDetails = gitHubService.getCommitChanges(owner, repo, sha);

      // 변경된 파일 내역 처리
      List<Map<String, Object>> files = (List<Map<String, Object>>) commitDetails.get("files");
      for (Map<String, Object> file : files) {
        codeChanges.append("File: ").append(file.get("filename")).append("\n")
            .append(file.get("patch")).append("\n\n");
      }
    }

    return codeReviewService.analyzeCodeAndSuggestImprovements(commitMessages.toString(), codeChanges.toString());
  }
}
