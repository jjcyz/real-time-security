package dashboard.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Transaction.
 * Equivalent to your Python Pydantic models
 */
public class TransactionDto {

    private Long id;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    private String currency = "USD";

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @NotBlank(message = "Merchant is required")
    private String merchant;

    private String location;
    private String paymentMethod;
    private String userId;
    private String ipAddress;
    private String deviceInfo;
    private Boolean isFraudulent = false;
    private Double fraudScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public TransactionDto() {}

    public TransactionDto(String transactionId, BigDecimal amount, String merchant, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.merchant = merchant;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Boolean getIsFraudulent() {
        return isFraudulent;
    }

    public void setIsFraudulent(Boolean isFraudulent) {
        this.isFraudulent = isFraudulent;
    }

    public Double getFraudScore() {
        return fraudScore;
    }

    public void setFraudScore(Double fraudScore) {
        this.fraudScore = fraudScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
