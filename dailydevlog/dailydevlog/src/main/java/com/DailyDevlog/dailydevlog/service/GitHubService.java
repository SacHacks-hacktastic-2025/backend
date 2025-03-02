package com.DailyDevlog.dailydevlog.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * GitHubService is a service class that interacts with the GitHub API.
 * It provides methods to fetch commit data, repository issues, and specific commit changes.
 */
@Service // Marks this class as a service component in the Spring application context.
public class GitHubService {

  // RestTemplate is used to make HTTP requests to external APIs (e.g., GitHub API).
  private final RestTemplate restTemplate = new RestTemplate();

  /**
   * Retrieves a list of commits from a specific GitHub repository.
   *
   * @param username The GitHub username or organization name.
   * @param repo The repository name.
   * @param author (Optional) The GitHub username of the commit author.
   * @param branch The branch name to retrieve commits from (default: "main").
   * @return A list of commits with metadata.
   */
  public List<Map<String, Object>> getUserCommits(String username, String repo, String author, String branch) {
    // Constructs the GitHub API URL to fetch commit data.
    String url = String.format(
        "https://api.github.com/repos/%s/%s/commits?sha=%s&author=%s",
        username, repo, branch, author
    );
    System.out.println("Request URL: " + url); // Logs the request URL for debugging.

    // Sends a GET request to the GitHub API and returns the response as a list of maps.
    return restTemplate.getForObject(url, List.class);
  }

  /**
   * Retrieves a simplified list of all issues from a specific GitHub repository.
   *
   * @param owner The repository owner's GitHub username or organization name.
   * @param repo The repository name.
   * @return A list of simplified issue data including title, body, state, and URLs.
   */
  public List<Map<String, Object>> getRepositoryIssues(String owner, String repo) {
    // Constructs the API URL to fetch all issues from the repository.
    String url = String.format("https://api.github.com/repos/%s/%s/issues?state=all", owner, repo);
    System.out.println("Request URL (Issues): " + url); // Logs the request URL for issues.

    // Fetches the full issue data from the GitHub API.
    List<Map<String, Object>> issues = restTemplate.getForObject(url, List.class);

    // Prepares a list to hold simplified issue data.
    List<Map<String, Object>> simplifiedIssues = new ArrayList<>();

    if (issues != null) {
      for (Map<String, Object> issue : issues) {
        // Creates a simplified issue map with only necessary fields.
        Map<String, Object> simplifiedIssue = new HashMap<>();
        simplifiedIssue.put("title", issue.get("title")); // Issue title
        simplifiedIssue.put("body", issue.get("body")); // Issue body text
        simplifiedIssue.put("state", issue.get("state")); // Issue state (open, closed, etc.)
        simplifiedIssue.put("url", issue.get("html_url")); // URL to the issue on GitHub
        simplifiedIssue.put("created_at", issue.get("created_at")); // Creation date
        simplifiedIssue.put("updated_at", issue.get("updated_at")); // Last update date

        // Adds the simplified issue to the list.
        simplifiedIssues.add(simplifiedIssue);
      }
    }

    // Returns the list of simplified issues.
    return simplifiedIssues;
  }

  /**
   * Retrieves detailed information about a specific commit from a GitHub repository.
   *
   * @param owner The repository owner's GitHub username or organization name.
   * @param repo The repository name.
   * @param sha The SHA (hash) of the commit to retrieve.
   * @return A map containing commit details such as files changed, author, and message.
   */
  public Map<String, Object> getCommitChanges(String owner, String repo, String sha) {
    // Constructs the API URL to fetch specific commit details.
    String url = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, sha);

    // Sends a GET request to fetch the commit details and returns them as a map.
    return restTemplate.getForObject(url, Map.class);
  }
}
