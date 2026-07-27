# Medical Management System

A full-stack Spring Boot application for managing medicine supply between Admin, Dealers, Hospitals, and Medical Shops.

---

## Technology Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, Hibernate
- **Frontend:** Thymeleaf, Bootstrap 5, HTML5, CSS3
- **Database:** MySQL 8+
- **Server:** Embedded Tomcat (runnable JAR), containerized with Docker
- **IDE:** Spring Tool Suite (STS) 4 / IntelliJ IDEA

---

## Default Admin Credentials

| Field          | Value       |
|----------------|-------------|
| Licence Number | `ADMIN001`  |
| Password       | `admin@123` |

---

## Setup Instructions (Local Development)

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+
- Spring Tool Suite (STS) 4 or IntelliJ IDEA

### 2. Database Setup
Create a MySQL database (the app will auto-create tables via `schema.sql`):
```sql
CREATE DATABASE medical_db;
```

The app reads its datasource settings from environment variables, falling back to
local defaults if they're not set (see `src/main/resources/application.properties`):
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/medical_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}
```
Either export those environment variables to match your local MySQL credentials,
or just edit the defaults directly in `application.properties` for local runs.

### 3. Run in STS / IntelliJ (Embedded Tomcat)
1. Import as **Existing Maven Project**
2. Right-click project → **Run As → Spring Boot App**
3. Open browser: `http://localhost:8080`

### 4. Run from the command line
```bash
mvn clean package -DskipTests
java -jar target/MedicalManagementSystem.jar
```

### 5. Run with Docker locally
```bash
docker build -t medical-management-system .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/medical_db?useSSL=false&serverTimezone=UTC \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  medical-management-system
```

---

## Deploying to Render (from GitHub)

This project ships with a `Dockerfile` and a `render.yaml` Blueprint. Render does not
offer a managed MySQL database, so you'll need a MySQL instance from an external
provider (e.g. PlanetScale, Aiven, AWS RDS, or your own server) and point the web
service at it via environment variables.

### Option A — Blueprint deploy
1. Push this project to a GitHub repository.
2. Provision a MySQL database with a provider of your choice and note its
   **host**, **port**, **database name**, **username**, and **password**.
3. In the Render Dashboard, click **New → Blueprint** and select your repo.
4. Render reads `render.yaml` and creates a **Docker web service**
   (`medical-management-system`). Fill in the `SPRING_DATASOURCE_URL`,
   `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` values
   (marked `sync: false`) in the Render dashboard using your MySQL credentials.
5. Click **Apply**. Render builds the Docker image and deploys it.
6. Once live, open the service URL. Log in with the default admin:
   `ADMIN001` / `admin@123` (change this password immediately in a real deployment).

### Option B — Manual setup
1. Push this project to GitHub.
2. Provision a MySQL database with an external provider. Note its host, port,
   database name, username, and password.
3. Create a **New → Web Service**, connect your GitHub repo, and choose
   **Runtime: Docker** (Render will detect the `Dockerfile` automatically).
4. Under the web service's **Environment** tab, add:
   - `SPRING_DATASOURCE_URL` = `jdbc:mysql://<host>:<port>/<database>?useSSL=false&serverTimezone=UTC`
   - `SPRING_DATASOURCE_USERNAME` = `<username>`
   - `SPRING_DATASOURCE_PASSWORD` = `<password>`
5. Deploy. Render sets `PORT` automatically, which `application.properties`
   already reads via `server.port=${PORT:8080}`.

### Notes for production
- Change the default admin password after your first login.
- `spring.jpa.hibernate.ddl-auto=validate` means schema changes come from
  `schema.sql`, not Hibernate auto-generation — keep that file in sync with any
  entity changes.

---

## User Workflow

### Registration Flow
1. New user visits `/register` → fills form (Dealer / Hospital / Medical Shop)
2. Visits `/signup` → sets password using Licence Number + Email
3. Waits for approval:
   - **Dealer** → approved by Admin
   - **Hospital / Medical Shop** → approved by Dealer

### Roles & Access

| Role          | Approved By | Can Buy From | Can Supply To  |
|---------------|-------------|--------------|----------------|
| Admin         | Default     | —            | Dealers        |
| Dealer        | Admin       | Admin        | Hospitals/Shops|
| Hospital      | Dealer      | Dealer       | —              |
| Medical Shop  | Dealer      | Dealer       | —              |

### Low Stock Alert
All users receive alerts when any medicine stock falls below **20 units**.

---

## Project Structure

```
MedicalManagementSystem/
├── src/main/java/com/medical/
│   ├── MedicalManagementSystemApplication.java
│   ├── config/
│   │   ├── CustomUserDetails.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── DataInitializer.java        ← Creates default Admin
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── AdminController.java
│   │   ├── DealerController.java
│   │   └── UserController.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Medicine.java
│   │   ├── Transaction.java
│   │   ├── Suggestion.java
│   │   ├── DealerStock.java
│   │   └── UserStock.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── MedicineRepository.java
│   │   ├── TransactionRepository.java
│   │   ├── SuggestionRepository.java
│   │   ├── DealerStockRepository.java
│   │   └── UserStockRepository.java
│   └── service/
│       ├── UserService.java
│       ├── MedicineService.java
│       ├── TransactionService.java
│       ├── SuggestionService.java
│       └── StockService.java
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/style.css
│   └── templates/
│       ├── login.html
│       ├── register.html
│       ├── signup.html
│       ├── access-denied.html
│       ├── admin/
│       │   ├── dashboard.html
│       │   ├── medicines.html
│       │   ├── dealers.html
│       │   ├── supply.html
│       │   ├── users.html
│       │   ├── transactions.html
│       │   └── suggestions.html
│       ├── dealer/
│       │   ├── dashboard.html
│       │   ├── stock.html
│       │   ├── buy.html
│       │   ├── supply.html
│       │   ├── users.html
│       │   ├── transactions.html
│       │   ├── suggestions.html
│       │   └── send-suggestion.html
│       └── user/
│           ├── dashboard.html
│           ├── medicines.html
│           ├── stock.html
│           ├── transactions.html
│           └── suggestions.html
├── Dockerfile
├── render.yaml
├── .dockerignore
├── .gitignore
└── pom.xml
```

---

## Features

- ✅ Role-based login (Admin / Dealer / Hospital / Medical Shop)
- ✅ Two-step registration (Register → Signup → Approval)
- ✅ Admin approves Dealers; Dealers approve Hospitals & Shops
- ✅ Admin manages and supplies medicines to Dealers
- ✅ Dealers supply medicines to Hospitals & Medical Shops
- ✅ Stock tracking per user with low-stock alerts (< 20 units)
- ✅ Suggestion system: Users → Dealer, Dealer → Admin
- ✅ Transaction history for all roles
- ✅ Colorful, responsive Bootstrap 5 UI
- ✅ Default Admin auto-created on first run
- ✅ Dockerized, runnable-JAR packaging — ready to deploy to Render (or any container host)
