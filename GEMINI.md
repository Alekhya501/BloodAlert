# BloodAlert (Smart Blood Bank)

BloodAlert is an intelligent, real-time blood donation alert system designed to bridge the gap between hospitals in need and registered donors. When a hospital urgently requires a specific blood group, BloodAlert sends instant notifications to donors in the vicinity.

## Project Architecture

The project follows a microservices architecture using Spring Boot and Spring Cloud.

### Core Services
- **eureka-service**: Service discovery and registration (Port: 8761).
- **config_server**: Centralized configuration management (Port: 8888). Configured to fetch properties from an external Git repository.
- **api-gateway-service**: Entry point for all client requests (Port: 8086). Routes:
    - `/api/donors/**` -> `donor-service`
    - `/api/hospitals/**` -> `hospital-service`
    - `/api/requests/**` -> `request-service`
- **auth-service**: Handles authentication and authorization (Skeleton).

### Business Services
- **donor-service**: Manages donor profiles with **spatial location (MySQL Spatial)**.
    - Endpoint: `POST /api/donors/register` - Register a donor with lat/long.
    - Endpoint: `GET /api/donors/search` - Search for nearby donors by blood group.
- **hospital-service**: Manages hospital records with spatial location.
    - Endpoint: `POST /api/hospitals/register` - Register a hospital.
- **request-service**: Orchestrates blood requests.
    - Endpoint: `POST /api/requests` - Create a request and publish a Kafka event.
- **notification-service**: Orchestrates alerts.
    - Listens for `blood-requests-topic`.
    - Uses **OpenFeign** to fetch nearby donors from `donor-service`.
    - Logs simulated notifications (Email/SMS).

## Technology Stack
- **Language**: Java 25
- **Framework**: Spring Boot 4.0.2, Spring Cloud 2025.1.1
- **Messaging**: Apache Kafka (Topic: `blood-requests-topic`)
- **Database**: MySQL with Hibernate Spatial
- **API**: REST (implemented) and GraphQL (planned)
- **Build Tool**: Maven

## Building and Running

### Prerequisites
- JDK 25
- Maven 3.9+
- MySQL (with Spatial support)
- Apache Kafka

### Running Services
For the correct startup sequence and end-to-end flow verification, refer to **[SERVICE_FLOW.md](./SERVICE_FLOW.md)**.

1. **eureka-service**
2. **config_server**
3. **api-gateway-service**
4. **donor-service**, **hospital-service**, **request-service**, **notification-service**

## Development Conventions
- **Centralized Configuration**: All service configurations are managed via the `config_server` using the `config-repo/` directory in the project root. Configurations are stored in **YAML (.yml)** format.
- **Geospatial Queries**: Use `ST_Distance_Sphere` in native queries for proximity searches.
- **Inter-service Comm**: Prefer Kafka for async (notifications) and Feign for sync (data fetching).
- **Lombok**: Extensively used for DTOs and Entities.

## TODOs / Future Work
- Implement full security in `auth-service`.
- Implement GraphQL resolvers.
- Add real notification provider integration (Twilio/Firebase).
