package dashboard.service;

import dashboard.model.Transaction;
import dashboard.model.User;

import java.util.Map;

/**
 * Service interface for fraud detection
 */
public interface FraudDetectionService {

    /**
     * Analyze a transaction for fraud
     * Returns a map with fraud score and reason
     */
    Map<String, Object> analyzeFraud(Transaction transaction, User user);

    /**
     * Check if a transaction is fraudulent based on threshold
     */
    boolean isFraudulent(double fraudScore);

    /**
     * Get fraud threshold (scores above this are considered fraudulent)
     */
    double getFraudThreshold();
}

