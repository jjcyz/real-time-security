package dashboard.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Root API controller
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Security Dashboard Backend");
        response.put("version", "2.0.0");
        response.put("status", "running");

        Map<String, Object> endpoints = new HashMap<>();

        // Info
        endpoints.put("server_info", "/info");
        endpoints.put("api_info", "/api");

        // Analytics
        endpoints.put("statistics", "/api/stats");
        endpoints.put("fraud_insights", "/api/fraud/insights");
        endpoints.put("fraud_threshold", "/api/fraud/threshold");

        // Transactions
        endpoints.put("all_transactions", "/api/transactions");
        endpoints.put("suspicious_transactions", "/api/transactions/suspicious");
        endpoints.put("high_risk_transactions", "/api/transactions/high-risk");

        // Users
        endpoints.put("all_users", "/api/users");

        // H2 Console (if enabled)
        endpoints.put("h2_console", "/h2-console");

        response.put("endpoints", endpoints);
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Security Dashboard Backend");
        response.put("version", "2.0.0");
        response.put("status", "running");
        return response;
    }
}
