# Production PAN Verification

Pan Verification application generated from custom prompts.

## Features

- PAN verification with proper format validation
- Reference number generation
- Audit logging with PII masking
- Complete CRUD operations
- PostgreSQL database support
- Flyway migrations

## API Endpoints

- `POST /api/pan/verify` - Verify PAN number
- `GET /api/pan/status/{referenceNumber}` - Get verification status
- `GET /api/pan/history?panNumber=XXX` - Get verification history

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.6+
- PostgreSQL (if using PostgreSQL)

### Running the Application

1. Clone the repository
2. Configure database connection in `application.yml`
3. Run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The application will start on http://localhost:8080

## Database Setup

If using PostgreSQL with Docker:
```bash
docker-compose up -d postgres
```

## Testing

Run tests with:
```bash
mvn test
```

## Generated from Custom Prompts

This application was generated based on custom business requirements and prompts, ensuring it matches your specific needs.
