package dashboard.service;

import dashboard.config.KafkaConfig;
import dashboard.event.FraudAlertEvent;
import dashboard.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing events to Kafka
 */
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, TransactionEvent> transactionKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, FraudAlertEvent> fraudAlertKafkaTemplate;

    /**
     * Publish transaction event to Kafka
     */
    public void publishTransactionEvent(TransactionEvent event) {
        String key = event.getUserId().toString();

        CompletableFuture<SendResult<String, TransactionEvent>> future =
            transactionKafkaTemplate.send(KafkaConfig.TRANSACTION_EVENTS_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Published transaction event: {} to partition: {}",
                    event.getEventId(), result.getRecordMetadata().partition());
            } else {
                logger.error("Failed to publish transaction event: {}", event.getEventId(), ex);
            }
        });
    }

    /**
     * Publish fraud alert event to Kafka
     */
    public void publishFraudAlert(FraudAlertEvent alert) {
        String key = alert.getUserId().toString();

        CompletableFuture<SendResult<String, FraudAlertEvent>> future =
            fraudAlertKafkaTemplate.send(KafkaConfig.FRAUD_ALERTS_TOPIC, key, alert);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.warn("🚨 FRAUD ALERT published: {} (Score: {}) to partition: {}",
                    alert.getAlertId(), alert.getFraudScore(), result.getRecordMetadata().partition());
            } else {
                logger.error("Failed to publish fraud alert: {}", alert.getAlertId(), ex);
            }
        });
    }
}

