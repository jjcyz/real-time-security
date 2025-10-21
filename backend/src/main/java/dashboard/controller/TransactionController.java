package dashboard.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransactionController {

    // Simple in-memory storage for demo
    private List<Map<String, Object>> transactions = new ArrayList<>();
    private long nextId = 1;

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Security Dashboard API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        return response;
    }

    @GetMapping("/transactions")
    public List<Map<String, Object>> getAllTransactions() {
        return transactions;
    }

    @PostMapping("/transactions")
    public Map<String, Object> createTransaction(@RequestBody Map<String, Object> transaction) {
        transaction.put("id", nextId++);
        transaction.put("createdAt", new Date());
        transactions.add(transaction);
        return transaction;
    }

    @GetMapping("/transactions/suspicious")
    public List<Map<String, Object>> getSuspiciousTransactions() {
        return transactions.stream()
                .filter(t -> Boolean.TRUE.equals(t.get("isFraudulent")))
                .toList();
    }
}
