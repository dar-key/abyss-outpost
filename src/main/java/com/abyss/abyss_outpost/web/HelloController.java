package com.abyss.abyss_outpost.web;

import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {
    @GetMapping("/api/ping")
    public String ping() { return "sonar online"; }
}