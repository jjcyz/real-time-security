# PostgreSQL + JPA Setup Guide

## Overview

The application now uses **JPA/Hibernate** with support for both **H2** (development) and **PostgreSQL** (production).

## Database Profiles

### Development Profile (H2 - In-Memory)
```bash
# Default profile - starts automatically
mvn spring-boot:run
```
- **Database:** H2 in-memory
- **Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:securitydb`
  - Username: `sa`
  - Password: (empty)
- **Data:** Auto-populated with 60 sample transactions (50 legitimate, 10 fraudulent)

### Production Profile (PostgreSQL)
```bash
# Start PostgreSQL with Docker
docker-compose up -d

# Run application with prod profile
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod

# Or with environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/security_dashboard
export DATABASE_USER=postgres
export DATABASE_PASSWORD=postgres
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod
```

## Docker Commands

```bash
# Start PostgreSQL
docker-compose up -d

# View logs
docker-compose logs -f postgres

# Stop PostgreSQL
docker-compose down

# Stop and remove data
docker-compose down -v
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    account_status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    type VARCHAR(50) DEFAULT 'PURCHASE',
    status VARCHAR(20) DEFAULT 'COMPLETED',
    merchant_name VARCHAR(100),
    merchant_category VARCHAR(50),
    transaction_location VARCHAR(100),
    ip_address VARCHAR(45),
    device_id VARCHAR(100),
    is_fraudulent BOOLEAN DEFAULT FALSE,
    fraud_score DECIMAL(5,2),
    fraud_reason VARCHAR(500),
    user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Indexes
- `idx_user_email` on users(email)
- `idx_user_created` on users(created_at)
- `idx_transaction_user` on transactions(user_id)
- `idx_transaction_created` on transactions(created_at)
- `idx_transaction_fraudulent` on transactions(is_fraudulent)
- `idx_transaction_status` on transactions(status)

## API Endpoints

### General
- `GET /api` - API info and endpoints
- `GET /api/stats` - Database statistics

### Transactions
- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `POST /api/transactions` - Create new transaction
- `GET /api/transactions/suspicious` - Get fraudulent transactions
- `GET /api/transactions/user/{userId}` - Get transactions by user

### Users
- `GET /api/users` - Get all users

## Sample API Requests

### Create Transaction
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 150.50,
    "userId": 1,
    "currency": "USD",
    "type": "PURCHASE",
    "merchantName": "Amazon",
    "merchantCategory": "Electronics",
    "transactionLocation": "New York, NY",
    "ipAddress": "192.168.1.100",
    "deviceId": "device_123"
  }'
```

### Get All Transactions
```bash
curl http://localhost:8080/api/transactions
```

### Get Suspicious Transactions
```bash
curl http://localhost:8080/api/transactions/suspicious
```

### Get Statistics
```bash
curl http://localhost:8080/api/stats
```

## Technology Stack

- **Spring Boot:** 3.5.6
- **Java:** 21
- **JPA/Hibernate:** 6.x
- **PostgreSQL:** 16 (Alpine)
- **H2:** Latest (dev only)
- **Connection Pool:** HikariCP
- **Validation:** Jakarta Bean Validation

## Performance Optimizations

### HikariCP Configuration
- Maximum pool size: 10 connections
- Minimum idle: 5 connections
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes

### Hibernate Optimizations
- Batch insert/update: 20 entities
- Order inserts: Enabled
- Order updates: Enabled
- SQL formatting: Enabled (dev)
- SQL comments: Enabled

## Troubleshooting

### Port Already in Use
```bash
# Check what's using port 5432
lsof -i :5432

# Kill the process
kill -9 <PID>
```

### Connection Refused
```bash
# Make sure PostgreSQL is running
docker ps

# Check PostgreSQL logs
docker-compose logs postgres
```

### Database Reset
```bash
# Stop and remove all data
docker-compose down -v

# Start fresh
docker-compose up -d
```

## Next Steps

1. PostgreSQL + JPA implementation
2. 🔨 Implement fraud detection algorithm
3. 🔨 Add Kafka for event streaming
4. 🔨 Integrate Elasticsearch for analytics
5. 🔨 Add Spring Security (JWT authentication)
6. 🔨 Implement caching (Redis)
7. 🔨 Add comprehensive testing
8. 🔨 CI/CD pipeline

