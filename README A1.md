# Energy Management System - Assignment 1

## 📋 Project Overview

This project represents the first module of the distributed software system "Integrated Energy Monitoring Platform for Households". It implements a microservices-based energy management system that permits authenticated users to access, monitor, and manage smart energy metering devices.

The system is built as a set of loosely coupled, containerized microservices, each deployed independently and orchestrated through a reverse proxy and API gateway.

---

## 🏗️ System Architecture

### Architecture Overview

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTP :80
       ▼
┌─────────────────────────────────────────────────┐
│            Docker Host (Localhost)               │
│  ┌───────────────────────────────────────────┐  │
│  │      proxy-network (Docker Bridge)        │  │
│  │                                            │  │
│  │  ┌──────────┐                             │  │
│  │  │ Traefik  │ :80, :8080                  │  │
│  │  │ Gateway  │                             │  │
│  │  └────┬─────┘                             │  │
│  │       │                                    │  │
│  │  ┌────┴─────┬──────────┬──────────┐      │  │
│  │  │          │          │          │       │  │
│  │ ┌▼────┐  ┌─▼──────┐ ┌─▼──────┐ ┌▼──────┐│  │
│  │ │React│  │  Auth  │ │  User  │ │Device ││  │
│  │ │:80  │  │ :8081  │ │ :8082  │ │:8083  ││  │
│  │ └─────┘  └───┬────┘ └───┬────┘ └───┬───┘│  │
│  │              │          │          │     │  │
│  │         ┌────▼───┐  ┌───▼───┐  ┌──▼───┐ │  │
│  │         │Cred DB │  │User DB│  │Dev DB│ │  │
│  │         │:5432   │  │:5432  │  │:5432 │ │  │
│  │         └────────┘  └───────┘  └──────┘ │  │
│  └────────────────────────────────────────────┘│
└─────────────────────────────────────────────────┘
```

### Components

1. **Frontend Application (React + NGINX)**
   - Browser-based interface

2. **API Gateway (Traefik)**
   - Reverse proxy 
   - Request routing based on path prefixes
   - Entry point for all HTTP requests

3. **Authentication Service**
   - User login and JWT token generation
   - Credential storage and verification
   - Token validation for protected endpoints

4. **User Management Service**
   - CRUD operations for user accounts
   - Orchestrates user creation across all services
   - Synchronizes user data with auth and device services

5. **Device Management Service**
   - CRUD operations for smart devices
   - Device-to-user assignment
   - User mapping table management

6. **PostgreSQL Databases (3 instances)**
   - Credential DB: Stores authentication data
   - User DB: Stores user profiles
   - Device DB: Stores device information and user mappings

---

## 🛠️ Technologies Used

### Backend
- **Java 25** with **Spring Boot 4.0.0-SNAPSHOT**
- **PostgreSQL 16** for data persistence
- **JWT (JSON Web Tokens)** for stateless authentication
- **Maven** for dependency management

### Frontend
- **React 16.8** with Hooks
- **jwt-decode** for token parsing

### DevOps
- **Docker** for containerization
- **Traefik v3.0** for reverse proxy
- **NGINX** for serving frontend

---

## 📦 Prerequisites

Before running this project, ensure you have the following installed:

- **Docker** (version 20.10 or higher)
- **Docker Compose** (version 1.29 or higher)
- **Git** (for cloning the repository)

## 🚀 Installation and Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd <repository-directory>
```

### 2. Project Structure

```
.
├── auth-service/          # Authentication microservice
├── user-service/          # User management microservice
├── device-service/        # Device management microservice
├── react-demo-hooks/      # Frontend React application
├── traefik/               # Traefik configuration
│   ├── traefik.yml        # Main Traefik config
│   └── dynamic/
│       └── path.yml       # Routing rules
├── docker-compose.yml     # Docker orchestration file
└── README.md
```

### 3. Configuration Files

**No manual configuration needed!** All services are pre-configured with environment variables in `docker-compose.yml`.

**Default Credentials:**
- **Admin User:**
  - Username: `admin`
  - Password: `1234`
  - Role: `ROLE_ADMIN`

This admin user is automatically created on first startup.

---

## ▶️ Running the Project

### Start All Services

```bash
docker-compose up --build
```
**First startup takes 3-5 minutes** to build images and download dependencies.

### Access the Application

Once all containers are running, open your browser:

- **Frontend:** http://localhost
- **Traefik Dashboard:** http://localhost:8080

### Stop All Services

```bash
docker-compose down
```

### Stop and Remove Volumes (Clean Reset)

```bash
docker-compose down -v
```

**Warning:** This deletes all data in the databases!

---

## 👥 User Roles and Features

### Admin Role (`ROLE_ADMIN`)

**Access:** Login with `admin` / `1234`

**Capabilities:**
- ✅ Create, Read, Update, Delete users
- ✅ Create, Read, Update, Delete devices
- ✅ Assign devices to users
- ✅ View all devices and users

**Available Pages:**
- `/admin/users` - User Management
- `/admin/devices` - Device Management

### Client Role (`ROLE_CLIENT`)

**Access:** Create a client user via Admin panel

**Capabilities:**
- ✅ View devices assigned to them
- ❌ Cannot modify users or devices
- ❌ Cannot see other users' devices

**Available Pages:**
- `/client/devices` - My Devices

---

**Note:** All API requests (except login) require a JWT token in the `Authorization` header:
```
Authorization: Bearer <your-jwt-token>
```

## 🐛 Troubleshooting

### Issue: Containers won't start

**Solution:**
```bash
# Check if ports are already in use
docker ps
netstat -ano | findstr :80    # Windows
lsof -i :80                    # Mac/Linux

# Stop all Docker containers
docker-compose down
docker-compose up --build
```

## 📚 Additional Resources
- **Spring Boot Documentation:** https://docs.spring.io/spring-boot/
- **React Documentation:** https://react.dev/learn
- **Traefik Documentation:** https://doc.traefik.io/traefik/
- **Docker Documentation:** https://docs.docker.com/

---

## 🚧 Known Limitations

1. **No Swagger Documentation** - API documentation could be improved
2. **No HTTPS** - Currently uses HTTP only
3. **No Password Complexity Rules** - Accepts any password
4. **No Email Validation** - Username field doesn't validate email format
5. **No Search/Filter** - Frontend tables lack search functionality

## 👨‍💻 Author

- **Student Name:** [David Mihai]
- **Group:** [30641]
- **Year:** 2025-2026
