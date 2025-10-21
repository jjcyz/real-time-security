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
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("endpoints", Map.of(
            "api", "/api"
        ));
        return response;
    }
}
