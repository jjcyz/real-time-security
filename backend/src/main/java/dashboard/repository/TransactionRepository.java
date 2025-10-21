package dashboard.repository;

import dashboard.model.Transaction;
import dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Transaction entity
 * Provides CRUD operations and custom queries for fraud detection
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all fraudulent transactions
     */
    List<Transaction> findByIsFraudulentTrue();

    /**
     * Find all non-fraudulent transactions
     */
    List<Transaction> findByIsFraudulentFalse();

    /**
     * Find transactions by user
     */
    List<Transaction> findByUser(User user);

    /**
     * Find transactions by user ID
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * Find transactions by user with fraud status
     */
    List<Transaction> findByUserAndIsFraudulent(User user, Boolean isFraudulent);

    /**
     * Find transactions above a certain amount
     */
    List<Transaction> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Find transactions in date range
     */
    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Count fraudulent transactions for a user
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.isFraudulent = true")
    Long countFraudulentTransactionsByUser(@Param("user") User user);

    /**
     * Count transactions by user in time window (for velocity checks)
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.createdAt >= :since")
    Long countRecentTransactionsByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Get total transaction amount for user in time window
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.createdAt >= :since")
    BigDecimal sumAmountByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Find transactions with high fraud scores
     */
    @Query("SELECT t FROM Transaction t WHERE t.fraudScore >= :threshold ORDER BY t.fraudScore DESC")
    List<Transaction> findHighRiskTransactions(@Param("threshold") BigDecimal threshold);

    /**
     * Get recent transactions for a user (for pattern analysis)
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactionsByUser(@Param("user") User user);
}

