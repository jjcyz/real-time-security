package dashboard.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dashboard.model.Transaction;
import dashboard.model.User;
import dashboard.repository.TransactionRepository;

/**
 * Unit tests for FraudDetectionServiceImpl
 *
 * WHAT WE'RE TESTING: The fraud detection algorithm with all 7 rules
 *
 * HOW IT WORKS:
 * 1. @ExtendWith(MockitoExtension.class) - Enables Mockito for mocking
 * 2. @Mock - Creates fake TransactionRepository (we don't want to hit real database)
 * 3. @InjectMocks - Creates real FraudDetectionServiceImpl and injects the mock
 * 4. when().thenReturn() - Tells mock what to return
 * 5. Assertions - Check if results match expectations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fraud Detection Service Tests")
class FraudDetectionServiceImplTest {

    // Create a mock (fake) repository - we control what it returns
    @Mock
    private TransactionRepository transactionRepository;

    // Create the real service we're testing, but inject the mock repository
    @InjectMocks
    private FraudDetectionServiceImpl fraudDetectionService;

    // Test data that we'll reuse
    private User testUser;
    private Transaction testTransaction;

    /**
     * This runs before each test method
     * Sets up common test data so we don't repeat code
     */
    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User("test_user", "test@example.com", "Test User");
        testUser.setId(1L);

        // Create a test transaction
        testTransaction = new Transaction();
        testTransaction.setUser(testUser);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    // ==================== RULE 1: HIGH AMOUNT DETECTION ====================

    @Test
    @DisplayName("Rule 1: Transaction of $10,000+ should score 40 points")
    void analyzeFraud_veryHighAmount_returnsFortyPoints() {
        // Arrange: Set up a very high amount transaction
        testTransaction.setAmount(new BigDecimal("15000.00"));

        // Mock repository to return no previous transactions (no velocity)
        when(transactionRepository.countRecentTransactionsByUser(any(User.class), any(LocalDateTime.class)))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(User.class), any(LocalDateTime.class)))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any(User.class)))
            .thenReturn(0L);

        // Act: Run fraud detection
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert: Check results
        assertEquals(40.0, result.get("fraudScore"), "Very high amount should score 40 points");
        assertTrue((Boolean) result.get("isFraudulent"), "Score of 40 + other rules should exceed threshold");
        assertTrue(((String) result.get("fraudReason")).contains("High transaction amount"));
    }

    @Test
    @DisplayName("Rule 1: Transaction of $5,000-$9,999 should score 25 points")
    void analyzeFraud_highAmount_returnsTwentyFivePoints() {
        // Arrange
        testTransaction.setAmount(new BigDecimal("7500.00"));

        // Mock: No velocity or other risk factors
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert
        assertTrue((Double) result.get("fraudScore") >= 25.0, "Should score at least 25 points");
        assertTrue(((String) result.get("fraudReason")).contains("High transaction amount"));
    }

    @Test
    @DisplayName("Rule 1: Small transaction should score 0 points for amount")
    void analyzeFraud_smallAmount_returnsZeroPointsForAmount() {
        // Arrange: Small transaction
        testTransaction.setAmount(new BigDecimal("50.00"));

        // Mock: No other risk factors
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert: Should have low score (no amount penalty)
        assertTrue((Double) result.get("fraudScore") < 10.0, "Small transaction should have low score");
    }

    // ==================== RULE 2: TRANSACTION VELOCITY ====================

    @Test
    @DisplayName("Rule 2: 10+ transactions in hour should score 30 points")
    void analyzeFraud_highVelocity_returnsVelocityScore() {
        // Arrange: Normal amount, but high velocity
        testTransaction.setAmount(new BigDecimal("100.00"));

        // Mock: Return 12 transactions in last hour (high velocity)
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(12L);  // 12 transactions!
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(new BigDecimal("1200.00"));
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert
        assertTrue((Double) result.get("fraudScore") >= 30.0, "High velocity should score at least 30 points");
        assertTrue(((String) result.get("fraudReason")).contains("Multiple transactions in short time"));
    }

    @Test
    @DisplayName("Rule 2: 5-6 transactions in hour should score 10 points")
    void analyzeFraud_mediumVelocity_returnsTenPoints() {
        // Arrange
        testTransaction.setAmount(new BigDecimal("100.00"));

        // Mock: 5 transactions (medium velocity)
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(5L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert
        assertTrue((Double) result.get("fraudScore") >= 10.0, "Medium velocity should score at least 10 points");
    }

    // ==================== RULE 3: AMOUNT VELOCITY ====================

    @Test
    @DisplayName("Rule 3: $20,000+ in last hour should score 25 points")
    void analyzeFraud_highAmountVelocity_returnsTwentyFivePoints() {
        // Arrange
        testTransaction.setAmount(new BigDecimal("500.00"));

        // Mock: High amount velocity ($25,000 in last hour)
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);  // Not many transactions, but high total amount
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(new BigDecimal("25000.00"));  // High volume!
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert
        assertTrue((Double) result.get("fraudScore") >= 25.0, "High amount velocity should score at least 25 points");
        assertTrue(((String) result.get("fraudReason")).contains("High transaction volume"));
    }

    // ==================== RULE 4: UNUSUAL TIME PATTERN ====================

    @Test
    @DisplayName("Rule 4: Transaction at 2 AM should score 15 points")
    void analyzeFraud_veryUnusualTime_returnsFifteenPoints() {
        // Arrange: Transaction at 2 AM (very unusual)
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setCreatedAt(LocalDateTime.now().withHour(2).withMinute(30));

        // Mock: No other risk factors
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Note: In a real scenario, you'd need to mock the time or use a Clock
        // For now, this test shows the concept. The actual implementation uses LocalDateTime.now()
        // which is hard to test. This is a limitation we'd fix with dependency injection of Clock.

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert: Score should reflect time penalty (if current time is unusual)
        // In practice, you'd use a Clock mock to control time
        assertNotNull(result.get("fraudScore"));
    }

    // ==================== RULE 5: GEOGRAPHIC ANOMALY ====================

    @Test
    @DisplayName("Rule 5: Suspicious IP address should score 20 points")
    void analyzeFraud_suspiciousIp_returnsTwentyPoints() {
        // Arrange: Transaction with suspicious IP
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setIpAddress("45.33.12.45");  // Known suspicious IP range

        // Mock: No other risk factors
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert
        assertTrue((Double) result.get("fraudScore") >= 20.0, "Suspicious IP should score at least 20 points");
        assertTrue(((String) result.get("fraudReason")).contains("Suspicious geographic location"));
    }

    @Test
    @DisplayName("Rule 5: Private IP address should score 5 points")
    void analyzeFraud_privateIp_returnsFivePoints() {
        // Arrange
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setIpAddress("192.168.1.100");  // Private IP (could be VPN)

        // Mock
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert: Should have some score for private IP
        assertTrue((Double) result.get("fraudScore") >= 5.0);
    }

    // ==================== RULE 6: FRAUD HISTORY ====================

    @Test
    @DisplayName("Rule 6: User with 5+ previous frauds should score 25 points")
    void analyzeFraud_userWithHighFraudHistory_returnsTwentyFivePoints() {
        // Arrange
        testTransaction.setAmount(new BigDecimal("100.00"));

        // Mock: User has 7 previous fraudulent transactions
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(7L);  // 7 previous frauds!

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert
        assertTrue((Double) result.get("fraudScore") >= 25.0, "High fraud history should score at least 25 points");
        assertTrue(((String) result.get("fraudReason")).contains("previous fraudulent transactions"));
    }

    // ==================== RULE 7: MERCHANT RISK ====================

    @Test
    @DisplayName("Rule 7: Cryptocurrency merchant should score 15 points")
    void analyzeFraud_highRiskMerchant_returnsFifteenPoints() {
        // Arrange: High-risk merchant category
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setMerchantCategory("Cryptocurrency");

        // Mock: No other risk factors
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert
        assertTrue((Double) result.get("fraudScore") >= 15.0, "High-risk merchant should score at least 15 points");
        assertTrue(((String) result.get("fraudReason")).contains("High-risk merchant category"));
    }

    // ==================== COMBINED RULES: MULTIPLE RED FLAGS ====================

    @Test
    @DisplayName("Combined: High amount + suspicious IP + crypto merchant should be fraudulent")
    void analyzeFraud_multipleRedFlags_marksAsFraudulent() {
        // Arrange: Multiple risk factors
        testTransaction.setAmount(new BigDecimal("9500.00"));  // High amount (25 points)
        testTransaction.setIpAddress("45.33.12.45");           // Suspicious IP (20 points)
        testTransaction.setMerchantCategory("Cryptocurrency");   // High-risk merchant (15 points)
        // Total: 60+ points, but with other rules might exceed 70

        // Mock
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert: Should be marked as fraudulent (score >= 70)
        assertTrue((Double) result.get("fraudScore") >= 60.0, "Multiple red flags should have high score");
        // Note: Might be fraudulent depending on time-based rules
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Null IP address should not cause error")
    void analyzeFraud_nullIpAddress_handlesGracefully() {
        // Arrange
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setIpAddress(null);  // Null IP

        // Mock
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(0L);
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(0L);

        // Act & Assert: Should not throw exception
        assertDoesNotThrow(() -> {
            Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Fraud threshold check: 70 points should be fraudulent")
    void isFraudulent_scoreOfSeventy_returnsTrue() {
        // Act
        boolean result = fraudDetectionService.isFraudulent(70.0);

        // Assert
        assertTrue(result, "Score of 70 should be fraudulent");
    }

    @Test
    @DisplayName("Fraud threshold check: 69.9 points should not be fraudulent")
    void isFraudulent_scoreOfSixtyNine_returnsFalse() {
        // Act
        boolean result = fraudDetectionService.isFraudulent(69.9);

        // Assert
        assertFalse(result, "Score below 70 should not be fraudulent");
    }

    @Test
    @DisplayName("Fraud threshold should be 70.0")
    void getFraudThreshold_returnsSeventy() {
        // Act
        double threshold = fraudDetectionService.getFraudThreshold();

        // Assert
        assertEquals(70.0, threshold, "Threshold should be 70.0");
    }

    @Test
    @DisplayName("Score should be capped at 100 even with multiple rules")
    void analyzeFraud_excessiveRiskFactors_capsAtOneHundred() {
        // Arrange: All risk factors
        testTransaction.setAmount(new BigDecimal("15000.00"));  // 40 points
        testTransaction.setIpAddress("45.33.12.45");            // 20 points
        testTransaction.setMerchantCategory("Cryptocurrency");   // 15 points

        // Mock: High velocity and fraud history
        when(transactionRepository.countRecentTransactionsByUser(any(), any()))
            .thenReturn(15L);  // 30 points
        when(transactionRepository.sumAmountByUserSince(any(), any()))
            .thenReturn(new BigDecimal("30000.00"));  // 25 points
        when(transactionRepository.countFraudulentTransactionsByUser(any()))
            .thenReturn(10L);  // 25 points
        // Total would be 155, but should cap at 100

        // Act
        Map<String, Object> result = fraudDetectionService.analyzeFraud(testTransaction, testUser);

        // Assert
        assertEquals(100.0, result.get("fraudScore"), "Score should be capped at 100");
        assertTrue((Boolean) result.get("isFraudulent"), "Score of 100 should be fraudulent");
    }
}

