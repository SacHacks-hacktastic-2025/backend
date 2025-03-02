package com.DailyDevlog.dailydevlog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

  private final RestTemplate restTemplate = new RestTemplate();

  public List<Map<String, Object>> getUserCommits(String username, String repo, String author, String branch) {
    String url = String.format("https://api.github.com/repos/%s/%s/commits?sha=%s&author=%s", username, repo, branch, author);
    System.out.println("Request URL: " + url); // 로그 추가
    return restTemplate.getForObject(url, List.class);
  }
}
