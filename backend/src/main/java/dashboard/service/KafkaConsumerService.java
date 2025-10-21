package dashboard.service;

import dashboard.config.KafkaConfig;
import dashboard.event.FraudAlertEvent;
import dashboard.event.TransactionEvent;
import dashboard.model.Transaction;
import dashboard.model.User;
import dashboard.repository.TransactionRepository;
import dashboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service for consuming events from Kafka
 */
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * Consume transaction events and perform fraud detection
     */
    @KafkaListener(topics = KafkaConfig.TRANSACTION_EVENTS_TOPIC, groupId = "fraud-detection-group")
    public void consumeTransactionEvent(TransactionEvent event) {
        logger.info("📥 Consumed transaction event: {} (Amount: ${})",
            event.getEventId(), event.getAmount());

        try {
            // Get transaction from database
            Transaction transaction = transactionRepository.findById(event.getTransactionId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            User user = transaction.getUser();

            // Perform fraud detection
            Map<String, Object> fraudAnalysis = fraudDetectionService.analyzeFraud(transaction, user);

            // Update transaction with fraud analysis results
            transaction.setIsFraudulent((Boolean) fraudAnalysis.get("isFraudulent"));
            transaction.setFraudScore(BigDecimal.valueOf((Double) fraudAnalysis.get("fraudScore")));
            transaction.setFraudReason((String) fraudAnalysis.get("fraudReason"));
            transactionRepository.save(transaction);

            logger.info("✅ Fraud analysis completed for transaction {}: Score={}, Fraudulent={}",
                transaction.getId(),
                fraudAnalysis.get("fraudScore"),
                fraudAnalysis.get("isFraudulent"));

            // If fraudulent, publish fraud alert
            if ((Boolean) fraudAnalysis.get("isFraudulent")) {
                FraudAlertEvent alert = new FraudAlertEvent(
                    transaction.getId(),
                    user.getId(),
                    user.getUsername(),
                    transaction.getAmount(),
                    (Double) fraudAnalysis.get("fraudScore"),
                    (String) fraudAnalysis.get("fraudReason")
                );
                kafkaProducerService.publishFraudAlert(alert);
            }

        } catch (Exception e) {
            logger.error("❌ Error processing transaction event: {}", event.getEventId(), e);
        }
    }

    /**
     * Consume fraud alerts (for logging/monitoring)
     */
    @KafkaListener(topics = KafkaConfig.FRAUD_ALERTS_TOPIC, groupId = "fraud-alerts-group")
    public void consumeFraudAlert(FraudAlertEvent alert) {
        logger.warn("🚨🚨🚨 FRAUD ALERT RECEIVED 🚨🚨🚨");
        logger.warn("Alert ID: {}", alert.getAlertId());
        logger.warn("Transaction ID: {}", alert.getTransactionId());
        logger.warn("User: {} (ID: {})", alert.getUsername(), alert.getUserId());
        logger.warn("Amount: ${}", alert.getAmount());
        logger.warn("Fraud Score: {}", alert.getFraudScore());
        logger.warn("Reason: {}", alert.getFraudReason());
        logger.warn("Severity: {}", alert.getSeverity());
        logger.warn("🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨");

        // In production: Send email, SMS, push notification, etc.
    }
}

