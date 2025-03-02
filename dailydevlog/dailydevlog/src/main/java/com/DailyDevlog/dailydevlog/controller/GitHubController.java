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

/**
 * Controller class for managing GitHub OAuth authentication, user information retrieval,
 * commit and issue inquiries, and automated GPT code reviews.
 * Provides API endpoints for interaction with GitHub services.
 */
@RestController // Indicates that this class handles HTTP requests and returns JSON responses.
@RequestMapping("/api/v1/github") // Base URL for all GitHub-related API endpoints.
@Tag(name = "GitHub OAuth", description = "GitHub login and user information API")
public class GitHubController {

  // Injecting GitHub OAuth client ID, secret, and redirect URI from application properties.
  @Value("${github.client.id}")
  private String clientId;

  @Value("${github.client.secret}")
  private String clientSecret;

  @Value("${github.redirect.uri}")
  private String redirectUri;

  /**
   * Starts GitHub OAuth authentication by redirecting to the GitHub login page.
   *
   * @return A redirect URL to the GitHub OAuth login page.
   */
  @Operation(summary = "GitHub Login", description = "GitHub OAuth Starts authentication.")
  @GetMapping("/login")
  public String login() {
    String githubLoginUrl = String.format(
        "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=user",
        clientId, redirectUri
    );
    return "redirect:" + githubLoginUrl;
  }

  /**
   * Handles the GitHub OAuth callback after authentication.
   * Exchanges the received authorization code for an access token.
   *
   * @param code The authorization code returned by GitHub.
   * @return The access token received from GitHub.
   */
  @Operation(summary = "GitHub Authentication callback", description = "GitHub Callback Called After Authentication URL")
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

  /**
   * Retrieves GitHub user information using the provided access token.
   *
   * @param accessToken The access token in the Authorization header.
   * @return User information or an error message if the request fails.
   */
  @Operation(summary = "User Information Inquiry", security = {@SecurityRequirement(name = "Bearer Authentication")})
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
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error getting user information: " + e.getMessage());
    }
  }

  @Autowired
  private GitHubService gitHubService;

  /**
   * Retrieves a list of commits from a specific GitHub repository.
   *
   * @param owner The repository owner.
   * @param repo The repository name.
   * @param author (Optional) The GitHub ID of the commit author.
   * @param branch (Optional) The repository branch, defaults to "main".
   * @return A list of commits with metadata.
   */
  @Operation(summary = "GitHub Commit Inquiry", description = "Look up the list of commitments in a specific repository.")
  @GetMapping("/commits")
  public List<Map<String, Object>> getGitHubCommits(
      @Parameter(description = "GitHub a repository owner", required = true) @RequestParam String owner,
      @Parameter(description = "GitHub Repository Name", required = true) @RequestParam String repo,
      @Parameter(description = "Commit Author GitHub ID", required = false) @RequestParam(required = false) String author,
      @Parameter(description = "Branch name (Default value: main)", required = false) @RequestParam(required = false, defaultValue = "main") String branch
  ) {
    return gitHubService.getUserCommits(owner, repo, author, branch);
  }

  /**
   * Retrieves a simplified list of issues from a specific GitHub repository.
   *
   * @param owner The repository owner.
   * @param repo The repository name.
   * @return A list of issues with basic details.
   */
  @Operation(summary = "GitHub Issue Simplified Inquiry", description = "Briefly look up the list of issues in a particular repository.")
  @GetMapping("/issues")
  public List<Map<String, Object>> getSimplifiedRepositoryIssues(
      @Parameter(description = "GitHub Repository Owner", required = true) @RequestParam String owner,
      @Parameter(description = "GitHub Repository Name", required = true) @RequestParam String repo
  ) {
    return gitHubService.getRepositoryIssues(owner, repo);
  }

  @Autowired
  private CodeReviewService codeReviewService;

  /**
   * Automatically performs a code review using GPT by analyzing commit messages and code changes.
   *
   * @param owner The repository owner.
   * @param repo The repository name.
   * @param author (Optional) The commit author.
   * @param branch (Optional) The branch to review, defaults to "main".
   * @return Code review suggestions from GPT.
   */
  @Operation(summary = "Automatic GPT Code Review", description = "Automatically import commitments and code changes from the GitHub repository and conduct code reviews through GPT.")
  @GetMapping("/auto-code-review")
  public String autoCodeReview(
      @Parameter(description = "GitHub Repository Owner", required = true) @RequestParam String owner,
      @Parameter(description = "GitHub Repository Name", required = true) @RequestParam String repo,
      @Parameter(description = "Commit Author GitHub ID", required = false) @RequestParam(required = false) String author,
      @Parameter(description = "Branch name (Default value: main)", required = false) @RequestParam(required = false, defaultValue = "main") String branch
  ) {
    List<Map<String, Object>> commits = gitHubService.getUserCommits(owner, repo, author, branch);
    StringBuilder commitMessages = new StringBuilder();
    StringBuilder codeChanges = new StringBuilder();

    for (Map<String, Object> commit : commits) {
      Map<String, Object> commitData = (Map<String, Object>) commit.get("commit");
      String message = (String) commitData.get("message");
      commitMessages.append(message).append("\n");

      String sha = (String) commit.get("sha");
      Map<String, Object> commitDetails = gitHubService.getCommitChanges(owner, repo, sha);

      List<Map<String, Object>> files = (List<Map<String, Object>>) commitDetails.get("files");
      for (Map<String, Object> file : files) {
        codeChanges.append("File: ").append(file.get("filename")).append("\n")
            .append(file.get("patch")).append("\n\n");
      }
    }
    return codeReviewService.analyzeCodeAndSuggestImprovements(commitMessages.toString(), codeChanges.toString());
  }
}
