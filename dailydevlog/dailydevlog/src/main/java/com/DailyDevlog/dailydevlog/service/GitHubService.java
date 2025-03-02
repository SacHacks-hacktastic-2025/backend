package com.DailyDevlog.dailydevlog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class GitHubService {

  private final RestTemplate restTemplate = new RestTemplate();

  public List<Map<String, Object>> getUserCommits(String username, String repo, String author, String branch) {
    String url = String.format("https://api.github.com/repos/%s/%s/commits?sha=%s&author=%s", username, repo, branch, author);
    System.out.println("Request URL: " + url); // 로그 추가
    return restTemplate.getForObject(url, List.class);
  }

  // 새로운 이슈 조회 메서드 추가
  public List<Map<String, Object>> getRepositoryIssues(String owner, String repo) {
    String url = String.format("https://api.github.com/repos/%s/%s/issues?state=all", owner, repo);
    System.out.println("Request URL (Issues): " + url);

    List<Map<String, Object>> issues = restTemplate.getForObject(url, List.class);
    List<Map<String, Object>> simplifiedIssues = new ArrayList<>();

    if (issues != null) {
      for (Map<String, Object> issue : issues) {
        Map<String, Object> simplifiedIssue = new HashMap<>();
        simplifiedIssue.put("title", issue.get("title"));
        simplifiedIssue.put("body", issue.get("body"));
        simplifiedIssue.put("state", issue.get("state"));
        simplifiedIssue.put("url", issue.get("html_url"));
        simplifiedIssue.put("created_at", issue.get("created_at"));
        simplifiedIssue.put("updated_at", issue.get("updated_at"));

        simplifiedIssues.add(simplifiedIssue);
      }
    }

    return simplifiedIssues;
  }
}
