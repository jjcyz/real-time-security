package dashboard.service;

import dashboard.dto.TransactionDto;
import dashboard.model.Transaction;
import dashboard.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for transaction business logic.
 * Equivalent to your Python services/transaction.py
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Get all transactions with pagination
     */
    public Page<TransactionDto> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * Get transaction by ID
     */
    public Optional<TransactionDto> getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * Create new transaction
     */
    public TransactionDto createTransaction(TransactionDto transactionDto) {
        Transaction transaction = convertToEntity(transactionDto);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return convertToDto(savedTransaction);
    }

    /**
     * Update transaction
     */
    public Optional<TransactionDto> updateTransaction(Long id, TransactionDto transactionDto) {
        return transactionRepository.findById(id)
                .map(existingTransaction -> {
                    updateEntityFromDto(existingTransaction, transactionDto);
                    Transaction savedTransaction = transactionRepository.save(existingTransaction);
                    return convertToDto(savedTransaction);
                });
    }

    /**
     * Delete transaction
     */
    public boolean deleteTransaction(Long id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Get suspicious transactions (fraud detection)
     */
    public List<TransactionDto> getSuspiciousTransactions() {
        return transactionRepository.findByIsFraudulentTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Run fraud analysis on all transactions
     * Equivalent to your Python fraud detection logic
     */
    public int runFraudAnalysis() {
        List<Transaction> transactions = transactionRepository.findAll();
        int analyzedCount = 0;

        for (Transaction transaction : transactions) {
            if (analyzeTransactionForFraud(transaction)) {
                analyzedCount++;
            }
        }

        return analyzedCount;
    }

    /**
     * Analyze individual transaction for fraud
     * Implements the same fraud detection rules as your Python version
     */
    private boolean analyzeTransactionForFraud(Transaction transaction) {
        boolean isFraudulent = false;
        double fraudScore = 0.0;

        // Rule 1: High-value transactions (95th percentile)
        if (isHighValueTransaction(transaction)) {
            fraudScore += 0.3;
            isFraudulent = true;
        }

        // Rule 2: Off-hours transactions (11 PM - 6 AM)
        if (isOffHoursTransaction(transaction)) {
            fraudScore += 0.2;
            isFraudulent = true;
        }

        // Rule 3: Rapid successive transactions (within 5 minutes)
        if (hasRapidSuccessiveTransactions(transaction)) {
            fraudScore += 0.4;
            isFraudulent = true;
        }

        // Rule 4: Duplicate transactions
        if (isDuplicateTransaction(transaction)) {
            fraudScore += 0.5;
            isFraudulent = true;
        }

        // Update transaction with fraud analysis results
        if (isFraudulent) {
            transaction.setIsFraudulent(true);
            transaction.setFraudScore(fraudScore);
            transactionRepository.save(transaction);
        }

        return isFraudulent;
    }

    /**
     * Check if transaction is high-value (above 95th percentile)
     */
    private boolean isHighValueTransaction(Transaction transaction) {
        // For simplicity, using a fixed threshold
        // In production, you'd calculate the 95th percentile dynamically
        BigDecimal highValueThreshold = new BigDecimal("1000.00");
        return transaction.getAmount().compareTo(highValueThreshold) > 0;
    }

    /**
     * Check if transaction occurs during off-hours (11 PM - 6 AM)
     */
    private boolean isOffHoursTransaction(Transaction transaction) {
        int hour = transaction.getTimestamp().getHour();
        return hour >= 23 || hour <= 6;
    }

    /**
     * Check for rapid successive transactions (within 5 minutes)
     */
    private boolean hasRapidSuccessiveTransactions(Transaction transaction) {
        LocalDateTime fiveMinutesAgo = transaction.getTimestamp().minusMinutes(5);
        LocalDateTime fiveMinutesLater = transaction.getTimestamp().plusMinutes(5);

        List<Transaction> recentTransactions = transactionRepository
                .findByUserIdAndTimestampBetween(
                        transaction.getUserId(),
                        fiveMinutesAgo,
                        fiveMinutesLater
                );

        return recentTransactions.size() > 3; // More than 3 transactions in 10-minute window
    }

    /**
     * Check for duplicate transactions (same amount, merchant, within 1 hour)
     */
    private boolean isDuplicateTransaction(Transaction transaction) {
        LocalDateTime oneHourAgo = transaction.getTimestamp().minusHours(1);
        LocalDateTime oneHourLater = transaction.getTimestamp().plusHours(1);

        List<Transaction> similarTransactions = transactionRepository
                .findByAmountAndMerchantAndTimestampBetween(
                        transaction.getAmount(),
                        transaction.getMerchant(),
                        oneHourAgo,
                        oneHourLater
                );

        return similarTransactions.size() > 1; // More than 1 similar transaction
    }

    /**
     * Convert Entity to DTO
     */
    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setTransactionId(transaction.getTransactionId());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setMerchant(transaction.getMerchant());
        dto.setLocation(transaction.getLocation());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setUserId(transaction.getUserId());
        dto.setIpAddress(transaction.getIpAddress());
        dto.setDeviceInfo(transaction.getDeviceInfo());
        dto.setIsFraudulent(transaction.getIsFraudulent());
        dto.setFraudScore(transaction.getFraudScore());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }

    /**
     * Convert DTO to Entity
     */
    private Transaction convertToEntity(TransactionDto dto) {
        Transaction transaction = new Transaction();
        transaction.setId(dto.getId());
        transaction.setTransactionId(dto.getTransactionId());
        transaction.setAmount(dto.getAmount());
        transaction.setCurrency(dto.getCurrency());
        transaction.setTimestamp(dto.getTimestamp());
        transaction.setMerchant(dto.getMerchant());
        transaction.setLocation(dto.getLocation());
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setUserId(dto.getUserId());
        transaction.setIpAddress(dto.getIpAddress());
        transaction.setDeviceInfo(dto.getDeviceInfo());
        transaction.setIsFraudulent(dto.getIsFraudulent());
        transaction.setFraudScore(dto.getFraudScore());
        return transaction;
    }

    /**
     * Update existing entity with DTO data
     */
    private void updateEntityFromDto(Transaction transaction, TransactionDto dto) {
        transaction.setTransactionId(dto.getTransactionId());
        transaction.setAmount(dto.getAmount());
        transaction.setCurrency(dto.getCurrency());
        transaction.setTimestamp(dto.getTimestamp());
        transaction.setMerchant(dto.getMerchant());
        transaction.setLocation(dto.getLocation());
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setUserId(dto.getUserId());
        transaction.setIpAddress(dto.getIpAddress());
        transaction.setDeviceInfo(dto.getDeviceInfo());
        transaction.setIsFraudulent(dto.getIsFraudulent());
        transaction.setFraudScore(dto.getFraudScore());
    }
}
