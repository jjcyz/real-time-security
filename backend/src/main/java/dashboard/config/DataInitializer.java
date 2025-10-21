package dashboard.config;

import dashboard.model.Transaction;
import dashboard.model.User;
import dashboard.repository.TransactionRepository;
import dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Initialize database with sample data
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void run(String... args) {
        // Only initialize if database is empty
        if (userRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        System.out.println("Initializing database with sample data...");

        // Create sample users
        User user1 = new User("john_doe", "john@example.com", "John Doe");
        User user2 = new User("jane_smith", "jane@example.com", "Jane Smith");
        User user3 = new User("bob_wilson", "bob@example.com", "Bob Wilson");
        User user4 = new User("alice_johnson", "alice@example.com", "Alice Johnson");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);

        System.out.println("Created 4 sample users");

        // Create sample transactions
        Random random = new Random();
        String[] merchants = {"Amazon", "Walmart", "Target", "Best Buy", "Apple Store", "Starbucks", "McDonald's", "Shell Gas", "Uber", "Netflix"};
        String[] categories = {"Electronics", "Groceries", "Retail", "Food & Dining", "Transportation", "Entertainment"};
        String[] locations = {"New York, NY", "Los Angeles, CA", "Chicago, IL", "Houston, TX", "Phoenix, AZ", "Philadelphia, PA"};

        User[] users = {user1, user2, user3, user4};

        // Create 50 legitimate transactions
        for (int i = 0; i < 50; i++) {
            Transaction transaction = new Transaction();
            transaction.setUser(users[random.nextInt(users.length)]);
            transaction.setAmount(BigDecimal.valueOf(10 + random.nextDouble() * 990)); // $10 - $1000
            transaction.setCurrency("USD");
            transaction.setType("PURCHASE");
            transaction.setStatus("COMPLETED");
            transaction.setMerchantName(merchants[random.nextInt(merchants.length)]);
            transaction.setMerchantCategory(categories[random.nextInt(categories.length)]);
            transaction.setTransactionLocation(locations[random.nextInt(locations.length)]);
            transaction.setIpAddress("192.168." + random.nextInt(256) + "." + random.nextInt(256));
            transaction.setDeviceId("device_" + random.nextInt(1000));
            transaction.setIsFraudulent(false);
            transaction.setFraudScore(BigDecimal.valueOf(random.nextDouble() * 30)); // Low fraud score 0-30

            transactionRepository.save(transaction);
        }

        System.out.println("Created 50 legitimate transactions");

        // Create 10 fraudulent transactions
        for (int i = 0; i < 10; i++) {
            Transaction transaction = new Transaction();
            transaction.setUser(users[random.nextInt(users.length)]);
            transaction.setAmount(BigDecimal.valueOf(5000 + random.nextDouble() * 5000)); // $5000 - $10000 (suspicious amounts)
            transaction.setCurrency("USD");
            transaction.setType("PURCHASE");
            transaction.setStatus("COMPLETED");
            transaction.setMerchantName(merchants[random.nextInt(merchants.length)]);
            transaction.setMerchantCategory(categories[random.nextInt(categories.length)]);
            transaction.setTransactionLocation(locations[random.nextInt(locations.length)]);
            transaction.setIpAddress("45.33." + random.nextInt(256) + "." + random.nextInt(256)); // Different IP range
            transaction.setDeviceId("device_suspicious_" + random.nextInt(100));
            transaction.setIsFraudulent(true);
            transaction.setFraudScore(BigDecimal.valueOf(70 + random.nextDouble() * 30)); // High fraud score 70-100
            transaction.setFraudReason(getFraudReason(random));

            transactionRepository.save(transaction);
        }

        System.out.println("Created 10 fraudulent transactions");
        System.out.println("Database initialization complete!");
        System.out.println("Total users: " + userRepository.count());
        System.out.println("Total transactions: " + transactionRepository.count());
    }

    private String getFraudReason(Random random) {
        String[] reasons = {
            "Unusually high transaction amount",
            "Multiple transactions in short time period",
            "Transaction from suspicious location",
            "Unusual merchant category for user",
            "Velocity check failed",
            "High-risk IP address detected"
        };
        return reasons[random.nextInt(reasons.length)];
    }
}

