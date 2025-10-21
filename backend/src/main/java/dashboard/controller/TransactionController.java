package dashboard.controller;

import dashboard.dto.TransactionRequest;
import dashboard.dto.TransactionResponse;
import dashboard.dto.UserResponse;
import dashboard.event.TransactionEvent;
import dashboard.model.Transaction;
import dashboard.model.User;
import dashboard.repository.TransactionRepository;
import dashboard.repository.UserRepository;
import dashboard.service.FraudDetectionService;
import dashboard.service.KafkaProducerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for transaction operations with fraud detection
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired(required = false)
    private KafkaProducerService kafkaProducerService;

    @GetMapping
    public Map<String, Object> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Security Dashboard API");
        response.put("version", "2.0.0");
        response.put("status", "running");
        response.put("database", "Connected");
        response.put("endpoints", Map.of(
            "transactions", "/api/transactions",
            "suspicious", "/api/transactions/suspicious",
            "users", "/api/users"
        ));
        return response;
    }

    /**
     * Get all transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        List<TransactionResponse> response = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction by ID
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .<ResponseEntity<?>>map(transaction -> ResponseEntity.ok(new TransactionResponse(transaction)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Transaction not found with id: " + id)));
    }

    /**
     * Create new transaction with fraud detection
     * Uses Kafka for async processing when enabled
     */
    @PostMapping("/transactions")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequest request) {
        // Find user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setUser(user);

        // Set optional fields
        if (request.getCurrency() != null) {
            transaction.setCurrency(request.getCurrency());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getMerchantName() != null) {
            transaction.setMerchantName(request.getMerchantName());
        }
        if (request.getMerchantCategory() != null) {
            transaction.setMerchantCategory(request.getMerchantCategory());
        }
        if (request.getTransactionLocation() != null) {
            transaction.setTransactionLocation(request.getTransactionLocation());
        }
        if (request.getIpAddress() != null) {
            transaction.setIpAddress(request.getIpAddress());
        }
        if (request.getDeviceId() != null) {
            transaction.setDeviceId(request.getDeviceId());
        }

        // Save transaction first
        Transaction savedTransaction = transactionRepository.save(transaction);

        if (kafkaProducerService != null) {
            // ASYNC MODE: Publish to Kafka for async fraud detection
            TransactionEvent event = new TransactionEvent(
                savedTransaction.getId(),
                savedTransaction.getAmount(),
                user.getId(),
                user.getUsername()
            );
            event.setCurrency(savedTransaction.getCurrency());
            event.setType(savedTransaction.getType());
            event.setMerchantName(savedTransaction.getMerchantName());
            event.setMerchantCategory(savedTransaction.getMerchantCategory());
            event.setTransactionLocation(savedTransaction.getTransactionLocation());
            event.setIpAddress(savedTransaction.getIpAddress());
            event.setDeviceId(savedTransaction.getDeviceId());

            kafkaProducerService.publishTransactionEvent(event);

            Map<String, Object> response = new HashMap<>();
            response.put("transaction", new TransactionResponse(savedTransaction));
            response.put("message", "Transaction created and published to Kafka for async fraud detection");
            response.put("processingMode", "ASYNC");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } else {
            // SYNC MODE: Perform fraud detection immediately
            Map<String, Object> fraudAnalysis = fraudDetectionService.analyzeFraud(savedTransaction, user);

            savedTransaction.setIsFraudulent((Boolean) fraudAnalysis.get("isFraudulent"));
            savedTransaction.setFraudScore(BigDecimal.valueOf((Double) fraudAnalysis.get("fraudScore")));
            savedTransaction.setFraudReason((String) fraudAnalysis.get("fraudReason"));
            savedTransaction = transactionRepository.save(savedTransaction);

            Map<String, Object> response = new HashMap<>();
            response.put("transaction", new TransactionResponse(savedTransaction));
            response.put("fraudAnalysis", fraudAnalysis);
            response.put("processingMode", "SYNC");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }

    /**
     * Get all fraudulent transactions
     */
    @GetMapping("/transactions/suspicious")
    public ResponseEntity<List<TransactionResponse>> getSuspiciousTransactions() {
        List<Transaction> fraudulent = transactionRepository.findByIsFraudulentTrue();
        List<TransactionResponse> response = fraudulent.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get transactions by user ID
     */
    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<?> getTransactionsByUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found with id: " + userId));
        }

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        List<TransactionResponse> response = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> response = users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get database statistics with fraud detection metrics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalTransactions = transactionRepository.count();
        long fraudulentTransactions = transactionRepository.findByIsFraudulentTrue().size();
        long totalUsers = userRepository.count();

        // Calculate fraud rate
        double fraudRate = totalTransactions > 0 ?
                (double) fraudulentTransactions / totalTransactions * 100 : 0.0;

        // Get high-risk transactions (score >= 70)
        List<Transaction> highRiskTransactions = transactionRepository
                .findHighRiskTransactions(BigDecimal.valueOf(fraudDetectionService.getFraudThreshold()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", totalTransactions);
        stats.put("fraudulentTransactions", fraudulentTransactions);
        stats.put("legitimateTransactions", totalTransactions - fraudulentTransactions);
        stats.put("fraudRate", String.format("%.2f%%", fraudRate));
        stats.put("totalUsers", totalUsers);
        stats.put("highRiskTransactions", highRiskTransactions.size());
        stats.put("fraudThreshold", fraudDetectionService.getFraudThreshold());
        stats.put("avgTransactionsPerUser", totalUsers > 0 ?
                String.format("%.2f", (double) totalTransactions / totalUsers) : "0");

        return ResponseEntity.ok(stats);
    }

    /**
     * Analyze a specific transaction for fraud
     */
    @PostMapping("/transactions/{id}/analyze")
    public ResponseEntity<?> analyzeTransaction(@PathVariable Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        User user = transaction.getUser();
        Map<String, Object> fraudAnalysis = fraudDetectionService.analyzeFraud(transaction, user);

        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", id);
        response.put("currentFraudScore", transaction.getFraudScore());
        response.put("currentStatus", transaction.getIsFraudulent() ? "FRAUDULENT" : "LEGITIMATE");
        response.put("reanalysis", fraudAnalysis);

        return ResponseEntity.ok(response);
    }

    /**
     * Get fraud detection threshold
     */
    @GetMapping("/fraud/threshold")
    public ResponseEntity<Map<String, Object>> getFraudThreshold() {
        Map<String, Object> response = new HashMap<>();
        response.put("threshold", fraudDetectionService.getFraudThreshold());
        response.put("description", "Transactions with fraud score >= threshold are marked as fraudulent");
        return ResponseEntity.ok(response);
    }

    /**
     * Get high-risk transactions
     */
    @GetMapping("/transactions/high-risk")
    public ResponseEntity<List<TransactionResponse>> getHighRiskTransactions() {
        List<Transaction> highRisk = transactionRepository
                .findHighRiskTransactions(BigDecimal.valueOf(fraudDetectionService.getFraudThreshold()));

        List<TransactionResponse> response = highRisk.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get fraud patterns and insights
     */
    @GetMapping("/fraud/insights")
    public ResponseEntity<Map<String, Object>> getFraudInsights() {
        List<Transaction> allFraudulent = transactionRepository.findByIsFraudulentTrue();

        Map<String, Object> insights = new HashMap<>();

        // Calculate average fraud score
        double avgFraudScore = allFraudulent.stream()
                .mapToDouble(t -> t.getFraudScore() != null ? t.getFraudScore().doubleValue() : 0.0)
                .average()
                .orElse(0.0);

        // Find most common fraud reasons
        Map<String, Long> reasonCounts = allFraudulent.stream()
                .filter(t -> t.getFraudReason() != null)
                .flatMap(t -> java.util.Arrays.stream(t.getFraudReason().split("; ")))
                .collect(Collectors.groupingBy(reason -> reason, Collectors.counting()));

        insights.put("totalFraudulentTransactions", allFraudulent.size());
        insights.put("averageFraudScore", String.format("%.2f", avgFraudScore));
        insights.put("topFraudReasons", reasonCounts);
        insights.put("fraudThreshold", fraudDetectionService.getFraudThreshold());

        return ResponseEntity.ok(insights);
    }
}
