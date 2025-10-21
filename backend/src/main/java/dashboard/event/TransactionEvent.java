package dashboard.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published to Kafka when a transaction is created
 */
public class TransactionEvent {

    private String eventId;
    private Long transactionId;
    private BigDecimal amount;
    private String currency;
    private String type;
    private Long userId;
    private String username;
    private String merchantName;
    private String merchantCategory;
    private String transactionLocation;
    private String ipAddress;
    private String deviceId;
    private LocalDateTime timestamp;

    public TransactionEvent() {
    }

    public TransactionEvent(Long transactionId, BigDecimal amount, Long userId, String username) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.userId = userId;
        this.username = username;
        this.timestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public void setMerchantCategory(String merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public String getTransactionLocation() {
        return transactionLocation;
    }

    public void setTransactionLocation(String transactionLocation) {
        this.transactionLocation = transactionLocation;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TransactionEvent{" +
                "eventId='" + eventId + '\'' +
                ", transactionId=" + transactionId +
                ", amount=" + amount +
                ", userId=" + userId +
                ", timestamp=" + timestamp +
                '}';
    }
}

