package com.bestrookies.portfolio.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    // 健康检查端点
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}

