# Real-Time Security Dashboard

Java Spring Boot backend with React frontend for transaction monitoring and fraud detection.

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+
- Node.js 18+

### Start Backend
```bash
cd backend
mvn spring-boot:run
```

### Start Frontend
```bash
cd frontend
npm install
npm run dev
```

## Access Points

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Database Console**: http://localhost:8080/h2-console
- **Credentials**: admin/admin

## API Endpoints

- `GET /api/` - Home
- `GET /api/api/transactions` - List transactions
- `POST /api/api/transactions` - Create transaction
- `GET /api/api/transactions/suspicious` - Suspicious transactions
- `GET /api/api/transactions/{id}` - Get transaction
- `PUT /api/api/transactions/{id}` - Update transaction
- `DELETE /api/api/transactions/{id}` - Delete transaction

## Project Structure

```
real-time-security/
├── backend/              # Spring Boot backend
├── frontend/             # React frontend
├── data/                 # Sample CSV files
└── README.md            # This file
```

## Database

H2 in-memory database with console at http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:security_dashboard`
- Username: `sa`
- Password: (empty)
