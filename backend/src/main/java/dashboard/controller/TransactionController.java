package dashboard.controller;

import dashboard.dto.TransactionDto;
import dashboard.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for transaction operations.
 * Equivalent to your Python routers/transaction.py
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Get all transactions with pagination
     * Equivalent to: @app.get("/transactions/")
     */
    @GetMapping
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(Pageable pageable) {
        Page<TransactionDto> transactions = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transaction by ID
     * Equivalent to: @app.get("/transactions/{id}")
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id)
                .map(transaction -> ResponseEntity.ok(transaction))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new transaction
     * Equivalent to: @app.post("/transactions/")
     */
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        TransactionDto createdTransaction = transactionService.createTransaction(transactionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    /**
     * Update transaction
     * Equivalent to: @app.put("/transactions/{id}")
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDto transactionDto) {
        return transactionService.updateTransaction(id, transactionDto)
                .map(transaction -> ResponseEntity.ok(transaction))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete transaction
     * Equivalent to: @app.delete("/transactions/{id}")
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        if (transactionService.deleteTransaction(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get suspicious transactions
     * Equivalent to: @app.get("/transactions/suspicious")
     */
    @GetMapping("/suspicious")
    public ResponseEntity<List<TransactionDto>> getSuspiciousTransactions() {
        List<TransactionDto> suspiciousTransactions = transactionService.getSuspiciousTransactions();
        return ResponseEntity.ok(suspiciousTransactions);
    }

    /**
     * Run fraud analysis on transactions
     * Equivalent to: @app.post("/transactions/analyze")
     */
    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeTransactions() {
        int analyzedCount = transactionService.runFraudAnalysis();
        return ResponseEntity.ok("Analyzed " + analyzedCount + " transactions for fraud");
    }
}
