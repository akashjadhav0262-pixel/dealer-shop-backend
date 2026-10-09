# DealerShop Backend

DealerShop is a backend application built with Java and Spring Boot for small shop owners to manage their products, inventory, customers, bills, and payments.

The application is designed to support multiple dealers. Each dealer must only be able to access their own shop's data, preventing unauthorized access to another dealer's products, customers, bills, and other resources.

## Why I Built This Project

I am a fresher building this project to develop practical backend engineering skills and gain hands-on experience with Java, Spring Boot, REST APIs, databases, authentication, security, testing, Docker, CI/CD, and AWS.

The goal is to build a secure, maintainable backend that demonstrates real-world software development practices.

## Current Progress

### Completed

* Spring Boot project setup with Java 21 and Maven
* MySQL database connection
* Health-check endpoints
* JPA entities and database table creation
* Dealer registration API
* BCrypt password hashing
* Input validation and structured error responses
* Spring Security configuration for protected endpoints
* JWT-based login and access-token authentication
* Protected current-user endpoint
* Refresh-token generation and storage using token hashes
* Refresh-token rotation
* Logout and refresh-token revocation
* Refresh-token reuse detection
* Postman testing of authentication and token lifecycle scenarios
* Git and GitHub repository setup

### Currently Working On

* Change password
* Forgot-password and password-reset functionality
* Further automated testing with JUnit and Mockito

## Tech Stack

| Technology      | Purpose                          |
| --------------- | -------------------------------- |
| Java 21         | Backend programming              |
| Spring Boot     | Application framework            |
| Spring Web      | REST API development             |
| Spring Data JPA | Database access                  |
| Hibernate       | ORM                              |
| Spring Security | Authentication and authorization |
| JWT             | Access-token authentication      |
| BCrypt          | Password hashing                 |
| MySQL           | Relational database              |
| Bean Validation | Request validation               |
| Maven           | Dependency management and builds |
| Git and GitHub  | Version control                  |
| IntelliJ IDEA   | Development environment          |
| Postman         | API testing                      |

**Planned technologies:** Docker, GitHub Actions, AWS, Swagger/OpenAPI, JUnit, and Mockito.

## Architecture

The application follows a layered architecture:

```text
Client / Postman
       |
       v
Controller
       |
       v
Service
       |
       v
Repository
       |
       v
MySQL Database
```

* **Controller:** Receives HTTP requests and returns responses.
* **Service:** Contains business logic.
* **Repository:** Performs database operations.
* **Entity:** Represents database records.
* **DTO:** Defines request and response data.
* **Security:** Handles authentication and access control.
* **Exception:** Provides consistent error handling.

### Package Structure

```text
com.dealershop.dealer_shop_backend
├── config
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
├── security
├── service
└── util
```

## Authentication and Security

The authentication system uses JWT access tokens and refresh tokens.

### Implemented Features

* Dealer registration with server-controlled `DEALER` role
* Secure password hashing with BCrypt
* Login with email and password
* JWT access-token authentication
* Protected API endpoints
* Refresh-token rotation
* Refresh-token revocation on logout
* Detection of previously revoked refresh-token reuse
* Refresh-token hashes stored in the database instead of raw tokens
* Environment-based configuration for sensitive secrets

### Authentication Flow

```text
Dealer Registration
        |
        v
       Login
        |
        v
Access Token + Refresh Token
        |
        v
Access Protected APIs
        |
        v
Refresh Token When Needed
        |
        v
New Access Token + Rotated Refresh Token
        |
        v
Logout / Revoke Refresh Token
```

Password change and password reset are planned next.

## API Endpoints

The following endpoints have been implemented as part of the current project.

| Method | Endpoint             | Purpose                                  |
| ------ | -------------------- | ---------------------------------------- |
| POST   | `/api/auth/register` | Register a dealer                        |
| POST   | `/api/auth/login`    | Authenticate and obtain tokens           |
| POST   | `/api/auth/refresh`  | Refresh tokens with rotation             |
| POST   | `/api/auth/logout`   | Revoke a refresh token                   |
| GET    | `/api/auth/me`       | Get the authenticated user's details     |
| GET    | `/api/ping`          | Check whether the application is running |
| GET    | `/actuator/health`   | Application health check                 |

Protected endpoints require a valid access token, except where explicitly configured as public.

### Registration Example

**Request**

`POST /api/auth/register`

```json
{
  "name": "Ramesh Kumar",
  "email": "ramesh@example.com",
  "phone": "9876543210",
  "password": "Secret@123"
}
```

**Example response — `201 Created`**

```json
{
  "id": 1,
  "name": "Ramesh Kumar",
  "email": "ramesh@example.com",
  "phone": "9876543210",
  "role": "DEALER"
}
```

The password is never returned, and the dealer role is assigned by the backend.

### Login Example

`POST /api/auth/login`

```json
{
  "email": "ramesh@example.com",
  "password": "Secret@123"
}
```

A successful login returns an access token, a refresh token, token type, expiration information, and user details.

### Refresh Token Example

`POST /api/auth/refresh`

```json
{
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

A successful refresh returns a new token pair. The previous refresh token is invalidated as part of token rotation.

### Logout Example

`POST /api/auth/logout`

```json
{
  "refreshToken": "YOUR_REFRESH_TOKEN"
}
```

A successful logout revokes the supplied refresh token.

### Error Response Example

```json
{
  "timestamp": "2026-10-04T12:00:00",
  "status": 409,
  "error": "CONFLICT",
  "message": "Email is already registered"
}
```

The application uses HTTP status codes such as `400`, `401`, and `409` for appropriate validation, authentication, and conflict errors.

## Database Design

The project currently contains the following business and authentication tables:

| Table                | Purpose                                                  |
| -------------------- | -------------------------------------------------------- |
| `users`              | Dealer accounts and authentication details               |
| `shops`              | Shop information                                         |
| `categories`         | Product categories                                       |
| `products`           | Product details, prices, and stock                       |
| `customers`          | Customer information                                     |
| `bills`              | Bill details and totals                                  |
| `bill_items`         | Products and quantities in each bill                     |
| `payments`           | Payment information                                      |
| `stock_transactions` | History of stock changes                                 |
| `refresh_tokens`     | Refresh-token hashes, expiry, and revocation information |

Database tables are mapped through JPA entities and created automatically according to the application's current database configuration.

### Database Design Decisions

* `BigDecimal` is used for monetary values instead of `double`.
* Bill items are designed to preserve the product name and price at the time of sale.
* Bills are intended to be cancelled rather than permanently deleted.
* Shop ownership must be enforced when implementing business APIs.
* Refresh tokens are stored as hashes rather than raw token values.

## Planned Features

### Shop Management

* Create and update shop details
* Store address, phone, email, GST number, and UPI ID

### Category Management

* Create, list, update, and delete categories
* Keep categories isolated by shop

### Product and Inventory Management

* Add and update products
* Search and filter products
* Track purchase price and selling price
* Manage stock quantities
* Detect low-stock products
* Maintain stock transaction history

### Customer Management

* Add and update customers
* View customer details
* Retrieve a customer's previous bills

### Billing System

* Create bills with multiple products
* Calculate totals on the backend
* Apply discounts and taxes where supported
* Validate stock before completing a sale
* Reduce stock after successful billing

### Payments

* Cash and UPI payment support
* Payment status tracking
* QR code support

### Invoice Generation

* Generate downloadable PDF invoices
* Include shop, customer, item, and payment details

### Dashboard and Reports

* Daily, weekly, and monthly sales
* Revenue summaries
* Low-stock products
* Top-selling products
* Payment and stock reports

### DevOps and Deployment

* Docker configuration
* Automated tests
* GitHub Actions CI/CD
* AWS deployment
* Swagger/OpenAPI documentation

## Running the Project Locally

### Prerequisites

* Java 21
* MySQL
* Git
* IntelliJ IDEA or another Java IDE
* Postman

### 1. Clone the Repository

```bash
git clone https://github.com/akashjadhav0262-pixel/dealer-shop-backend.git
cd dealer-shop-backend
```

### 2. Create the Database

```sql
CREATE DATABASE dealershop;
```

### 3. Configure Environment Variables

Configure the required variables in IntelliJ IDEA under:

`Run → Edit Configurations → Environment variables`

Use the variable names expected by your application configuration, including:

* `DATABASE_PASSWORD`
* `JWT_SECRET`

Use your own local database credentials and a securely generated JWT secret. Never commit real secrets to GitHub.

### 4. Start the Application

Run `DealerShopBackendApplication` from IntelliJ IDEA.

### 5. Check Application Health

Open these URLs in your browser:

* `http://localhost:8080/api/ping`
* `http://localhost:8080/actuator/health`

### 6. Test the APIs

Use Postman to register a dealer, log in, access protected endpoints, refresh tokens, and test logout and token revocation.

## Testing

### Manual Testing

Authentication flows have been tested with Postman, including:

* Dealer registration and login
* Access to protected endpoints
* Refresh-token rotation
* Logout and refresh-token revocation
* Invalid and reused refresh tokens

### Planned Automated Testing

* JUnit tests for service and controller logic
* Mockito tests for service dependencies
* Spring Boot integration tests
* Authentication and authorization tests
* Multi-tenant isolation tests
* Product, stock, billing, and payment tests

## Development Roadmap

* [x] Phase 1: Project setup and database connection
* [x] Phase 2: Database entities and tables
* [x] Phase 3: Dealer registration and JWT authentication foundations
* [x] Login and access-token authentication
* [x] Refresh-token generation and rotation
* [x] Logout and token revocation
* [ ] Change password and password reset
* [ ] Phase 4: Shop management
* [ ] Phase 5: Category management
* [ ] Phase 6: Product and inventory management
* [ ] Phase 7: Customer management
* [ ] Phase 8: Billing system
* [ ] Phase 9: Payments and QR codes
* [ ] Phase 10: PDF invoice generation
* [ ] Phase 11: Dashboard and reports
* [ ] Phase 12: Notifications
* [ ] Phase 13: Automated testing and security verification
* [ ] Phase 14: Docker
* [ ] Phase 15: CI/CD with GitHub Actions
* [ ] Phase 16: AWS deployment

## Future Security Improvements

* Secure password-reset tokens and expiry handling
* Revoke refresh tokens after password changes
* Automated tests for authentication and authorization
* Strict ownership checks for every shop resource
* Database migrations for controlled schema changes
* Production-ready secrets management and HTTPS configuration

## About Me

**Akash Shankar Jadhav**

GitHub: [akashjadhav0262-pixel](https://github.com/akashjadhav0262-pixel)

This project is being developed incrementally, with a focus on understanding the code, testing each feature, and following practical backend engineering practices.
