package com.DailyDevlog.dailydevlog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

  @GetMapping("/home")
  public String home() {
    return "Welcome to our website!";
  }
}
