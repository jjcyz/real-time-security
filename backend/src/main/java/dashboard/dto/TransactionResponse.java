package dashboard.dto;

import dashboard.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction responses
 */
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private String currency;
    private String type;
    private String status;
    private String merchantName;
    private String merchantCategory;
    private String transactionLocation;
    private String ipAddress;
    private String deviceId;
    private Boolean isFraudulent;
    private BigDecimal fraudScore;
    private String fraudReason;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;

    // Constructor from Entity
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.amount = transaction.getAmount();
        this.currency = transaction.getCurrency();
        this.type = transaction.getType();
        this.status = transaction.getStatus();
        this.merchantName = transaction.getMerchantName();
        this.merchantCategory = transaction.getMerchantCategory();
        this.transactionLocation = transaction.getTransactionLocation();
        this.ipAddress = transaction.getIpAddress();
        this.deviceId = transaction.getDeviceId();
        this.isFraudulent = transaction.getIsFraudulent();
        this.fraudScore = transaction.getFraudScore();
        this.fraudReason = transaction.getFraudReason();
        if (transaction.getUser() != null) {
            this.userId = transaction.getUser().getId();
            this.username = transaction.getUser().getUsername();
        }
        this.createdAt = transaction.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getIsFraudulent() {
        return isFraudulent;
    }

    public void setIsFraudulent(Boolean isFraudulent) {
        this.isFraudulent = isFraudulent;
    }

    public BigDecimal getFraudScore() {
        return fraudScore;
    }

    public void setFraudScore(BigDecimal fraudScore) {
        this.fraudScore = fraudScore;
    }

    public String getFraudReason() {
        return fraudReason;
    }

    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

