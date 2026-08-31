# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
## Architecture

**Layered structure:** `controller → service → repository → model/dao`

All packages are under `pl.ib.beauty.*`:
- `controller` — REST endpoints with `@PreAuthorize` for role-based access
- `service` — Business logic; uses `@Transactional`, `@Cacheable`, `@CachePut`
- `repository` — `JpaRepository` extensions with JPQL `@Query` for complex queries
- `model/dao` — JPA entities; implement `IdentifiedDataSerializable` for Hazelcast serialization
- `model/dto` — DTOs named `{Entity}DtoRequest` / `{Entity}DtoResponse`
- `mapper` — MapStruct interfaces for DAO ↔ DTO conversion
- `config` — Spring configuration beans (Security, JWT, CORS, Hazelcast, AWS, Stripe, WebSocket)
- `security` — Auth services: `LoginService`, `TokenService`, `UserDetailsServiceImpl`, `Oauth2LoginSuccessHandler`
- `exception` — Custom exception classes

## Security

**Authentication:** JWT (RSA key pair). Tokens contain email and roles as `scope` claim; 24h expiry.  
**OAuth2:** Google login supported via `Oauth2LoginSuccessHandler`.  
**Authorization:** Method-level `@PreAuthorize` with `SCOPE_TEACHER`, `SCOPE_ADMIN` authorities, plus a custom `@securityService.isCourseCreator(#id)` SpEL expression.  
**Sessions:** Stateless (`SessionCreationPolicy.STATELESS`).

## Database & Migrations

**Production & Tests:** PostgreSQL only. Migrations live in `src/main/resources/db/migration/postgresql/`.  
**Flyway** manages all schema changes — `ddl-auto` is set to `none`. New schema changes require a new versioned migration file (e.g., `V27__description.sql`). Use `/project:migration <description>` to create one automatically.  
**Audit trail:** Hibernate Envers tracks entity revisions. The main class enables `@EnableJpaRepositories(repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)`.

## Key Integrations

| Integration | Purpose | Config class |
|---|---|---|
| Hazelcast 5.5 | Distributed cache (Course entity, 1h TTL, LRU) | `HazelcastConfig` |
| AWS S3 | File/image uploads | `S3Config` / `S3Service` |
| Stripe | Payment processing | `StripeConfig` / `PaymentService` |
| Google Maps | Address geocoding (lat/lng) | `GoogleMapsConfig` |
| Spring AI (Anthropic Claude) | AI features | `AIController` / `AIService` |
| WebSocket (STOMP) | Real-time notifications | `WebSocketConfig` / `NotificationSocketService` |
| Thymeleaf + SMTP | Email templates | `MailService` |

## Required Environment Variables

```
DB_URL, DB_USERNAME, DB_PASSWORD
MAIL_USERNAME, MAIL_PASSWORD
GOOGLE_CLIENT_ID, GOOGLE_SECRET
JWT_PRIVATE_KEY, JWT_PUBLIC_KEY
AWS_ACCESS_KEY, AWS_SECRET_KEY
ANTHROPIC_API_KEY
GOOGLE_ACCESS_KEY
STRIPE_SECRET_KEY
```

Tests load JWT keys from classpath (`classpath:public-key.pem`, `classpath:pkcs8.key`) via `application-test.yml`.

## CORS

Allowed origins: `http://localhost:4200` (dev) and `https://beauty-front.onrender.com/` (prod). Configured in `CorsConfig`.

## Testing

Uses both **JUnit 5** and **Spock (Groovy BDD)** for tests. TestContainers spins up a real PostgreSQL for integration tests — no database mocking. The active Spring profile for tests is `test` (`application-test.yml`).
