package com.DailyDevlog.dailydevlog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

  private final RestTemplate restTemplate = new RestTemplate();

  public List<Map<String, Object>> getUserCommits(String username, String repo) {
    String url = String.format("https://api.github.com/repos/%s/%s/commits", username, repo);
    return restTemplate.getForObject(url, List.class);
  }

  public List<Map<String, Object>> getUserIssues(String username, String repo) {
    String url = String.format("https://api.github.com/repos/%s/%s/issues", username, repo);
    return restTemplate.getForObject(url, List.class);
  }
}
