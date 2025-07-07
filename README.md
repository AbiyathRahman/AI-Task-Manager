# InsightPulse Backend

This is the backend for **InsightPulse**, a full-stack task and productivity management app.  
It provides secure RESTful APIs for user authentication, task management, and data operations using **Spring Boot** and **PostgreSQL**.

![Render Deployment](https://img.shields.io/badge/Render-Deployed-success?style=flat-square)
[![Live API](https://img.shields.io/badge/🌐%20Live-API-blue?style=for-the-badge)](https://insightpulse-dye1.onrender.com)

---

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Spring Security + JWT**
- **Spring Data JPA**
- **PostgreSQL** (NeonDB or Render DB)
- **Docker** (optional)
- **Render** (hosting)

---

## 🌐 Deployed Frontend

The frontend is deployed on Vercel:

[🔗 InsightPulse Frontend](https://your-frontend.vercel.app)

---

## 🧪 Features

- ✅ JWT-based Authentication (Login/Register)
- ✅ Task CRUD APIs
- ✅ CORS-enabled for frontend communication
- ✅ PostgreSQL database integration
- ✅ Environment-variable driven config

---

## 📦 Running Locally

### 1. Clone the Repo

```bash
git clone https://github.com/your-username/insightpulse-backend.git
cd insightpulse-backend
2. Set Environment Variables
Create a .env file in the project root:

env
DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db-name
DATASOURCE_USERNAME=your_db_user
DATASOURCE_PASSWORD=your_db_pass
JWT_SECRET=your_secret_key

Make sure application.properties includes:

properties

spring.datasource.url=${DATASOURCE_URL}
spring.datasource.username=${DATASOURCE_USERNAME}
spring.datasource.password=${DATASOURCE_PASSWORD}
jwt.secret=${JWT_SECRET}

Build the package
./mvnw spring-boot:run


