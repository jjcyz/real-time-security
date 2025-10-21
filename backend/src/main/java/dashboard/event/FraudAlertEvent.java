package dashboard.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published to Kafka when fraud is detected
 */
public class FraudAlertEvent {

    private String alertId;
    private Long transactionId;
    private Long userId;
    private String username;
    private BigDecimal amount;
    private Double fraudScore;
    private String fraudReason;
    private LocalDateTime timestamp;
    private String severity; // HIGH, MEDIUM, LOW

    public FraudAlertEvent() {
    }

    public FraudAlertEvent(Long transactionId, Long userId, String username, BigDecimal amount,
                           Double fraudScore, String fraudReason) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.username = username;
        this.amount = amount;
        this.fraudScore = fraudScore;
        this.fraudReason = fraudReason;
        this.timestamp = LocalDateTime.now();
        this.alertId = java.util.UUID.randomUUID().toString();
        this.severity = fraudScore >= 90 ? "HIGH" : fraudScore >= 75 ? "MEDIUM" : "LOW";
    }

    // Getters and Setters
    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Double getFraudScore() {
        return fraudScore;
    }

    public void setFraudScore(Double fraudScore) {
        this.fraudScore = fraudScore;
    }

    public String getFraudReason() {
        return fraudReason;
    }

    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "FraudAlertEvent{" +
                "alertId='" + alertId + '\'' +
                ", transactionId=" + transactionId +
                ", fraudScore=" + fraudScore +
                ", severity='" + severity + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

