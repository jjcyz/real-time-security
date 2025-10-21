package dashboard.service.impl;

import dashboard.model.Transaction;
import dashboard.model.User;
import dashboard.repository.TransactionRepository;
import dashboard.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of fraud detection with multiple detection strategies
 */
@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private static final double FRAUD_THRESHOLD = 70.0;

    // Rule thresholds
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("5000.00");
    private static final BigDecimal VERY_HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000.00");
    private static final int VELOCITY_CHECK_MINUTES = 60;
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    private static final BigDecimal MAX_AMOUNT_PER_HOUR = new BigDecimal("20000.00");

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Map<String, Object> analyzeFraud(Transaction transaction, User user) {
        double totalScore = 0.0;
        List<String> reasons = new ArrayList<>();
        Map<String, Double> ruleScores = new HashMap<>();

        // Rule 1: High Amount Detection
        double amountScore = checkHighAmount(transaction);
        if (amountScore > 0) {
            totalScore += amountScore;
            reasons.add(String.format("High transaction amount ($%.2f)", transaction.getAmount()));
            ruleScores.put("high_amount", amountScore);
        }

        // Rule 2: Velocity Check - Count of recent transactions
        double velocityScore = checkTransactionVelocity(user);
        if (velocityScore > 0) {
            totalScore += velocityScore;
            reasons.add("Multiple transactions in short time period");
            ruleScores.put("velocity", velocityScore);
        }

        // Rule 3: Amount Velocity - Sum of recent transaction amounts
        double amountVelocityScore = checkAmountVelocity(user);
        if (amountVelocityScore > 0) {
            totalScore += amountVelocityScore;
            reasons.add("High transaction volume in short time period");
            ruleScores.put("amount_velocity", amountVelocityScore);
        }

        // Rule 4: Unusual Time Pattern
        double timeScore = checkUnusualTime(transaction);
        if (timeScore > 0) {
            totalScore += timeScore;
            reasons.add("Transaction at unusual time");
            ruleScores.put("unusual_time", timeScore);
        }

        // Rule 5: Geographic Anomaly (simplified - checking IP range)
        double geoScore = checkGeographicAnomaly(transaction);
        if (geoScore > 0) {
            totalScore += geoScore;
            reasons.add("Suspicious geographic location");
            ruleScores.put("geographic", geoScore);
        }

        // Rule 6: User's Fraud History
        double historyScore = checkFraudHistory(user);
        if (historyScore > 0) {
            totalScore += historyScore;
            reasons.add("User has previous fraudulent transactions");
            ruleScores.put("fraud_history", historyScore);
        }

        // Rule 7: Merchant Category Risk
        double merchantScore = checkMerchantRisk(transaction);
        if (merchantScore > 0) {
            totalScore += merchantScore;
            reasons.add("High-risk merchant category");
            ruleScores.put("merchant_risk", merchantScore);
        }

        // Cap the score at 100
        totalScore = Math.min(totalScore, 100.0);

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("fraudScore", totalScore);
        result.put("isFraudulent", isFraudulent(totalScore));
        result.put("fraudReason", reasons.isEmpty() ? "No fraud indicators detected" : String.join("; ", reasons));
        result.put("ruleScores", ruleScores);
        result.put("reasonCount", reasons.size());

        return result;
    }

    @Override
    public boolean isFraudulent(double fraudScore) {
        return fraudScore >= FRAUD_THRESHOLD;
    }

    @Override
    public double getFraudThreshold() {
        return FRAUD_THRESHOLD;
    }

    /**
     * Rule 1: Check for high transaction amounts
     * Score: 0-40 points
     */
    private double checkHighAmount(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();

        if (amount.compareTo(VERY_HIGH_AMOUNT_THRESHOLD) >= 0) {
            return 40.0; // $10,000+
        } else if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            return 25.0; // $5,000+
        } else if (amount.compareTo(new BigDecimal("2000.00")) >= 0) {
            return 10.0; // $2,000+
        }

        return 0.0;
    }

    /**
     * Rule 2: Check transaction velocity (count of transactions)
     * Score: 0-30 points
     */
    private double checkTransactionVelocity(User user) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(VELOCITY_CHECK_MINUTES);
        Long recentCount = transactionRepository.countRecentTransactionsByUser(user, since);

        if (recentCount >= MAX_TRANSACTIONS_PER_HOUR) {
            return 30.0; // 10+ transactions in an hour
        } else if (recentCount >= 7) {
            return 20.0; // 7-9 transactions
        } else if (recentCount >= 5) {
            return 10.0; // 5-6 transactions
        }

        return 0.0;
    }

    /**
     * Rule 3: Check amount velocity (sum of transaction amounts)
     * Score: 0-25 points
     */
    private double checkAmountVelocity(User user) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(VELOCITY_CHECK_MINUTES);
        BigDecimal recentAmount = transactionRepository.sumAmountByUserSince(user, since);

        if (recentAmount.compareTo(MAX_AMOUNT_PER_HOUR) >= 0) {
            return 25.0; // $20,000+ in an hour
        } else if (recentAmount.compareTo(new BigDecimal("10000.00")) >= 0) {
            return 15.0; // $10,000+ in an hour
        } else if (recentAmount.compareTo(new BigDecimal("5000.00")) >= 0) {
            return 8.0; // $5,000+ in an hour
        }

        return 0.0;
    }

    /**
     * Rule 4: Check for unusual transaction times
     * Score: 0-15 points
     */
    private double checkUnusualTime(Transaction transaction) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        // Suspicious hours: 1 AM - 5 AM
        if (hour >= 1 && hour <= 5) {
            return 15.0;
        }
        // Slightly suspicious: 11 PM - 1 AM or 5 AM - 7 AM
        else if ((hour >= 23) || (hour >= 5 && hour <= 7)) {
            return 8.0;
        }

        return 0.0;
    }

    /**
     * Rule 5: Check for geographic anomalies
     * Score: 0-20 points
     * (Simplified: checks IP address ranges)
     */
    private double checkGeographicAnomaly(Transaction transaction) {
        String ipAddress = transaction.getIpAddress();

        if (ipAddress == null) {
            return 0.0;
        }

        // Check for suspicious IP ranges (simplified)
        // In production, you'd use a geo-IP database
        if (ipAddress.startsWith("45.") || ipAddress.startsWith("185.")) {
            return 20.0; // Known high-risk IP ranges
        } else if (ipAddress.startsWith("10.") || ipAddress.startsWith("172.") || ipAddress.startsWith("192.168.")) {
            return 5.0; // Private IPs (could be VPN/proxy)
        }

        return 0.0;
    }

    /**
     * Rule 6: Check user's fraud history
     * Score: 0-25 points
     */
    private double checkFraudHistory(User user) {
        Long fraudCount = transactionRepository.countFraudulentTransactionsByUser(user);

        if (fraudCount >= 5) {
            return 25.0; // 5+ previous fraudulent transactions
        } else if (fraudCount >= 3) {
            return 18.0; // 3-4 previous fraudulent transactions
        } else if (fraudCount >= 1) {
            return 10.0; // 1-2 previous fraudulent transactions
        }

        return 0.0;
    }

    /**
     * Rule 7: Check merchant category risk
     * Score: 0-15 points
     */
    private double checkMerchantRisk(Transaction transaction) {
        String category = transaction.getMerchantCategory();

        if (category == null) {
            return 0.0;
        }

        // High-risk categories
        Set<String> highRisk = Set.of("Wire Transfer", "Cryptocurrency", "Gift Cards", "Money Services");
        if (highRisk.stream().anyMatch(risk -> category.toLowerCase().contains(risk.toLowerCase()))) {
            return 15.0;
        }

        // Medium-risk categories
        Set<String> mediumRisk = Set.of("Electronics", "Jewelry", "Travel");
        if (mediumRisk.stream().anyMatch(risk -> category.toLowerCase().contains(risk.toLowerCase()))) {
            return 8.0;
        }

        return 0.0;
    }
}

