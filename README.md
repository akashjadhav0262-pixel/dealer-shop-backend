# DealerShop

A multi-tenant backend for small shopkeepers and dealers to manage inventory, create bills, accept payments and generate invoices. Every dealer gets a private shop, and no dealer can ever see another dealer's data.

## Live Demo

🚧 Not deployed yet. The plan is to deploy on AWS (EC2 + RDS) and add the link here.

- Live API: _coming soon_
- Swagger docs: _coming soon_

## About the Project

**Why I built it:** I wanted a real-world project to learn backend development properly: Spring Boot, security, databases, Docker, CI/CD and cloud deployment. It also serves as a project I can walk through in interviews.

**What problem it solves:** Small shops often track stock and bills on paper or in notebooks, which makes it hard to know what is in stock, what was sold and who still owes money. DealerShop gives each shop a simple system for all of this.

**Who can use it:** Shopkeepers, dealers and small retailers. Many shops can use the same application, and each one only sees its own products, customers, bills and reports.

## Features

**Working now**
- Application starts and connects to MySQL
- Health check endpoints
- `users` and `shops` tables created from JPA entities

**Planned**
- Dealer registration and login (JWT authentication)
- Role-based authorization (DEALER, ADMIN)
- Tenant isolation, so Dealer A can never access Dealer B's data
- Shop profile management
- Custom product categories
- Add, update, delete and view products
- Search and filter products, low-stock detection
- Stock tracking with a full history of changes
- Customer management and bill history
- Billing with server-side price, discount and tax calculation
- Cash and UPI payments, plus QR payment information
- PDF invoice download
- Dashboard and sales reports
- Email notifications (SMS and WhatsApp later)

## Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Data JPA and Hibernate
- MySQL
- Spring Security and JWT _(planned)_

### Tools
- Git and GitHub
- Maven
- IntelliJ IDEA
- Postman

### Planned
- Docker and Docker Compose
- GitHub Actions (CI/CD)
- AWS EC2 and RDS
- Swagger / OpenAPI
- JUnit and Mockito

## Architecture

```text
Client (Postman / Swagger / future frontend)
 ↓
REST API
 ↓
Controller      (handles HTTP requests, no business logic)
 ↓
Service         (business rules, tenant checks, transactions)
 ↓
Repository      (database access)
 ↓
MySQL Database
```

**Tenant isolation:** each request carries a JWT that identifies the dealer and their shop. Every query looks up data by ID **and** shop, so changing an ID in a request never exposes another dealer's data.

## Database Design

Tables are created from JPA entities. All tables share `id`, `created_at` and `updated_at`.

**Implemented**

```text
users                         shops
-----                         -----
id (PK)                       id (PK)
name                          name
email (unique)                owner_name
phone                         address, phone, email
password_hash                 gst_number
role (DEALER / ADMIN)         upi_id, logo_url
active                        owner_id (FK → users.id)
```

A shop belongs to a user. The relationship is many-to-one so one dealer can own several shops in the future. For now the application will allow one shop per dealer.

**Planned**

```text
shops
 ├── categories
 ├── products ── stock_transactions
 ├── customers
 └── bills
      ├── bill_items
      └── payments
```

Every business table will carry a `shop_id`, which is what makes tenant isolation possible.

## Project Structure

```text
dealer-shop-backend/
 ├── src/
 │   ├── main/
 │   │   ├── java/com/dealershop/dealer_shop_backend/
 │   │   │   ├── controller/
 │   │   │   ├── service/
 │   │   │   ├── repository/
 │   │   │   ├── entity/          (BaseEntity, Role, User, Shop)
 │   │   │   ├── dto/
 │   │   │   ├── mapper/
 │   │   │   ├── security/
 │   │   │   ├── exception/
 │   │   │   ├── config/
 │   │   │   └── util/
 │   │   └── resources/
 │   │       └── application.properties
 │   └── test/
 ├── .env.example
 ├── .gitignore
 ├── pom.xml
 └── README.md
```

## Getting Started

### Prerequisites
- Java 21
- MySQL
- Git

### Setup

1. Clone the repository
```bash
git clone https://github.com/akashjadhav0262-pixel/dealer-shop-backend.git
cd dealer-shop-backend
```

2. Create the database
```sql
CREATE DATABASE dealershop;
```

3. Set your database password as an environment variable `DATABASE_PASSWORD`.
   In IntelliJ: Run → Edit Configurations → Environment variables.
   Other settings are listed in `.env.example`.

4. Run the application
```bash
.\mvnw.cmd spring-boot:run
```

5. Check that it works
   - http://localhost:8080/api/ping
   - http://localhost:8080/actuator/health (should return `{"status":"UP"}`)

The tables are created automatically on startup.

## Environment Variables

| Variable | Purpose |
|---|---|
| `DATABASE_URL` | JDBC URL of the MySQL database |
| `DATABASE_USERNAME` | Database user |
| `DATABASE_PASSWORD` | Database password |
| `JWT_SECRET` | Secret used to sign tokens _(planned)_ |
| `PAYMENT_SECRET` | Payment provider secret _(planned)_ |

Secrets are never committed to GitHub. See `.env.example` for the list.

## Roadmap

- [x] Phase 1: Project foundation and database connection
- [ ] Phase 2: Database entities and relationships _(in progress: users and shops done)_
- [ ] Phase 3: Authentication and JWT security
- [ ] Phase 4: Shop management
- [ ] Phase 5: Categories
- [ ] Phase 6: Products and stock
- [ ] Phase 7: Customers
- [ ] Phase 8: Billing
- [ ] Phase 9: Payments and QR
- [ ] Phase 10: PDF invoices
- [ ] Phase 11: Dashboard and reports
- [ ] Phase 12: Notifications
- [ ] Phase 13: Testing
- [ ] Phase 14: Docker
- [ ] Phase 15: CI/CD with GitHub Actions
- [ ] Phase 16: AWS deployment

## Author

**Akash Shankar Jadhav**
GitHub: [akashjadhav0262-pixel](https://github.com/akashjadhav0262-pixel)