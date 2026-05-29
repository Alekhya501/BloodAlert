# Service Execution & End-to-End Flow Guide

This document outlines the startup sequence for the BloodAlert microservices and the step-by-step API flow to verify the system.

## 1. Startup Order (Mandatory)

Services must be started in this specific order to ensure discovery and configuration are available.

| Order | Service Name | Port | Verification URL                                                                           |
| :--- | :--- |:-----|:-------------------------------------------------------------------------------------------|
| 1 | **Eureka Server** | 8761 | [http://localhost:8761](http://localhost:8761) (Dashboard)                                 |
| 2 | **Config Server** | 8888 | [http://localhost:8888/donor-service/default](http://localhost:8888/donor-service/default) |
| 3 | **Auth Service** | 8085 | [http://localhost:8085/actuator/health](http://localhost:8085/actuator/health)             |
| 4 | **Donor Service** | 8084 | [http://localhost:8084/actuator/health](http://localhost:8084/actuator/health)             |
| 5 | **Hospital Service** | 8083 | [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)             |
| 6 | **Request Service** | 8081 | [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)             |
| 7 | **Notification Service** | 8082 | [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health)             |
| 8 | **API Gateway** | 8086 | [http://localhost:8086/actuator/health](http://localhost:8086/actuator/health)             |

---

## 2. End-to-End Flow (API Checklist)

All client requests should be directed through the **API Gateway (Port 8086)**.

### Phase 1: Registration & Authentication

1.  **Register a Donor**
    *   **Endpoint**: `POST http://localhost:8086/api/auth/register`
    *   **Body**:
        ```json
        {
          "name": "John Doe",
          "email": "john@example.com",
          "password": "password123",
          "role": "DONOR",
          "bloodGroup": "O+",
          "latitude": 17.3850,
          "longitude": 78.4867
        }
        ```

2.  **Login to get JWT**
    *   **Endpoint**: `POST http://localhost:8086/api/auth/login`
    *   **Body**: `{"email": "john@example.com", "password": "password123"}`
    *   **Action**: Copy the `accessToken` from the response. Use it as a Bearer token in all subsequent requests.

### Phase 2: Hospital Emergency Request

3.  **Register a Hospital**
    *   **Endpoint**: `POST http://localhost:8086/api/auth/register` (Role: `HOSPITAL`)
    *   **Action**: Login as the hospital to get a Hospital JWT.

4.  **Create Blood Request**
    *   **Endpoint**: `POST http://localhost:8086/api/requests`
    *   **Header**: `Authorization: Bearer <HOSPITAL_TOKEN>`
    *   **Body**:
        ```json
        {
          "bloodGroup": "O+",
          "unitsNeeded": 2,
          "latitude": 17.3850,
          "longitude": 78.4867,
          "urgency": "URGENT"
        }
        ```
    *   **Expected Result**: Request is saved; Kafka event is published.

### Phase 3: Notification & Search

5.  **Search Nearby Donors (Verification)**
    *   **Endpoint**: `GET http://localhost:8086/api/donors/search?bloodGroup=O+&latitude=17.3850&longitude=78.4867&radius=10000`
    *   **Header**: `Authorization: Bearer <ANY_VALID_TOKEN>`
    *   **Expected Result**: Returns the registered donor "John Doe".

6.  **Verify Notification Logs**
    *   **Endpoint**: `GET http://localhost:8086/api/notifications`
    *   **Header**: `Authorization: Bearer <ANY_VALID_TOKEN>`
    *   **Expected Result**: List containing the notification sent to "John Doe".

### Phase 4: Fulfillment & Management

7.  **Respond to Request**
    *   **Endpoint**: `POST http://localhost:8086/api/requests/{requestId}/respond` (Note: This endpoint is part of the logical flow but currently handled by hospital/admin manually updating status or via future logic).
    *   **Update Status manually**: `PATCH http://localhost:8086/api/requests/{requestId}/status?status=FULFILLED`
    *   **Header**: `Authorization: Bearer <HOSPITAL_TOKEN>`

8.  **View All Data (Admin/Audit)**
    *   **All Donors**: `GET http://localhost:8086/api/donors`
    *   **All Hospitals**: `GET http://localhost:8086/api/hospitals`
    *   **All Requests**: `GET http://localhost:8086/api/requests`
    *   **Requests by Hospital**: `GET http://localhost:8086/api/requests/hospital/{hospitalId}`
    *   **Notifications for Request**: `GET http://localhost:8086/api/notifications/request/{requestId}`

---

## 3. Full API Catalog (New Endpoints)

### Donor Service (Port 8084 via Gateway 8086)
- `GET /api/donors`: List all donors.
- `GET /api/donors/{id}`: Get donor details.
- `DELETE /api/donors/{id}`: Delete a donor.
- `PATCH /api/donors/{id}/availability`: Toggle donor availability.
- `GET /api/donors/search`: Spatial proximity search.

### Hospital Service (Port 8083 via Gateway 8086)
- `GET /api/hospitals`: List all hospitals.
- `GET /api/hospitals/{id}`: Get hospital details.
- `PUT /api/hospitals/{id}`: Update hospital info.
- `DELETE /api/hospitals/{id}`: Delete a hospital.

### Request Service (Port 8081 via Gateway 8086)
- `GET /api/requests`: List all blood requests.
- `GET /api/requests/{id}`: Get specific request details.
- `GET /api/requests/hospital/{hospitalId}`: List requests raised by a specific hospital.
- `PATCH /api/requests/{id}/status`: Update request status (`OPEN`, `CLOSED`, `FULFILLED`, `CANCELLED`).

### Notification Service (Port 8082 via Gateway 8086)
- `GET /api/notifications`: List all notification logs.
- `GET /api/notifications/request/{requestId}`: View who was notified for a specific request.
- `GET /api/notifications/donor/{donorId}`: View notification history for a specific donor.
