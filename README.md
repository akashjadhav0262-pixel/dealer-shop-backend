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
- Complete database schema (9 tables) created from JPA entities
- Database-level rules: per-shop unique names, SKUs and invoice numbers, plus foreign keys

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

Tables are created from JPA entities. Every table has `id`, `created_at` and `updated_at`.

```text
users
 └── shops (owner_id)
      ├── categories (shop_id)
      │    └── products (shop_id, category_id)
      │         ├── bill_items (bill_id, product_id)
      │         └── stock_transactions (shop_id, product_id)
      ├── customers (shop_id)
      └── bills (shop_id, customer_id, created_by_id)
           ├── bill_items
           └── payments (shop_id, bill_id)
```

| Table | Purpose |
|---|---|
| `users` | Dealer and admin accounts (email is unique, only a password hash is stored) |
| `shops` | A dealer's shop details, GST number and UPI ID |
| `categories` | Custom product categories, unique per shop |
| `products` | Items with SKU, barcode, prices, stock, unit and active flag |
| `customers` | A shop's customers |
| `bills` | Invoice header: totals, status, invoice number unique per shop |
| `bill_items` | Lines of a bill, with a copy of the product name and price at sale time |
| `payments` | Payment records with method, status and provider reference |
| `stock_transactions` | Append-only stock history (PURCHASE, SALE, ADJUSTMENT, RETURN) |

**Key design decisions**
- **Tenant isolation:** every business table carries a `shop_id`.
- **Per-shop uniqueness:** two shops can both have a category "Snacks" or an invoice `INV-0001`, but one shop cannot repeat them.
- **Money and stock use `BigDecimal`**, never `double`, to avoid rounding errors and to support quantities like 2.5 kg.
- **Bill items keep a price snapshot**, so old invoices never change when prices change.
- **Bills are cancelled, not deleted**, to keep a clean audit trail.
- **Payment status lives only in `payments`**, so there is one source of truth.
- **A shop belongs to a user through a many-to-one link**, so one dealer can own several shops later. For now the application will allow one shop per dealer.

## Project Structure

```text
dealer-shop-backend/
 ├── src/
 │   ├── main/
 │   │   ├── java/com/dealershop/dealer_shop_backend/
 │   │   │   ├── controller/
 │   │   │   ├── service/
 │   │   │   ├── repository/
 │   │   │   ├── entity/
 │   │   │   │    ├── BaseEntity, Role, Unit
 │   │   │   │    ├── User, Shop
 │   │   │   │    ├── Category, Product, Customer
 │   │   │   │    ├── Bill, BillItem, BillStatus
 │   │   │   │    ├── Payment, PaymentMethod, PaymentStatus
 │   │   │   │    └── StockTransaction, StockTransactionType
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

All tables are created automatically on startup.

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
- [x] Phase 2: Database entities and relationships
- [ ] Phase 3: Authentication and JWT security _(next)_
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