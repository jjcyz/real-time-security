package dashboard.repository;

import dashboard.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity.
 * Spring Data JPA automatically implements this interface.
 * Equivalent to your Python database queries
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transaction by transaction ID
     */
    Optional<Transaction> findByTransactionId(String transactionId);

    /**
     * Find transactions by user ID
     */
    List<Transaction> findByUserId(String userId);

    /**
     * Find transactions by merchant
     */
    List<Transaction> findByMerchant(String merchant);

    /**
     * Find fraudulent transactions
     */
    List<Transaction> findByIsFraudulentTrue();

    /**
     * Find transactions within amount range
     */
    List<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find transactions within time range
     */
    List<Transaction> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find transactions by user ID within time range
     */
    List<Transaction> findByUserIdAndTimestampBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find duplicate transactions (same amount, merchant, within time range)
     */
    List<Transaction> findByAmountAndMerchantAndTimestampBetween(
            BigDecimal amount,
            String merchant,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * Find high-value transactions (above threshold)
     */
    @Query("SELECT t FROM Transaction t WHERE t.amount > :threshold")
    List<Transaction> findHighValueTransactions(@Param("threshold") BigDecimal threshold);

    /**
     * Find off-hours transactions (11 PM - 6 AM)
     */
    @Query("SELECT t FROM Transaction t WHERE HOUR(t.timestamp) >= 23 OR HOUR(t.timestamp) <= 6")
    List<Transaction> findOffHoursTransactions();

    /**
     * Find rapid successive transactions for a user
     */
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.timestamp BETWEEN :startTime AND :endTime")
    List<Transaction> findRapidSuccessiveTransactions(
            @Param("userId") String userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Get transaction statistics
     */
    @Query("SELECT COUNT(t) FROM Transaction t")
    long getTotalTransactionCount();

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isFraudulent = true")
    long getFraudulentTransactionCount();

    @Query("SELECT AVG(t.amount) FROM Transaction t")
    BigDecimal getAverageTransactionAmount();

    @Query("SELECT MAX(t.amount) FROM Transaction t")
    BigDecimal getMaxTransactionAmount();

    @Query("SELECT MIN(t.amount) FROM Transaction t")
    BigDecimal getMinTransactionAmount();

    /**
     * Find transactions by fraud score range
     */
    List<Transaction> findByFraudScoreBetween(Double minScore, Double maxScore);

    /**
     * Find transactions by payment method
     */
    List<Transaction> findByPaymentMethod(String paymentMethod);

    /**
     * Find transactions by location
     */
    List<Transaction> findByLocation(String location);

    /**
     * Find transactions by IP address
     */
    List<Transaction> findByIpAddress(String ipAddress);

    /**
     * Get recent transactions (last 24 hours)
     */
    @Query("SELECT t FROM Transaction t WHERE t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<Transaction> findRecentTransactions(@Param("since") LocalDateTime since);

    /**
     * Get transactions by merchant with pagination
     */
    Page<Transaction> findByMerchant(String merchant, Pageable pageable);

    /**
     * Get transactions by user ID with pagination
     */
    Page<Transaction> findByUserId(String userId, Pageable pageable);

    /**
     * Get fraudulent transactions with pagination
     */
    Page<Transaction> findByIsFraudulentTrue(Pageable pageable);

    /**
     * Get transactions by amount range with pagination
     */
    Page<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Get transactions by time range with pagination
     */
    Page<Transaction> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
