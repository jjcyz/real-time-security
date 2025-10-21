# 🛡️ Real-Time Security Dashboard

**Full-stack fraud detection system** with Java Spring Boot backend and React frontend for real-time transaction monitoring and multi-layered fraud detection.

## Features

- ✅ **PostgreSQL Database** with JPA/Hibernate ORM
- ✅ **Multi-Layered Fraud Detection** with 7 detection rules
- ✅ **Real-Time Analysis** - Fraud scoring on every transaction
- ✅ **RESTful API** with comprehensive endpoints
- ✅ **Multi-Profile Configuration** (H2 dev / PostgreSQL prod)
- ✅ **Advanced Analytics** - Fraud insights and patterns
- ✅ **Apache Kafka Integration** - Event-driven async processing
- ⏳ **Elasticsearch** (coming soon)

## Quick Start

### Prerequisites
- **Java 21+**
- **Maven 3.6+**
- **Node.js 18+**
- **Docker** (optional, for PostgreSQL)

### Option 1: Quick Start (H2 Database)
```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install && npm run dev
```

### Option 2: Production Setup (PostgreSQL)
```bash
# Start PostgreSQL
docker-compose up -d

# Backend
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod

# Frontend
cd frontend
npm install && npm run dev
```

## 🌐 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | - |
| **Backend API** | http://localhost:8080/api | - |
| **H2 Console** | http://localhost:8080/h2-console | sa / (empty) |
| **PostgreSQL** | localhost:5432 | postgres / postgres |

## 📊 Technology Stack

### Backend
- **Framework:** Spring Boot 3.5.6
- **Language:** Java 21
- **ORM:** JPA/Hibernate 6.x
- **Databases:** PostgreSQL 16 / H2
- **Connection Pool:** HikariCP
- **Validation:** Jakarta Bean Validation

### Frontend
- **Framework:** React 18
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **Build:** Vite

## 🛡️ Fraud Detection System

### 7 Detection Rules:
1. **High Amount Detection** (0-40 pts) - Flags large transactions
2. **Transaction Velocity** (0-30 pts) - Detects rapid transactions
3. **Amount Velocity** (0-25 pts) - Monitors transaction volume
4. **Unusual Time Pattern** (0-15 pts) - Flags odd-hour transactions
5. **Geographic Anomaly** (0-20 pts) - Detects suspicious IPs
6. **Fraud History** (0-25 pts) - Considers user's fraud history
7. **Merchant Risk** (0-15 pts) - Assesses merchant categories

**Fraud Threshold:** 70 points (out of 100)

See [FRAUD_DETECTION.md](FRAUD_DETECTION.md) for detailed documentation.

## 📡 API Endpoints

### Transactions
- `GET /api/transactions` - List all transactions
- `POST /api/transactions` - Create with fraud detection
- `GET /api/transactions/{id}` - Get by ID
- `GET /api/transactions/suspicious` - Get fraudulent transactions
- `GET /api/transactions/high-risk` - Get high-risk (score ≥ 70)
- `GET /api/transactions/user/{userId}` - Get by user
- `POST /api/transactions/{id}/analyze` - Re-analyze transaction

### Fraud Detection
- `GET /api/fraud/threshold` - Get fraud threshold
- `GET /api/fraud/insights` - Get fraud patterns and insights

### Analytics
- `GET /api/stats` - Database statistics
- `GET /api/users` - List all users

## 🧪 Testing

### Quick Test
```bash
# Get statistics
curl http://localhost:8080/api/stats

# Create legitimate transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"amount": 50, "userId": 1, "merchantName": "Starbucks"}'

# Create fraudulent transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"amount": 9500, "userId": 1, "merchantCategory": "Cryptocurrency", "ipAddress": "45.33.12.45"}'
```

### Comprehensive Test Suite
```bash
./test-fraud-detection.sh
```

## 📁 Project Structure

```
real-time-security/
├── backend/                          # Spring Boot Backend
│   ├── src/main/java/dashboard/
│   │   ├── config/                   # Configuration classes
│   │   ├── controller/               # REST controllers
│   │   ├── dto/                      # Data Transfer Objects
│   │   ├── model/                    # JPA Entities (User, Transaction)
│   │   ├── repository/               # JPA Repositories
│   │   └── service/                  # Business logic (Fraud Detection)
│   ├── src/main/resources/
│   │   └── application.yml           # Multi-profile configuration
│   └── pom.xml                       # Maven dependencies
├── frontend/                         # React Frontend
│   ├── src/
│   │   ├── components/               # React components
│   │   ├── pages/                    # Page components
│   │   └── services/                 # API services
│   └── package.json
├── docker-compose.yml                # PostgreSQL setup
├── test-fraud-detection.sh           # Test script
├── README.md                         # This file
├── README_POSTGRESQL.md              # Database documentation
└── FRAUD_DETECTION.md                # Fraud detection guide
```

## 📚 Documentation

- **[README_POSTGRESQL.md](README_POSTGRESQL.md)** - Database setup and configuration
- **[FRAUD_DETECTION.md](FRAUD_DETECTION.md)** - Fraud detection system documentation
- **[KAFKA_INTEGRATION.md](KAFKA_INTEGRATION.md)** - Kafka event-driven architecture
- **[API_ENDPOINTS.md](API_ENDPOINTS.md)** - Complete API reference

## 🎯 Development Roadmap

### Phase 1: Foundation ✅
- [x] PostgreSQL + JPA implementation
- [x] Multi-profile configuration (H2/PostgreSQL)
- [x] RESTful API with proper DTOs
- [x] Sample data initialization

### Phase 2: Fraud Detection ✅
- [x] Multi-layered fraud detection (7 rules)
- [x] Real-time fraud scoring
- [x] Analytics and insights endpoints
- [x] Comprehensive testing

### Phase 3: Big Data & Streaming ✅
- [x] Apache Kafka integration
- [x] Event-driven architecture
- [x] Async fraud detection
- [ ] Elasticsearch for analytics
- [ ] Redis caching

### Phase 4: Security & Testing 🔨
- [ ] Spring Security + JWT
- [ ] Unit and integration tests
- [ ] Load testing
- [ ] CI/CD pipeline

## 💻 Development

### Build JAR
```bash
cd backend
mvn clean package
java -jar target/security-dashboard-1.0.0.jar
```

### Hot Reload
Both frontend and backend support hot reload during development.

## 🤝 Contributing

This is a portfolio project demonstrating:
- Enterprise Java development
- Database design and optimization
- Fraud detection algorithms
- RESTful API design
- Full-stack development
