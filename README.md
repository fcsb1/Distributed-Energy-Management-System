# **Distributed Energy Management System (Assignment 3)**

## **📋 Overview**

This project is a distributed software system designed to manage energy consumption data with added real-time communication capabilities. It allows administrators to manage users and smart metering devices, while clients can view their assigned devices, monitor historical energy consumption, and receive real-time alerts.
The system features a secure **Chat System** for customer support, integrating a rule-based auto-responder and **AI-driven support** (using Google Gemini). It also utilizes **WebSockets** for instant messaging and overconsumption notifications.

## **🏗️ System Architecture**

The system is composed of independent microservices communicated via a **Traefik API Gateway** (REST) and **RabbitMQ** (Asynchronous Events).

### **Microservices**

1.  **Authentication Service**: Handles user login, registration validation, and JWT token generation.
2.  **User Management Service**: Manages user accounts (Admin/Client) and synchronizes user data.
3.  **Device Management Service**: Manages smart devices and links them to users.
4.  **Monitoring Service**: Consumes sensor data, calculates energy consumption, and triggers overconsumption alerts.
5.  **Chat Service (NEW)**: Facilitates real-time chat between Clients and Admins. Handles real-time message delivery and system notifications.

### **Infrastructure**

* **API Gateway (Traefik)**: Reverse proxy routing requests and handling load balancing.
* **Message Broker (RabbitMQ)**: Handles device data ingestion and chat/notification events.
* **Databases**: Dedicated PostgreSQL instances for Credential, User, Device, and Monitoring services.
* **Frontend**: React application serving the UI for Clients and Admins.

## **🔌 Services & Ports**

The following ports are exposed on your localhost when running the project:

| Service | Container Name | Local Port | Internal Port | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend** | `frontend` | **80** | 80 | Web UI (React + Nginx) |
| **Traefik** | `traefik` | **80**, **8080** | 80, 8080 | Gateway & Dashboard |
| **Auth Service** | `auth-service` | **8081** | 8081 | Authentication API |
| **User Service** | `user-service` | **8082** | 8082 | User Management API |
| **Device Service** | `device-service` | **8083** | 8083 | Device Management API |
| **Monitoring** | `monitoring-service` | *N/A* | 8084 | Internal processing only |
| **Chat Service** | `chat-service` | **8085** | 8085 | Chat & WebSocket API |
| **RabbitMQ** | `rabbitmq` | **5672**, **15672** | 5672, 15672 | Broker & Management UI |

## **🛠️ Technologies**

* **Backend**: Java, Spring Boot, Spring WebSocket, Spring AMQP
* **AI/LLM**: Google Gemini API
* **Frontend**: React.js, Bootstrap, Nginx
* **Database**: PostgreSQL 16
* **Messaging**: RabbitMQ 3.13
* **DevOps**: Docker, Docker Compose, Traefik

## **🖥️ Accessing the Application**

Once all containers are running, you can access the system:

* **Frontend Application**: [http://localhost](https://www.google.com/search?q=http://localhost)  
* **Traefik Dashboard**: [http://localhost:8080/dashboard/](https://www.google.com/search?q=http://localhost:8080/dashboard/)  
* **RabbitMQ Management**: [http://localhost:15672](https://www.google.com/search?q=http://localhost:15672) (User: guest, Pass: guest)

## **👤 Usage**

### **Default Credentials**

The system automatically creates an administrator account on startup:

* **Username**: admin  
* **Password**: 1234  
* **Role**: Admin


## **🚀 Installation and Setup**

### **Prerequisites**
* Docker Desktop (running)
* Git

## **🐛 Troubleshooting**

Database connection errors?  
Wait a few moments. The microservices might try to connect before the PostgreSQL containers are fully ready. They are configured to restart automatically on failure.  
RabbitMQ connection failed?  
If the services cannot connect to RabbitMQ, ensure the rabbitmq container is healthy. You can check its status in Docker Desktop.


### **Configuration (Optional)**
To enable the AI Chat features, open `docker-compose.yml` and ensure your Google Gemini API key is set for the `chat-service`:
```yaml
environment:
  GEMINI_API_KEY: "Your_Actual_API_Key_Here"

