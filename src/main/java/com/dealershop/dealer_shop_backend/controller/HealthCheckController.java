package com.dealershop.dealer_shop_backend.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/api/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "application", "DealerShop",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}