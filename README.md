# AgroConnect

Farmer-to-Consumer Marketplace built with Java Spring Boot, React, MySQL, JWT, and Docker.

## 1. Project Overview

AgroConnect is a secure, role-based marketplace that directly bridges the gap between farmers and consumers. By bypassing intermediaries, farmers maintain full control over their inventory and pricing, while consumers enjoy access to fresh, organic, and locally-sourced agricultural products.

**The platform workflow:**
Farmer registers & provisions inventory → Customer browses marketplace → Adds items to Cart → Manages Address & Checkout → System strictly validates database inventory & prices → Order is placed & Stock is decremented → Delivery → Customer leaves verified Review.

## 2. Key Features

### Authentication
* **Registration & Login:** Secure authentication endpoints.
* **JWT Authentication:** Stateless JSON Web Token architecture.
* **BCrypt Hashing:** Secure password storage.
* **Role-Based Authorization:** Strict horizontal and vertical privilege separation (CUSTOMER, FARMER, ADMIN).

### Customer
* **Browse Products:** Search and filter by category, location, and organic status.
* **Product Details:** View inventory, prices, and aggregated ratings.
* **Cart Management:** Add, update, and clear cart items safely.
* **Address Management:** Store multiple validated delivery addresses.
* **Checkout:** Secure transaction processing.
* **Orders:** Track personal order history.
* **Reviews and Ratings:** Post-delivery verified review submission.

### Farmer
* **Product CRUD:** Manage farm inventory.
* **Ownership Protection:** Strict backend isolation (farmers can only mutate their own catalog).
* **Farmer Dashboard:** Analytics overview.
* **Order Visibility:** Track orders containing their specific products.
* **Order Status Management:** Update delivery lifecycle (e.g., PLACED → DELIVERED).
* **Sales Statistics:** Track real revenue generated.
* **Low-Stock Monitoring:** Real-time alert threshold for products under 5 units.

### Admin
* **Admin Dashboard:** Platform-wide oversight.
* **Customer & Farmer Statistics:** Registration tracking.
* **Product & Order Statistics:** Platform utilization metrics.
* **Revenue & Inventory Statistics:** Macro-level insights.
* **Administrative Management APIs:** Full read-only visibility into system health.

### Security
* **JWT & BCrypt:** Industry standard identity verification.
* **Role-Based Authorization & Ownership Checks:** Controllers enforce strict boundary limits.
* **IDOR Protection:** Backend strictly derives identities from the `SecurityContext` rather than trusting frontend payload IDs.
* **Server-Side Price Calculation:** Checkout ignores client-side totals.
* **Server-Side Stock Validation:** Order placement rejects if database inventory is insufficient.
* **Transaction Rollback:** `@Transactional` services prevent orphaned records.
* **DTO Validation:** Jakarta `@Valid` constraints reject malformed payloads.
* **Error Handling:** Global handlers gracefully swallow Java stack traces.

## 3. Technology Stack

**Backend:**
* Java 17
* Spring Boot 3
* Spring MVC
* Spring Data JPA
* Hibernate
* Spring Security
* JWT (JSON Web Tokens)
* Maven

**Database:**
* MySQL 8.0

**Frontend:**
* React 18
* JavaScript
* HTML / CSS / Bootstrap
* Axios

**Testing:**
* PowerShell API test scripts (Integration & E2E Validation)

**Deployment:**
* Docker
* Docker Compose
* Nginx (Frontend Serving)

## 4. Architecture

The platform operates on a robust decoupled architecture:

**Frontend Flow:**
React (UI) → Axios (HTTP Client) → REST API (JSON)

**Backend Flow:**
Controller (Routing & DTOs) → Security Filter (JWT Validation) → Service (Business Logic & Transactions) → Repository (JPA Data Access) → Hibernate (ORM) → MySQL (Persistence)

**Docker Compose Topology:**
```text
Docker Compose
├── Frontend (Nginx, Port 3000)
├── Backend (Spring Boot, Port 8080)
└── MySQL (Database, Port 3306)
```

## 5. User Roles

| Role     | Capabilities                                 |
| -------- | -------------------------------------------- |
| CUSTOMER | Browse, Cart, Checkout, Orders, Reviews      |
| FARMER   | Product management, Farmer Orders, Dashboard |
| ADMIN    | Platform management and Dashboard            |

## 6. Database / Domain Model

* **User**: Base authentication entity storing credentials and roles.
* **Customer / Farmer**: Profile entities mapped natively to the `User`.
* **Product**: Inventory items tied explicitly to a `Farmer` and `Category`.
* **Cart & CartItem**: Ephemeral staging ground for customer purchases.
* **Order & OrderItem**: Immutable snapshot of a checkout event.
* **Address**: Strict 1-to-Many relationship with a `Customer`.
* **Review**: Post-delivery feedback tied to both `Product` and `Customer`.

## 7. API Documentation

### Authentication
* `POST /api/auth/register` - (Public) Registers a new user.
* `POST /api/auth/login` - (Public) Authenticates credentials and returns JWT.

### Products
* `GET /api/products` - (Public) Retrieves all products.
* `POST /api/products` - (Farmer) Creates a new product.
* `PUT /api/products/{id}` - (Farmer) Updates an existing owned product.

### Cart
* `GET /api/cart` - (Customer) Retrieves active cart.
* `POST /api/cart/items` - (Customer) Adds product to cart.

### Addresses
* `GET /api/addresses` - (Customer) Retrieves personal addresses.
* `POST /api/addresses` - (Customer) Adds a new address.

### Orders
* `POST /api/orders` - (Customer) Submits cart for checkout.
* `GET /api/orders/my-orders` - (Customer) Retrieves personal history.
* `GET /api/orders/farmer` - (Farmer) Retrieves relevant fulfillment requests.
* `PUT /api/orders/{id}/status` - (Farmer) Updates delivery state.

### Reviews
* `GET /api/products/{id}/reviews` - (Public) Retrieves product ratings.
* `POST /api/products/{id}/reviews` - (Customer) Submits new review.

### Admin
* `GET /api/admin/dashboard` - (Admin) Retrieves platform aggregations.

## 8. Security Architecture

### JWT Authentication
Login → Credentials validated against BCrypt → JWT generated via HMAC → Sent with subsequent requests in `Authorization: Bearer` header → `JwtAuthenticationFilter` validates token → Identity extracted into Spring `SecurityContext` → Endpoints authorized via `@PreAuthorize` or `SecurityConfig`.

### Ownership Security
Sensitive operations **derive identity from the authenticated JWT** rather than trusting frontend IDs. For instance, updating an address extracts the target `customerId` natively from the backend context, making cross-tenant manipulation mathematically impossible.

## 9. Important Security Fixes

* **Product Creation IDOR:** 
  * *Problem:* Backend previously trusted `farmerId` supplied by the client payload during product creation.
  * *Fix:* Overrode payload value with `securityUtils.getCurrentFarmerId()` at the entity mapping layer.
* **Order Status Authorization:** 
  * *Problem:* Order status modification lacked sufficient farmer ownership validation.
  * *Fix:* Enforced a deep validation sequence ensuring that only an authorized farmer *who explicitly owns a product within that specific order* can mutate the status.

## 10. Checkout Architecture

1. **Customer** adds items to **Cart** and selects an **Address**.
2. **Checkout** is triggered via `/api/orders`.
3. **Backend retrieves product price** natively from the database (ignoring any UI totals).
4. **Backend validates stock** dynamically.
5. **Order created** & **OrderItem price snapshot created**.
6. **Stock reduced** & **Cart cleared**.
7. Entire workflow executes under `@Transactional` to guarantee data integrity.

## 11. Testing

The platform was verified using comprehensive automated PowerShell testing scripts executing against live Docker environments.

* **Authentication & Authorization:** Validated isolated boundaries (401/403 responses).
* **Product Security:** Validated IDOR prevention.
* **Cart & Checkout:** Verified negative quantity blocks, out-of-stock rejections, and price freezing.
* **Review Validation:** Verified that unpurchased items or pending orders block review submissions.
* **Dashboard Verification:** Verified analytics isolation for Farmers and Admins.
* **E2E Validation:** Successfully executed the complete customer and farmer lifecycles.

## 12. Docker Setup

The platform utilizes a 3-container topology:
* `mysql-db` (Port 3306)
* `backend` (Port 8080)
* `frontend` (Port 3000)

**Deployment Commands:**
```bash
# Build the container images
docker compose build

# Start the cluster in detached mode
docker compose up -d

# Verify container health
docker compose ps

# Monitor logs
docker compose logs -f backend
```

## 13. Local Setup

### Requirements
* Java 17
* Node.js / npm
* Docker Desktop

### Configuration
1. Copy `.env.example` to `.env`.
2. Update the variables with your secure configurations (e.g., `JWT_SECRET`).

### Backend (Without Docker)
```bash
./mvnw clean package
java -jar target/agroconnect-0.0.1-SNAPSHOT.jar
```

### Frontend (Without Docker)
```bash
cd frontend
npm install
npm start
```

## Screenshots
> *TODO: Capture high-resolution screenshots of the Customer Cart, Farmer Dashboard, Checkout Flow, and Admin Panel for portfolio display.*

## 15. Project Structure

```text
agroconnect/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── .env.example
├── .gitignore
├── src/
│   └── main/java/com/agroconnect/
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── repository/
│       ├── security/
│       └── service/
└── frontend/
    ├── package.json
    ├── Dockerfile
    ├── nginx.conf
    └── src/
        ├── components/
        ├── contexts/
        ├── pages/
        └── services/
```

## 16. Future Improvements
* Payment gateway integration (Stripe/Razorpay)
* Real-time order tracking notifications
* Automated CI/CD pipeline deployment
* Cloud image storage (AWS S3) for product photos

---

### Resume Project Summary
* **Engineered a Farmer-to-Consumer E-Commerce Platform** leveraging Spring Boot 3, React 18, and MySQL, successfully decoupling the supply chain and deploying via a multi-container Docker Compose topology.
* **Implemented Robust Security Architectures** combining JWT authentication, BCrypt, and deep `@Transactional` IDOR protections to strictly isolate Tenant (Farmer/Customer) data boundaries and prevent horizontal privilege escalation.
* **Designed a Tamper-Proof Checkout Pipeline** utilizing strict server-side price snapshotting, dynamic inventory constraints, and automated transaction rollbacks to guarantee absolute data integrity.
