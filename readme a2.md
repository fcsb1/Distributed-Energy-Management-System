# **Integrated Energy Management System**

## **📋 Overview**

This project is a distributed software system designed to manage energy consumption data. It allows administrators to manage users and smart metering devices, while clients can view their assigned devices and monitor historical energy consumption.

The architecture is built using microservices, containerized with Docker, and orchestrated via a Traefik API Gateway. 

🏗️ System Architecture

The system is composed of independent microservices that communicate either synchronously via REST (through the Gateway) or asynchronously via RabbitMQ events.

### **Microservices**

1. **Authentication Service**: Handles user login, registration validation, and JWT token generation.  
2. **User Management Service**: Manages user accounts (Admin/Client) and synchronizes user data with other services.  
3. **Device Management Service**: Manages smart devices, handles CRUD operations, and links devices to users.  
4. **Monitoring Service**: Consumes sensor data from the message broker, calculates hourly energy consumption, and stores it for visualization.

### **Infrastructure**

* **API Gateway (Traefik)**: Acts as a reverse proxy, routing requests to the appropriate microservices and handling authorization checks.  
* **Message Broker (RabbitMQ)**: Handles asynchronous communication between the Simulator, Monitoring Service, and synchronization events between User/Device services.  
* **Databases**: Four separate PostgreSQL instances (Credential DB, User DB, Device DB, Monitoring DB).  
* **Frontend**: A React application served via Nginx.

### **Auxiliary**

* **Device Simulator**: A standalone application that generates synthetic energy consumption data and sends it to RabbitMQ.

## **🔌 Services & Ports**

The following ports are exposed on your localhost when running the project:

| Service | Container Name | Local Port | Internal Port | Description |
| :---- | :---- | :---- | :---- | :---- |
| **Frontend** | frontend | 80 | 80 | Main Web UI (React \+ Nginx) |
| **Traefik** | traefik | 80 (Router) 8080 (Dash) | 80 8080 | API Gateway & Dashboard |
| **Auth Service** | auth-service | 8081 | 8081 | Authentication API |
| **User Service** | user-service | 8082 | 8082 | User Management API |
| **Device Service** | device-service | 8083 | 8083 | Device Management API |
| **Monitoring** | monitoring-service | *Not exposed* | 8084 | Accessible via Gateway only |
| **RabbitMQ** | rabbitmq | 5672 15672 | 5672 15672 | AMQP Protocol Management UI |

*Note: The databases (postgres-\*-db) run on standard port 5432 internally but are not exposed to the host to avoid conflicts.*

## **🛠️ Technologies**

* **Backend**: Java 25, Spring Boot 3.x, Hibernate, Spring AMQP  
* **Frontend**: React.js, Bootstrap, Nginx  
* **Database**: PostgreSQL 16  
* **Messaging**: RabbitMQ 3.13  
* **Containerization**: Docker, Docker Compose

## **🚀 Installation and Setup**

### **Prerequisites**

* Docker Desktop (running)  
* Git

### **Steps**

1. Clone the repository to your local machine.  
2. Open a terminal in the root directory of the project (where docker-compose.yml is located).  
3. Build and start the containers using the following command:

docker-compose up \--build

### **Stopping the Application**

To stop the application and remove the containers/volumes (resetting all databases):

docker-compose down \-v

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

### **Features by Role**

**Administrator**:

* Login to the platform.  
* Create, update, and delete user accounts (Clients).  
* Create, update, and delete devices.  
* Assign devices to specific clients.

**Client**:

* Login to the platform.  
* View the list of devices assigned to their account.  
* View historical energy consumption charts for their devices (data provided by the simulator).

## **🐛 Troubleshooting**

Database connection errors?  
Wait a few moments. The microservices might try to connect before the PostgreSQL containers are fully ready. They are configured to restart automatically on failure.  
RabbitMQ connection failed?  
If the services cannot connect to RabbitMQ, ensure the rabbitmq container is healthy. You can check its status in Docker Desktop.