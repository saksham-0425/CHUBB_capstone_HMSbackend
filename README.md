# Hotel Management System - Backend

A scalable, microservice-based backend system for managing hotel operations including room inventory, bookings, real-time availability, check-in/check-out workflows, billing, and operational reporting.

The system is designed following enterprise backend principles, emphasizing clear domain ownership, transactional consistency, secure role-based access, and high-performance availability handling using Redis.

## Project Overview

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.x-6DB33F?logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Authentication-black?logo=jsonwebtokens)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Hibernate-6DB33F?logo=spring)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?logo=hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Cache%20%26%20Locks-DC382D?logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Event%20Broker-FF6600?logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containers-2496ED?logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Orchestration-2496ED?logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![Eureka](https://img.shields.io/badge/Eureka-Service%20Discovery-6DB33F?logo=spring)
![Config Server](https://img.shields.io/badge/Spring%20Config-Centralized%20Config-6DB33F?logo=spring)

This backend serves as the core business layer for the Hotel Management System.
It exposes REST APIs consumed by an Angular frontend and coordinates hotel operations across multiple roles:

- Admin
- Hotel Manager
- Receptionist
- Guest

The architecture avoids unnecessary services and focuses on correctness, simplicity, and scalability, making it suitable for real-world hotel reservation workflows.

## High-Level Architecture

<img width="2418" height="1210" alt="image" src="https://github.com/user-attachments/assets/6e31c0bf-fad2-4316-adfa-08c0f73befee" />

The backend follows a microservice architecture with database-per-service and a central API Gateway.

Key Architectural Characteristics :-

- Stateless services with JWT-based authentication
- Strong separation of concerns
- No shared databases
- Redis-based availability & concurrency control
- Booking Service as the transactional core
- Reports generated directly from booking data (no reporting service)

## Backend Components

### 1. API Gateway
Role: System entry point

Responsibilities:

- Acts as the single external access point
- Validates JWT tokens
- Extracts user identity and role
- Routes requests to downstream services
- Performs coarse-grained authorization

Shields internal services from direct exposure and centralizes cross-cutting concerns.

### 2. Auth Service
Role: Identity & Security Backbone

Responsibilities:
- User registration and authentication
- Secure password handling (BCrypt hashing)
- JWT token generation and validation
- Role management

Owns:
- Users
- Roles
- Credentials

API's:
- POST  /auth/register                 (register the person as user)
- POST  /auth/login                    (common login for all the roles, redirecting based on the headers received)
- POST  /auth/internal/receptionists   (register the user as receptionist accessible by MANAGER only)
- POST  /auth/internal/create-manager  (create the user as manager, accessible by ADMIN only)

Database: Auth DB
Register (USER):-
<img width="1284" height="597" alt="image" src="https://github.com/user-attachments/assets/952dd00f-49aa-4542-8369-28e34c5433f5" />
Login (ADMIN):-
<img width="1255" height="621" alt="image" src="https://github.com/user-attachments/assets/38c045f1-b4fa-48bd-b3be-b11c52a89984" />

### 3. Hotel Service

Role: Static Domain & Inventory Authority

Responsibilities:
- Manage hotels and metadata
- Manage room categories
- Manage physical rooms
- Maintain room status (AVAILABLE, OCCUPIED, MAINTENANCE)
- Map managers to hotels

Owns:
- Hotels
- Room categories
- Rooms

Database: Hotel DB

### 4. Booking Service

Role: Transactional Core of the System

Responsibilities:

- Booking creation and lifecycle management
- Availability validation using Redis
- Check-in and check-out workflows
- Billing calculation
- Operational and analytical report generation

Owns:
- Bookings
- Booking status
- Stay dates
- Check-in / check-out timestamps
- Billing and revenue data

Database: Booking DB

All reports are generated from Booking DB using optimized queries.
No separate reporting service or reporting database is used by design.

### 5. Notification Service
Role: Asynchronous Side-Effect Handler

Responsibilities:
- Booking confirmations
- Check-in and check-out notifications
- User communication
- Design Principle:
- Asynchronous and non-blocking
- Failures do not impact booking transactions

Design Principle:
- Asynchronous and non-blocking
- Failures do not impact booking transactions

### 6. Redis – Availability & Concurrency Control
Redis is a critical runtime component, used as a derived state store, not a primary database.

Why Redis?
- Fast availability lookups
- Prevents race conditions during concurrent bookings
- Scales independently from relational databases

Stored in Redis
- Available room counts per:
- Hotel
- Room category
- Date
- Temporary locks during booking creation

Not Stored in Redis
- Bookings
- Rooms
- Users
- Billing data

Redis data is always rebuildable from Booking DB, ensuring system resilience.

### 7. Booking Lifecycle
```
BOOKED → CONFIRMED → CHECKED_IN → CHECKED_OUT
        ↘
         CANCELLED
```
### 8. Databases
```
| Service         | Database   | Responsibility               |
| --------------- | ---------- | ---------------------------- |
| Auth Service    | Auth DB    | Users & roles                |
| Hotel Service   | Hotel DB   | Hotels & rooms               |
| Booking Service | Booking DB | Reservations, stays, billing |
```
### Docker and Jenkins Pipeline:-
<img width="1891" height="931" alt="image" src="https://github.com/user-attachments/assets/d6bfee98-6e37-4aa1-bd65-391ad7e0a42a" />

### service-registry:-
<img width="1887" height="923" alt="image" src="https://github.com/user-attachments/assets/72ea62cd-9fa5-40d9-a532-e8ec31be8ba0" />

### Frontend page:-
<img width="1895" height="945" alt="image" src="https://github.com/user-attachments/assets/975a334b-2068-4de9-b821-263d64d4ab40" />

### Jacoco Reports
<img width="1919" height="550" alt="image" src="https://github.com/user-attachments/assets/1b1b4515-6e45-4d19-8a95-1854b1d5b7dd" />
<img width="1919" height="320" alt="image" src="https://github.com/user-attachments/assets/cc4011ad-b930-4dae-8237-afee1db033f4" />
<img width="1919" height="476" alt="image" src="https://github.com/user-attachments/assets/07800fb3-2646-464b-b5b0-1eb99f2497c3" />



