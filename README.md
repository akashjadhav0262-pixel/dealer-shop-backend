# DealerShop

DealerShop is a backend project I am building with Java and Spring Boot. It is for small shop owners who want to manage their products, stock, customers and bills in one place.

Many shops can use the same app, but each shop only sees its own data. One shop should never be able to see another shop's products, customers or bills.

## Why I made this

I am a fresher and I wanted a real project to learn backend development properly. I am using it to learn Spring Boot, MySQL, security, Docker, CI/CD and AWS, and to show in interviews.

## What is done so far

- Spring Boot project is set up and connected to MySQL
- Health check endpoints work
- All 9 database tables are created from Java classes (entities)
- Dealer registration works (`POST /api/auth/register`)
- Passwords are hashed with BCrypt, so they are never saved as plain text
- Input validation and clean error messages (400, 401, 409)
- All other endpoints are locked with Spring Security
- Git and GitHub are set up

## What I plan to add

- Login with JWT, refresh token, logout, change and reset password
- Shop details
- Categories and products
- Stock tracking
- Customers
- Billing (the backend calculates all prices and totals)
- Cash and UPI payments, QR code
- PDF invoice
- Dashboard and sales reports
- Docker, GitHub Actions and AWS deployment

## Tech used

- Java 21
- Spring Boot
- Spring Data JPA (Hibernate)
- Spring Security
- Bean Validation
- MySQL
- Maven
- Git and GitHub
- IntelliJ IDEA and Postman

Coming later: JWT, Docker, GitHub Actions, AWS, Swagger, JUnit and Mockito.

## How the code is organised

```text
Controller  -> takes the request
Service     -> does the work (business logic)
Repository  -> talks to the database
```

Packages: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `security`, `exception`, `config`, `util`.

## API

Right now these endpoints are open (no login needed):

| Method | URL | What it does |
|---|---|---|
| POST | /api/auth/register | Register a new dealer |
| GET | /api/ping | Simple check that the app is running |
| GET | /actuator/health | Health check (also checks the database) |

Every other URL returns `401 Unauthorized`.

**Register example**

Request:
```json
{
  "name": "Ramesh Kumar",
  "email": "ramesh@example.com",
  "phone": "9876543210",
  "password": "Secret@123"
}
```

Response (`201 Created`):
```json
{
  "id": 1,
  "name": "Ramesh Kumar",
  "email": "ramesh@example.com",
  "phone": "9876543210",
  "role": "DEALER"
}
```

The password is never returned, and the role is always set to `DEALER` by the server (it cannot be sent in the request).

Errors look like this:
```json
{
  "timestamp": "2026-10-04T12:00:00",
  "status": 409,
  "error": "CONFLICT",
  "message": "Email is already registered"
}
```

## Database tables

| Table | What it stores |
|---|---|
| users | Dealer accounts |
| shops | Shop details |
| categories | Product categories of a shop |
| products | Products, price and stock |
| customers | Customers of a shop |
| bills | Bill details and totals |
| bill_items | Products inside a bill |
| payments | Payment details of a bill |
| stock_transactions | History of stock changes |

Most tables have a `shop_id` column so that every shop's data stays separate.

Some things I did on purpose:
- Prices and quantities use `BigDecimal` instead of `double` to avoid rounding mistakes.
- A bill item saves the product name and price at the time of sale, so old bills do not change when prices change.
- Bills are cancelled, not deleted.

## How to run it

You need Java 21, MySQL and Git.

1. Clone the project
```
   git clone https://github.com/akashjadhav0262-pixel/dealer-shop-backend.git
```
2. Create the database in MySQL
```sql
   CREATE DATABASE dealershop;
```
3. Add your MySQL password as an environment variable named `DATABASE_PASSWORD`
   (in IntelliJ: Run -> Edit Configurations -> Environment variables)
4. Run the app from IntelliJ
5. Open these in the browser to check:
   - http://localhost:8080/api/ping
   - http://localhost:8080/actuator/health
6. Try registering a user with Postman using the example above

The tables are created automatically when the app starts.

## Settings

Passwords and secrets are not stored in the code. They are read from environment variables. See `.env.example` for the list.

## Progress

- [x] Phase 1: Project setup and database connection
- [x] Phase 2: Database tables (entities)
- [ ] Phase 3: Register, login and JWT (in progress: registration done)
- [ ] Phase 4: Shop
- [ ] Phase 5: Categories
- [ ] Phase 6: Products and stock
- [ ] Phase 7: Customers
- [ ] Phase 8: Billing
- [ ] Phase 9: Payments and QR
- [ ] Phase 10: PDF invoice
- [ ] Phase 11: Dashboard and reports
- [ ] Phase 12: Notifications
- [ ] Phase 13: Testing
- [ ] Phase 14: Docker
- [ ] Phase 15: CI/CD
- [ ] Phase 16: AWS deployment

## About me

Akash Shankar Jadhav
GitHub: [akashjadhav0262-pixel](https://github.com/akashjadhav0262-pixel)