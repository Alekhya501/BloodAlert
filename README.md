# BloodAlert
bllodalert is an intelligent, realtime blood donation alert system that bridges the critical gap between hospitals in need and donors willing to help. when hospitals urgently need blood of specific groups, BloodAlert sends instant notification to registered donors in the vicinity, enabling  them to donate immediately and save lives.
# Smart Blood Bank & Emergency Donor System

## Problem Statement

In India, hospitals face a critical challenge during medical emergencies — finding the right blood group donor in time. The current process is entirely manual. Hospital staff open a register, pick up a phone, and start calling donors one by one. This process takes 2 to 3 hours. In emergencies, those hours cost lives.

There is no central system where donors are registered and available for all hospitals. There is no way to alert multiple donors instantly. There is no tracking of who donated recently and who is eligible to donate again.

This project solves that problem by building a centralized digital platform that connects hospitals and donors instantly — using a microservices architecture with Java, Spring Boot, MySQL, Kafka, JWT, and GraphQL.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core language for all microservices |
| Spring Boot 3.2 | Framework for building REST APIs |
| Spring Security | Password encoding and route protection |
| Spring Data JPA | MySQL database access layer |
| MySQL | Relational database for all services |
| JWT (jjwt 0.11.5) | Token-based stateless authentication |
| Apache Kafka | Async event-driven communication between services |
| GraphQL | Flexible donor search queries |
| Spring Cloud Gateway | API Gateway — single entry point for all requests |
| Eureka Server | Service discovery and registration |
| Resilience4j | Circuit breaker for fault tolerance |
| Lombok | Reduces boilerplate code |
| Maven | Build and dependency management |

---

## Microservices Overview

| Service | Port | Database | Communication |
|---|---|---|---|
| Eureka Server | 8761 | None | — |
| Config Server | 8888 | None | REST |
| API Gateway | 8086 | None | Routes all requests |
| Auth Service | 8085 | auth_db (MySQL) | REST |
| Donor Service | 8084 | donor_db (MySQL) | REST + GraphQL + Kafka consumer |
| Hospital Service | 8083 | hospital_db (MySQL) | REST |
| Notification Service | 8082 | notification_db (MySQL) | Kafka consumer |
| Request Service | 8081 | request_db (MySQL) | REST + Kafka producer |

---

## Database Setup

Run these SQL commands once before starting any service:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE donor_db;
CREATE DATABASE request_db;
CREATE DATABASE hospital_db;
CREATE DATABASE notification_db;
```

Spring Boot creates all tables automatically on startup using `ddl-auto: update`.

---

## Complete Flow — Step by Step

---

### Flow 1 — Donor Registration

A person who wants to donate blood registers on the platform.

**Request:**
```
POST /auth/register
Body:
{
  "name": "Ravi Kumar",
  "email": "ravi@gmail.com",
  "password": "ravi1234",
  "role": "DONOR",
  "bloodGroup": "O+",
  "city": "Hyderabad"
}
```

**What Auth Service does:**
1. Checks if email already exists in `auth_db.users` table
2. If duplicate — throws 409 CONFLICT error
3. Hashes the password using BCrypt before saving
4. Saves user to `auth_db.users` table with role = DONOR
5. Generates JWT access token containing userId and role = DONOR
6. Generates JWT refresh token containing userId only
7. Makes internal REST call to Donor Service:
   `POST /donors/internal/create` with userId, name, email, bloodGroup, city
8. Returns JWT token immediately to the client

**Donor Service receives internal call and:**
1. Creates a donor record in `donor_db.donors` table
2. Sets `available = true` by default
3. Sets `lastDonatedAt = null` — never donated yet

**What the donor receives:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "DONOR",
  "userId": 1
}
```

**Data stored after this step:**
- `auth_db.users` → id, name, email, password(hashed), role = DONOR, createdAt
- `donor_db.donors` → id, userId, name, email, bloodGroup, city, available = true

---

### Flow 2 — Donor Login

The donor opens the app on a later day and logs in.

**Request:**
```
POST /auth/login
Body:
{
  "email": "ravi@gmail.com",
  "password": "ravi1234"
}
```

**What Auth Service does:**
1. Finds user by email in `auth_db.users`
2. If email not found — throws 404 NOT FOUND
3. Compares entered password with BCrypt hashed password using `passwordEncoder.matches()`
4. If password wrong — throws 401 UNAUTHORIZED
5. Generates new JWT access token and refresh token
6. Returns tokens to client

**The donor saves the token and attaches it to every future request:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### Flow 3 — Hospital Registration

A hospital administrator registers the hospital on the platform.

**Request:**
```
POST /auth/register
Body:
{
  "name": "Apollo Admin",
  "email": "admin@apollo.com",
  "password": "apollo1234",
  "role": "HOSPITAL"
}
```

**What Auth Service does:**
1. Checks email for duplicates
2. Hashes password and saves user with role = HOSPITAL
3. Generates JWT token with role = HOSPITAL
4. Makes internal REST call to Hospital Service:
   `POST /hospitals/internal/create` with userId, name, email, city
5. Returns token immediately

**Hospital Service receives internal call and:**
1. Saves hospital record in `hospital_db.hospitals` table

**What the hospital admin receives:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "HOSPITAL",
  "userId": 2
}
```

---

### Flow 4 — Hospital Login

```
POST /auth/login
Body:
{
  "email": "admin@apollo.com",
  "password": "apollo1234"
}
```

Same flow as donor login. Returns JWT token with role = HOSPITAL.

---

### Flow 5 — Admin Registration

Admin is created by calling the same register endpoint with role = ADMIN.
There is no public admin panel — admin is created once manually.

```
POST /auth/register
Body:
{
  "name": "System Admin",
  "email": "admin@bloodbank.com",
  "password": "admin1234",
  "role": "ADMIN"
}
```

---

### Flow 6 — Every Request Goes Through API Gateway

From this point every request from every client goes to port 8080.
No client ever calls a service directly on its own port.

**What API Gateway does on every request:**
1. Reads the `Authorization: Bearer <token>` header
2. If header is missing — returns 401 immediately, request never reaches service
3. Validates the JWT token using JwtUtil
4. If token is expired or invalid — returns 401
5. Extracts userId and role from token claims
6. Adds `X-User-Id` and `X-User-Role` headers to the forwarded request
7. Uses Eureka to find the correct service by name
8. Routes the request to the correct service
9. If service is down — Resilience4j circuit breaker returns 503

**The only endpoints that skip JWT validation:**
```
POST /auth/register
POST /auth/login
```

---

### Flow 7 — Hospital Posts Emergency Blood Request

A patient arrives needing blood urgently. Hospital staff raises an emergency request.

**Request:**
```
POST /requests
Headers: Authorization: Bearer <hospital-token>
Body:
{
  "bloodGroup": "O+",
  "unitsNeeded": 2,
  "city": "Hyderabad",
  "urgencyLevel": "HIGH"
}
```

**What API Gateway does:**
1. Validates JWT token
2. Adds X-User-Id: 2 and X-User-Role: HOSPITAL to headers
3. Routes to Request Service

**What Request Service does:**
1. Reads hospitalId from X-User-Id header
2. Validates role is HOSPITAL — if not returns 403
3. Creates BloodRequest entity with status = PENDING
4. Saves to `request_db.blood_requests` table
5. Returns 201 CREATED to hospital immediately
6. Publishes Kafka event to `blood.request.created` topic

**Kafka event published:**
```json
{
  "requestId": "1",
  "bloodGroup": "O+",
  "city": "Hyderabad",
  "unitsNeeded": 2,
  "urgencyLevel": "HIGH"
}
```

**Hospital receives 201 immediately — does not wait for SMS to be sent.**

---

### Flow 8 — Notification Service Alerts Donors via Kafka

Notification Service is always listening to `blood.request.created` topic.

**What Notification Service does automatically:**
1. Receives the Kafka event
2. Reads bloodGroup = O+ and city = Hyderabad from event
3. Calls Donor Service via REST internally:
   `GET /donors/search?bloodGroup=O+&city=Hyderabad`
4. Donor Service returns all available eligible donors
   — those with O+ or compatible blood group in Hyderabad
   — who have not donated in last 90 days
   — whose available = true
5. Sends SMS to every donor in the list
6. Saves a notification log for each SMS in `notification_db.notification_logs`

**SMS received by donors:**
```
Emergency: O+ blood needed in Hyderabad.
2 units required. Please respond on the app.
```

---

### Flow 9 — Donor Updates Availability

Donor can toggle their availability on or off.

**Request:**
```
PUT /donors/1/availability
Headers: Authorization: Bearer <donor-token>
Body:
{
  "available": true
}
```

**What Donor Service does:**
1. Reads userId from X-User-Id header
2. Finds donor record in `donor_db.donors`
3. Updates `available` field
4. Returns updated donor profile

If a donor sets `available = false` they will not appear in any search results
until they set it back to true.

---

### Flow 10 — Donor Searches Using GraphQL

Hospital can search for donors using GraphQL for flexible filtering.

**GraphQL Query:**
```graphql
query {
  searchDonors(bloodGroup: "O+", city: "Hyderabad") {
    id
    name
    phone
    bloodGroup
    city
    available
    lastDonatedAt
  }
}
```

**What Donor Service does:**
1. GraphQL resolver receives the query
2. Calls DonorService.searchDonors(bloodGroup, city)
3. Finds all eligible available donors matching filters
4. Returns only the fields the hospital asked for

---

### Flow 11 — Donor Responds to Emergency Alert

The donor receives the SMS and accepts the request in the app.

**Request:**
```
PUT /requests/1/respond
Headers: Authorization: Bearer <donor-token>
Body:
{
  "donorId": "1",
  "donorName": "Ravi Kumar",
  "donorPhone": "9876543210",
  "status": "ACCEPTED"
}
```

**What Request Service does:**
1. Fetches BloodRequest by id
2. Checks request is still PENDING or MATCHED — not already FULFILLED
3. Saves DonorResponse to `request_db.donor_responses` table
4. Updates request status from PENDING to MATCHED
5. Returns 200 OK to donor immediately
6. Publishes Kafka event to `donor.response` topic

**Kafka event published:**
```json
{
  "requestId": "1",
  "donorId": "1",
  "donorName": "Ravi Kumar",
  "responseStatus": "ACCEPTED",
  "respondedAt": "2024-01-15T10:30:00"
}
```

---

### Flow 12 — Hospital Views Donor Responses

The hospital checks which donors accepted.

**Request:**
```
GET /requests/1
Headers: Authorization: Bearer <hospital-token>
```

**Response:**
```json
{
  "requestId": 1,
  "bloodGroup": "O+",
  "unitsNeeded": 2,
  "city": "Hyderabad",
  "status": "MATCHED",
  "createdAt": "2024-01-15T10:00:00",
  "donorResponses": [
    {
      "donorName": "Ravi Kumar",
      "donorPhone": "9876543210",
      "status": "ACCEPTED",
      "respondedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

Hospital calls Ravi directly. Ravi comes to the hospital and donates blood.

---

### Flow 13 — Hospital Marks Request as Fulfilled

After blood is collected the hospital marks the request as fulfilled.

**Request:**
```
PUT /requests/1/fulfill
Headers: Authorization: Bearer <hospital-token>
```

**What Request Service does:**
1. Fetches BloodRequest — validates hospitalId matches
2. Sets status = FULFILLED and fulfilledAt = now
3. Saves to MySQL
4. Returns 200 OK immediately
5. Publishes Kafka event to `request.fulfilled` topic

**Kafka event published:**
```json
{
  "requestId": "1",
  "donorId": "1",
  "bloodGroup": "O+",
  "city": "Hyderabad",
  "fulfilledAt": "2024-01-15T11:00:00"
}
```

**Two services consume this event simultaneously:**

**Donor Service consumes `request.fulfilled`:**
1. Finds donor by donorId in MySQL
2. Inserts a record into `donor_db.donation_history` table
3. Updates `lastDonatedAt` to today in `donor_db.donors`
4. Donor will not appear in search results for 90 days

**Notification Service consumes `request.fulfilled`:**
1. Finds all notification logs for requestId 1
2. Marks them as CLOSED
3. No more SMS alerts go out for this request

---

### Flow 14 — Donor Views Donation History

```
GET /donors/me
Headers: Authorization: Bearer <donor-token>
```

**Response:**
```json
{
  "name": "Ravi Kumar",
  "email": "ravi@gmail.com",
  "bloodGroup": "O+",
  "city": "Hyderabad",
  "available": true,
  "lastDonatedAt": "2024-01-15",
  "nextEligibleDate": "2024-04-15",
  "donationHistory": [
    {
      "hospitalName": "Apollo Hospitals",
      "bloodGroup": "O+",
      "donatedAt": "2024-01-15"
    }
  ]
}
```

---

### Flow 15 — Hospital Views Its Dashboard

```
GET /hospitals/me/dashboard
Headers: Authorization: Bearer <hospital-token>
```

**What Hospital Service does:**
1. Finds hospital by userId from X-User-Id header
2. Calls Request Service internally to get all requests by this hospital
3. Counts total, pending, matched, fulfilled requests
4. Returns dashboard summary

**Response:**
```json
{
  "hospitalName": "Apollo Hospitals",
  "city": "Hyderabad",
  "totalRequests": 10,
  "pendingRequests": 2,
  "matchedRequests": 3,
  "fulfilledRequests": 5
}
```

---

## Kafka Topics Summary

| Topic | Published by | Consumed by | When |
|---|---|---|---|
| blood.request.created | Request Service | Notification Service | Hospital posts emergency request |
| donor.response | Request Service | Donor Service | Donor accepts or declines |
| request.fulfilled | Request Service | Notification Service + Donor Service | Hospital marks request fulfilled |

---

## JWT Token Summary

| Field | Value |
|---|---|
| Subject | userId |
| Claim: role | DONOR / HOSPITAL / ADMIN |
| Expiry | 24 hours |
| Signing algorithm | HS256 |
| Validated by | API Gateway on every request |

**Token payload example:**
```json
{
  "sub": "1",
  "role": "DONOR",
  "iat": 1716540000,
  "exp": 1716626400
}
```

---

## Role-Based Access Control

| Role | Allowed endpoints |
|---|---|
| DONOR | POST /auth/register, POST /auth/login, PUT /donors/{id}/availability, GET /donors/me, PUT /requests/{id}/respond |
| HOSPITAL | POST /auth/register, POST /auth/login, POST /requests, GET /requests/{id}, PUT /requests/{id}/fulfill, GET /hospitals/me/dashboard, GET /donors/search (GraphQL) |
| ADMIN | All endpoints |

---

## Error Responses

| Scenario | HTTP Status | Message |
|---|---|---|
| Email already registered | 409 CONFLICT | "Email already registered." |
| Email not found on login | 404 NOT FOUND | "Email not registered." |
| Wrong password | 401 UNAUTHORIZED | "Incorrect password." |
| Missing or invalid JWT | 401 UNAUTHORIZED | "Unauthorized." |
| Wrong role accessing endpoint | 403 FORBIDDEN | "Access denied." |
| Resource not found | 404 NOT FOUND | "Resource not found." |
| Validation failure | 400 BAD REQUEST | Field-level error messages |
| Service down | 503 SERVICE UNAVAILABLE | "Service temporarily unavailable." |

---

## Build Order

Build and test each service in this exact order. See `SERVICE_FLOW.md` for detailed endpoint checks.

```
1. Eureka Server         → verify at http://localhost:8761
2. Config Server         → verify at http://localhost:8888
3. Auth Service          → register, login with email and password
4. Donor Service         → donor profile, search, GraphQL
5. Hospital Service      → register hospital profile
6. Request Service       → blood requests, Kafka producer
7. Notification Service  → Kafka consumer, SMS alerts
8. API Gateway           → JWT filter, routing, circuit breaker
```

---

## What This System Replaces

| Problem Today | Solution in This System |
|---|---|
| Hospital calls donors one by one | One request alerts all matching donors via SMS |
| Finding a donor takes 2-3 hours | Donors alerted within seconds via Kafka |
| No central donor registry | All donors searchable by blood group and city |
| No eligibility check | System excludes donors who donated in last 90 days |
| No security on donor data | JWT + role-based access on every endpoint |
| Single point of failure | Microservices — each service runs independently |
| No history tracking | Full donation history per donor with next eligible date |