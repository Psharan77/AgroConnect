# AgroConnect: Farmer-to-Consumer Marketplace

![Build Status](https://img.shields.io/badge/build-passing-brightgreen) ![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-brightgreen) ![React](https://img.shields.io/badge/React-18-blue) ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue) ![Docker](https://img.shields.io/badge/Docker-Compose-blue)

AgroConnect is a secure, role-based e-commerce platform that directly bridges the gap between farmers and consumers. By bypassing intermediaries, farmers maintain full control over their inventory and pricing, while consumers enjoy access to fresh, organic, and locally-sourced agricultural products.

---

## 1. Project Overview

This full-stack application provides a complete digital supply chain solution. The platform natively handles product cataloging, cart staging, secure transaction snapshots, and post-delivery reviews. It relies on a rigorous zero-trust backend architecture, ensuring that cross-tenant data manipulation is mathematically impossible.

## 2. Key Features

* **Multi-Tenant Architecture:** Complete horizontal isolation for Customers, Farmers, and Admins.
* **Tamper-Proof Checkout:** Backend calculates totals directly from the database and ignores client-side pricing.
* **Concurrency Control:** Strict inventory validation prevents out-of-stock purchases and race conditions.
* **JWT Identity Resolution:** Sensitive parameters (like `farmerId` or `customerId`) are extracted from the `SecurityContext`, neutralizing Insecure Direct Object Reference (IDOR) vectors.
* **Graceful Degradation:** Custom exception handling swallows internal Java/Hibernate stack traces, returning sanitized JSON maps to the frontend.

## 3. User Roles

| Role | Capabilities |
| :--- | :--- |
| **CUSTOMER** | Browse marketplace, manage cart, maintain multiple delivery addresses, execute secure checkouts, track order history, and submit verified product reviews. |
| **FARMER** | Full CRUD control over personal inventory, track sales metrics via dashboard, view relevant customer orders, update fulfillment statuses, and receive low-stock alerts. |
| **ADMIN** | Platform-wide oversight, monitor aggregated system revenue, track registration statistics, and identify global inventory anomalies. |

## 4. Technology Stack

**Backend Framework:**
* Java 17
* Spring Boot 3
* Spring MVC (RESTful API)
* Spring Data JPA & Hibernate (ORM)
* Spring Security & JWT (Authentication)
* Maven

**Database:**
* MySQL 8.0

**Frontend Framework:**
* React 18
* JavaScript (ES6+)
* HTML5 / CSS3 / Bootstrap
* Axios (HTTP Client)

**Deployment & Infrastructure:**
* Docker & Docker Compose
* Nginx (Frontend Reverse Proxy)

## 5. System Architecture

The platform operates on a heavily decoupled microservice-style pattern using a multi-container Docker topology.

**Data Flow Pipeline:**
`React UI` → `Axios (JSON)` → `REST API` → `Spring Security Filter` → `Controller` → `Service Layer (@Transactional)` → `JPA Repository` → `Hibernate` → `MySQL`

## 6. Authentication & Security

AgroConnect implements a rigorous security posture:
* **JWT Authentication:** Stateless, signed JSON Web Tokens handle session management.
* **BCrypt Password Hashing:** Salted cryptography protects database credentials.
* **Role-Based Authorization:** Endpoints are strictly gated by `hasRole()` decorators.
* **Farmer Ownership Checks:** Farmers can only mutate products they explicitly own.
* **Customer Ownership Checks:** Address and Cart modifications natively derive target IDs from the JWT.
* **Order Ownership Checks:** Farmers can only mutate the status of an order if they supply a product contained within that specific order.
* **Server-Side Price Calculation:** The cart uses the frontend for display only; the backend recalculates all totals from secure database reads.
* **Stock Validation:** Immediate rejection of orders that exceed available product quantities.
* **Transaction Rollback:** Failed database constraints trigger automatic rollbacks, preventing orphaned `OrderItems`.
* **IDOR Protections:** Deep iteration checks block horizontal privilege escalation across all domains.

## 7. Main REST API Endpoints

### Authentication
* `POST /api/auth/register` (Public)
* `POST /api/auth/login` (Public)

### Products
* `GET /api/products` (Public)
* `POST /api/products` (FARMER)
* `PUT /api/products/{id}` (FARMER)

### Cart & Checkout
* `GET /api/cart` (CUSTOMER)
* `POST /api/cart/items` (CUSTOMER)
* `POST /api/orders` (CUSTOMER)

### Dashboard
* `GET /api/farmer/dashboard` (FARMER)
* `GET /api/admin/dashboard` (ADMIN)

### Reviews
* `POST /api/products/{id}/reviews` (CUSTOMER)

## 8. Database / Entity Overview

* **User:** Base authentication entity storing credentials and roles.
* **Customer & Farmer:** Domain profiles mapped natively via Foreign Keys to `User`.
* **Product:** Inventory items tied explicitly to a `Farmer`.
* **Cart & CartItem:** Ephemeral staging models.
* **Order & OrderItem:** Immutable snapshot of a checkout event capturing frozen `unitPrice` and `quantity`.
* **Address:** Strict 1-to-Many relationship with a `Customer`.
* **Review:** Post-delivery feedback tied to both `Product` and `Customer`.

## 9. Customer Workflow
1. Registers and authenticates via `/login`.
2. Browses the marketplace and adds inventory to their `Cart`.
3. Creates a persistent delivery `Address`.
4. Triggers `Checkout`. The backend safely snapshots the prices, reduces inventory, and generates the `Order`.
5. Once the farmer updates the status to `DELIVERED`, the customer submits a `Review`.

## 10. Farmer Workflow
1. Authenticates into the Farmer platform.
2. Provisions catalog via the `Product` CRUD interface.
3. Monitors the `Farmer Dashboard` for aggregate sales data and low-stock alerts.
4. Retrieves a filtered list of `Orders` containing their specific products.
5. Updates the lifecycle of the order from `PLACED` → `SHIPPED` → `DELIVERED`.

## 11. Admin Workflow
1. Authenticates using an ADMIN-provisioned JWT.
2. accesses the `Admin Dashboard`.
3. Monitors macro-level statistics (total registrations, platform revenue, out-of-stock ratios).

## 12. Reviews & Ratings
The platform enforces strict rules on the review pipeline:
* Only **CUSTOMERS** can write reviews.
* A customer must have successfully purchased the product.
* The corresponding order must be in a **DELIVERED** state.
* Ratings are strictly capped between 1 and 5.
* Customers can only edit or delete their own reviews.

## 13. Docker Setup

The platform utilizes a 3-container topology:
* `mysql-db` (Port 3306)
* `backend` (Spring Boot - Port 8080)
* `frontend` (Nginx - Port 3000)

**Deployment Commands:**
```bash
# Build the images and start the cluster in detached mode
docker compose up -d --build

# Verify container health
docker compose ps

# Monitor backend logs
docker compose logs -f backend
```

## 14. Testing

The application behavior was verified using a comprehensive suite of automated API test scripts.
* **Authentication Matrices:** Blocked unauthorized cross-role access (401/403).
* **IDOR Prevention:** Verified farmers cannot mutate products belonging to other tenants.
* **Checkout Rules:** Verified negative quantities, out-of-stock items, and tampered prices are explicitly rejected.
* **Address Isolation:** Verified customers cannot attach another user's address to their checkout request.

## 15. Local Setup Instructions

**Prerequisites:** Java 17, Node.js 18+, Docker Desktop

**1. Environment Configuration**
```bash
cp .env.example .env
# Edit .env with your local credentials and a secure 256-bit JWT secret.
```

**2. Backend (Without Docker)**
```bash
./mvnw clean package -DskipTests
java -jar target/agroconnect-0.0.1-SNAPSHOT.jar
```

**3. Frontend (Without Docker)**
```bash
cd frontend
npm install
npm start
```

## 16. Project Screenshots

> *Note: UI captures will be added here prior to final portfolio submission.*

* **[Placeholder] Customer Marketplace & Product Filters**
* **[Placeholder] Secure Cart & Checkout Pipeline**
* **[Placeholder] Farmer Analytics Dashboard**
* **[Placeholder] Admin Oversight Panel**

## 17. Future Improvements
* Payment gateway integration (Stripe API)
* Automated email notifications for order lifecycle events (SendGrid)
* Automated CI/CD pipeline deployment (GitHub Actions)
* Cloud image storage for product catalogs (AWS S3)

---

### Resume Project Summary
* **Engineered a Full-Stack E-Commerce Platform** leveraging Spring Boot 3, React 18, and MySQL, successfully decoupling the agricultural supply chain and deploying via a multi-container Docker Compose topology.
* **Implemented Robust Security Architectures** combining JWT authentication, BCrypt, and deep `@Transactional` IDOR protections to strictly isolate Tenant (Farmer/Customer) data boundaries and prevent horizontal privilege escalation.
* **Designed a Tamper-Proof Checkout Pipeline** utilizing strict server-side price snapshotting, dynamic inventory constraints, and automated transaction rollbacks to guarantee absolute data integrity.
