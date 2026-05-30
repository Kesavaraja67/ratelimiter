# Rate Limiter Service

A production-grade API rate limiting engine built with Java and Spring Boot. Uses a sliding window algorithm backed by Redis to enforce per-user request limits in real time, with full request history persisted in MySQL.

---

## What It Does

Every incoming API request passes through a filter that validates the caller's API key, runs a sliding window check against Redis, and either allows or rejects the request. Allowed and blocked requests are both logged to MySQL for analytics and anomaly tracking. A nightly scheduler purges records older than 30 days automatically.

---

## Architecture

```
Incoming Request
      │
      ▼
ApiKeyFilter (OncePerRequestFilter)
      │
      ├── No API key         →  401 Unauthorized
      ├── Invalid API key    →  401 Unauthorized
      ├── User deactivated   →  403 Forbidden
      ├── Rate limit hit     →  429 Too Many Requests
      │
      └── All checks pass    →  Controller → 200 OK
```

**Three-layer structure**

- `filter/` — intercepts every request before it reaches any controller
- `controller/` — handles HTTP endpoints, delegates to services
- `service/` — business logic, rate limit calculations, user management
- `repository/` — database access only, no logic
- `model/` — JPA entity classes mapped to MySQL tables
- `scheduler/` — nightly cleanup job

---

## Sliding Window Algorithm

Unlike a fixed window (which resets every 60 seconds and can be gamed at the boundary), the sliding window moves forward in real time with every request.

At any moment, the window covers exactly the last 60 seconds from now. Redis sorted sets store each request timestamp as a score. On every incoming request:

1. Remove all entries older than `now - 60s` using `removeRangeByScore`
2. Count remaining entries with `zCard`
3. If count is under the limit, add the new timestamp and allow
4. If count is at or over the limit, reject with 429

This approach handles burst traffic correctly and runs in O(log n) per request.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Rate limit store | Redis (sorted sets via Spring Data Redis) |
| Persistence | MySQL 9 + Spring Data JPA + Hibernate 7 |
| Auth | API key via request header |
| Build | Maven |
| Deployment | Railway |

---

## API Endpoints

### Public (no key required)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/admin/users` | Create a new user and generate their API key |
| PUT | `/admin/users/{id}` | Deactivate a user |

### Protected (requires `X-API-KEY` header)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/ping` | Test endpoint, returns current request count in window |
| GET | `/api/stats` | Full usage stats for the authenticated user |

---

## Getting Started

**Prerequisites**

- Java 21
- MySQL running locally
- Redis running locally

**Setup**

```bash
git clone https://github.com/yourusername/ratelimiter.git
cd ratelimiter
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties` with your MySQL credentials, then run:

```bash
./mvnw spring-boot:run
```

Spring Boot will auto-create all database tables on first startup.

**Create your first user**

```bash
curl -X POST http://localhost:8080/admin/users \
  -H "Content-Type: application/json" \
  -d '{"name": "testuser", "maxRequestsPerMinute": 100}'
```

Copy the `apiKey` from the response and use it in all subsequent requests.

**Make a protected request**

```bash
curl http://localhost:8080/api/ping \
  -H "X-API-KEY: your-api-key-here"
```

---

## Configuration

All rate limit settings are in `application.properties` and can be overridden with environment variables at deployment time.

```properties
app.rate-limit.max-requests-per-minute=100
app.rate-limit.window-size-seconds=60
```

Individual users can have different limits set at creation time via `maxRequestsPerMinute`.

---

## Environment Variables for Deployment

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | MySQL connection string |
| `SPRING_DATASOURCE_USERNAME` | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password |
| `SPRING_DATA_REDIS_HOST` | Redis host |
| `SPRING_DATA_REDIS_PORT` | Redis port |

---

## Key Concepts Demonstrated

- `OncePerRequestFilter` for request interception before controllers
- Redis sorted sets for O(log n) sliding window rate limiting
- Spring Data JPA derived queries with zero SQL
- Constructor injection via Lombok `@RequiredArgsConstructor`
- `@Value` for externalized configuration
- `@Scheduled` with cron expression for nightly jobs
- Layered architecture with strict separation between controller, service, and repository
