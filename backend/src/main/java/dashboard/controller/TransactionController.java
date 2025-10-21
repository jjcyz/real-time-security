package dashboard.controller;

import dashboard.dto.TransactionRequest;
import dashboard.dto.TransactionResponse;
import dashboard.model.Transaction;
import dashboard.model.User;
import dashboard.repository.TransactionRepository;
import dashboard.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for transaction operations
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

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
     * Create new transaction
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

        // Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TransactionResponse(savedTransaction));
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
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Get database statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalTransactions = transactionRepository.count();
        long fraudulentTransactions = transactionRepository.findByIsFraudulentTrue().size();
        long totalUsers = userRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", totalTransactions);
        stats.put("fraudulentTransactions", fraudulentTransactions);
        stats.put("legitimateTransactions", totalTransactions - fraudulentTransactions);
        stats.put("fraudRate", totalTransactions > 0 ?
                (double) fraudulentTransactions / totalTransactions * 100 : 0.0);
        stats.put("totalUsers", totalUsers);

        return ResponseEntity.ok(stats);
    }
}
