package dashboard.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import dashboard.model.Transaction;
import dashboard.model.User;
import dashboard.repository.TransactionRepository;
import dashboard.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional  // Each test runs in a transaction that gets rolled back
@DisplayName("Transaction Controller Integration Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;  // Simulates HTTP requests

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;  // Converts Java objects to JSON

    private User testUser;

    /**
     * Set up test data before each test
     * This runs in a transaction that gets rolled back after the test
     */
    @BeforeEach
    void setUp() {
        // Clean up any existing data (though @Transactional should handle this)
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        testUser = new User("test_user", "test@example.com", "Test User");
        testUser = userRepository.save(testUser);
    }

    // ==================== GET /api/transactions ====================

    @Test
    @DisplayName("GET /api/transactions should return empty list when no transactions exist")
    void getAllTransactions_emptyDatabase_returnsEmptyList() throws Exception {
        // Act & Assert: Make HTTP GET request and verify response
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isOk())  // HTTP 200
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", is(empty())));  // Empty array
    }

    @Test
    @DisplayName("GET /api/transactions should return all transactions")
    void getAllTransactions_withExistingTransactions_returnsAllTransactions() throws Exception {
        // Arrange: Create some transactions in database
        Transaction tx1 = createTestTransaction(new BigDecimal("50.00"));
        Transaction tx2 = createTestTransaction(new BigDecimal("100.00"));

        // Act & Assert
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))  // Should have 2 transactions
            .andExpect(jsonPath("$[0].amount", is(50.0)))
            .andExpect(jsonPath("$[1].amount", is(100.0)));
    }

    // ==================== GET /api/transactions/{id} ====================

    @Test
    @DisplayName("GET /api/transactions/{id} should return transaction when exists")
    void getTransactionById_existingId_returnsTransaction() throws Exception {
        // Arrange
        Transaction transaction = createTestTransaction(new BigDecimal("75.50"));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/" + transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(transaction.getId().intValue())))
            .andExpect(jsonPath("$.amount", is(75.50)))
            .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/transactions/{id} should return 404 when transaction not found")
    void getTransactionById_nonExistentId_returnsNotFound() throws Exception {
        // Act & Assert: Request non-existent ID
        mockMvc.perform(get("/api/transactions/99999"))
            .andExpect(status().isNotFound())  // HTTP 404
            .andExpect(jsonPath("$.error", containsString("Transaction not found")));
    }

    // ==================== POST /api/transactions ====================

    @Test
    @DisplayName("POST /api/transactions should create legitimate transaction")
    void createTransaction_legitimateTransaction_createsAndAnalyzes() throws Exception {
        // Arrange: Prepare JSON request body
        String requestBody = """
            {
                "amount": 50.00,
                "userId": %d,
                "merchantName": "Starbucks",
                "merchantCategory": "Food & Dining",
                "ipAddress": "192.168.1.100"
            }
            """.formatted(testUser.getId());

        // Act & Assert: Make POST request
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())  // HTTP 201
            .andExpect(jsonPath("$.transaction").exists())
            .andExpect(jsonPath("$.transaction.amount", is(50.0)))
            .andExpect(jsonPath("$.fraudAnalysis").exists())
            .andExpect(jsonPath("$.fraudAnalysis.fraudScore").exists());

        // Verify it was saved to database
        assertEquals(1L, transactionRepository.count());
    }

    @Test
    @DisplayName("POST /api/transactions should detect fraudulent transaction")
    void createTransaction_fraudulentTransaction_marksAsFraudulent() throws Exception {
        // Arrange: High amount + suspicious IP + high-risk merchant
        String requestBody = """
            {
                "amount": 9500.00,
                "userId": %d,
                "merchantName": "Crypto Exchange",
                "merchantCategory": "Cryptocurrency",
                "ipAddress": "45.33.12.45"
            }
            """.formatted(testUser.getId());

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fraudAnalysis.isFraudulent", is(true)))
            .andExpect(jsonPath("$.fraudAnalysis.fraudScore", greaterThanOrEqualTo(70.0)));
    }

    @Test
    @DisplayName("POST /api/transactions should validate required fields")
    void createTransaction_missingRequiredFields_returnsBadRequest() throws Exception {
        // Arrange: Missing userId (required field)
        String requestBody = """
            {
                "amount": 50.00
            }
            """;

        // Act & Assert: Should return 400 Bad Request
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/transactions should reject invalid user ID")
    void createTransaction_invalidUserId_returnsError() throws Exception {
        // Arrange: User ID that doesn't exist
        String requestBody = """
            {
                "amount": 50.00,
                "userId": 99999
            }
            """;

        // Act & Assert: Should return 500 (RuntimeException thrown)
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("POST /api/transactions should reject negative amount")
    void createTransaction_negativeAmount_returnsBadRequest() throws Exception {
        // Arrange: Negative amount (invalid)
        String requestBody = """
            {
                "amount": -10.00,
                "userId": %d
            }
            """.formatted(testUser.getId());

        // Act & Assert: Should return 400 Bad Request (validation error)
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    // ==================== GET /api/transactions/suspicious ====================

    @Test
    @DisplayName("GET /api/transactions/suspicious should return only fraudulent transactions")
    void getSuspiciousTransactions_withFraudulentAndLegitimate_returnsOnlyFraudulent() throws Exception {
        // Arrange: Create both fraudulent and legitimate transactions
        Transaction legitimate = createTestTransaction(new BigDecimal("50.00"));
        legitimate.setIsFraudulent(false);
        legitimate.setFraudScore(new BigDecimal("10.0"));
        transactionRepository.save(legitimate);

        Transaction fraudulent = createTestTransaction(new BigDecimal("9500.00"));
        fraudulent.setIsFraudulent(true);
        fraudulent.setFraudScore(new BigDecimal("85.0"));
        transactionRepository.save(fraudulent);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/suspicious"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))  // Only 1 fraudulent
            .andExpect(jsonPath("$[0].isFraudulent", is(true)));
    }

    // ==================== GET /api/transactions/user/{userId} ====================

    @Test
    @DisplayName("GET /api/transactions/user/{userId} should return user's transactions")
    void getTransactionsByUser_withMultipleUsers_returnsOnlyUserTransactions() throws Exception {
        // Arrange: Create transactions for two different users
        User user2 = userRepository.save(new User("user2", "user2@test.com", "User 2"));

        Transaction tx1 = createTestTransaction(new BigDecimal("50.00"));  // testUser's transaction
        Transaction tx2 = createTestTransaction(new BigDecimal("100.00")); // testUser's transaction

        Transaction tx3 = new Transaction();
        tx3.setAmount(new BigDecimal("200.00"));
        tx3.setUser(user2);
        transactionRepository.save(tx3);

        // Act & Assert: Should only return testUser's transactions
        mockMvc.perform(get("/api/transactions/user/" + testUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))  // Only testUser's 2 transactions
            .andExpect(jsonPath("$[0].userId", is(testUser.getId().intValue())))
            .andExpect(jsonPath("$[1].userId", is(testUser.getId().intValue())));
    }

    // ==================== GET /api/stats ====================

    @Test
    @DisplayName("GET /api/stats should return correct statistics")
    void getStats_withTransactions_returnsCorrectStatistics() throws Exception {
        // Arrange: Create some transactions
        Transaction tx1 = createTestTransaction(new BigDecimal("50.00"));
        tx1.setIsFraudulent(false);
        transactionRepository.save(tx1);

        Transaction tx2 = createTestTransaction(new BigDecimal("100.00"));
        tx2.setIsFraudulent(true);
        transactionRepository.save(tx2);

        // Act & Assert
        mockMvc.perform(get("/api/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTransactions", is(2)))
            .andExpect(jsonPath("$.fraudulentTransactions", is(1)))
            .andExpect(jsonPath("$.legitimateTransactions", is(1)))
            .andExpect(jsonPath("$.totalUsers", is(1)));
    }

    private Transaction createTestTransaction(BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setUser(testUser);
        return transactionRepository.save(transaction);
    }
}

