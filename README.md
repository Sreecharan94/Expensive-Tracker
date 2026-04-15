# Expense Tracker

A modern expense tracking application that helps users monitor, categorize, and analyze their spending habits. Built with Spring Boot for the backend and React for the frontend, this application provides a seamless experience for managing personal finances.

## Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Troubleshooting](#troubleshooting)

## Features

- **User Authentication**
  - Secure registration and login with JWT
  - Password hashing with BCrypt
  
- **Expense Management**
  - Add, edit, and delete expenses
  - Categorize expenses for better tracking
  - Filter by date range and categories
  
- **Dashboard Analytics**
  - Visualize spending patterns
  - View monthly and yearly expense trends
  - Category-based expense distribution
  
- **Reporting**
  - Generate custom reports
  - Export reports in multiple formats (PDF, CSV, Excel)
  - Spending insights and recommendations

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.0-M3
- **Language**: Java 22
- **Database**: MongoDB Atlas (NoSQL)
- **Security**: Spring Security with JWT Authentication
- **Build Tool**: Maven

### Frontend
- **Framework**: React
- **Styling**: Bootstrap 5, Font Awesome, Custom CSS
- **HTTP Client**: Axios

## Prerequisites

Before you begin, ensure you have the following installed:

- Java Development Kit (JDK) 22
- Maven 3.8.1+
- Node.js 18.x
- npm 9.x
- MongoDB Atlas account (for cloud database)
- A code editor (VS Code, IntelliJ IDEA, etc.)

## Installation

### Backend Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/expense-tracker.git
cd expense-tracker
```

2. Install Maven dependencies:
```bash
mvn clean install
```

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd expense-tracker-frontend
```

2. Install Node.js dependencies:
```bash
npm install
```

## Configuration

### Backend Configuration

1. Create a MongoDB Atlas cluster and database:
   - Sign up for MongoDB Atlas
   - Create a new cluster
   - Create a database named `expensetracker`
   - Add your IP to the whitelist
   - Create database user credentials

2. Configure `application.properties`:
```properties
# MongoDB Configuration
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster0.mongodb.net/expensetracker?retryWrites=true&w=majority

# Server Configuration
server.port=8080

# JWT Configuration
jwt.secret=your-strong-secret-key
jwt.expirationMs=86400000

# Logging Configuration
logging.level.org.springframework=INFO
logging.level.com.expensetracker=DEBUG
```

**Important Note**: Replace `your-strong-secret-key` with a secure random key. Generate one using:
```bash
openssl rand -base64 32
```

### Frontend Configuration

1. Create a `.env` file in the frontend directory:
```env
REACT_APP_API_BASE_URL=http://localhost:8080
```

## Running the Application

### Start Backend

```bash
cd expense-tracker
mvn spring-boot:run
```

### Start Frontend

```bash
cd expense-tracker-frontend
npm start
```

The application will be accessible at:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate a user and get JWT token

### User Management
- `GET /api/users/me` - Get current user details
- `PUT /api/users/me` - Update user profile

### Expense Management
- `POST /api/expenses` - Add a new expense
- `GET /api/expenses` - Get all expenses (with optional filters)
- `GET /api/expenses/{id}` - Get expense by ID
- `PUT /api/expenses/{id}` - Update an expense
- `DELETE /api/expenses/{id}` - Delete an expense

### Dashboard & Reporting
- `GET /api/dashboard` - Get dashboard data
- `POST /api/reports` - Generate a report

## Troubleshooting

### Common Issues and Solutions

#### 1. Application fails to start with JWT configuration error
```
Unable to resolve the Configuration with the provided Issuer of "http://localhost:8080/"
```

**Solution**: Update the `SecurityConfig.java` file with the corrected JWT decoder:

```java
@Bean
public JwtDecoder jwtDecoder() {
    // For symmetric key signing (HS256)
    SecretKeySpec secretKey = new SecretKeySpec(
        jwtSecret.getBytes(StandardCharsets.UTF_8), 
        "HS256"
    );
    return NimbusJwtDecoder.withSecretKey(secretKey).build();
}
```

#### 2. Circular dependency error between SecurityConfig and UserService

**Solution**: Ensure proper dependency injection and use `@Lazy` where necessary:

```java
@Configuration
public class SecurityConfig {
    private final UserService userService;

    public SecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }
    // Other configurations...
}
```

#### 3. MongoDB connection issues

**Solution**: Verify your MongoDB Atlas connection string in `application.properties`:
- Ensure your IP address is whitelisted in MongoDB Atlas
- Check that your database username and password are correct
- Verify the database name is correct

#### 4. "Access to localhost was denied" error

**Solution**: This typically occurs when the backend server isn't running or there's a port conflict:
- Verify the backend is running on port 8080
- Check for port conflicts with `netstat -ano | findstr :8080` (Windows) or `lsof -i :8080` (Mac/Linux)
- If another process is using port 8080, change the port in `application.properties`:
  ```properties
  server.port=8081
  ```
