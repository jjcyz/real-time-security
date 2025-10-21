package dashboard.dto;

import dashboard.model.User;
import java.time.LocalDateTime;

/**
 * DTO for user responses
 */
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String accountStatus;
    private LocalDateTime createdAt;
    private Long transactionCount;

    // Constructor from Entity
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.accountStatus = user.getAccountStatus();
        this.createdAt = user.getCreatedAt();
        // Don't access lazy-loaded transactions
        this.transactionCount = 0L;
    }

    // Constructor with transaction count
    public UserResponse(User user, Long transactionCount) {
        this(user);
        this.transactionCount = transactionCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }
}

