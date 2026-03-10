# Audit Command

Perform a comprehensive audit of this Java/Spring Boot project.

## Goal

Identify risks, weaknesses, and improvement opportunities in the codebase from the perspectives of:

- architecture
- maintainability
- correctness
- security
- performance
- concurrency
- transactional integrity
- observability
- testability
- production readiness

Do not only describe issues. For each important issue, explain:
1. what is wrong
2. why it is risky
3. how it can fail in production
4. how to improve it
5. what code or structural change is recommended

---

## Scope

Audit the project with special attention to the following areas:

### 1. Architecture and Layering
- Check whether controller, service, domain, repository, infrastructure, and config layers are clearly separated
- Identify layer violations and unnecessary coupling
- Detect business logic placed in controllers, entities, DTOs, or repositories
- Check whether external dependencies are properly isolated
- Review package/module structure for clarity and long-term maintainability

### 2. Domain and Business Logic
- Identify anemic domain patterns where business rules are scattered
- Check whether invariants and validation rules are enforced consistently
- Review whether method names and responsibilities reflect business intent
- Identify duplicated business logic across services or handlers

### 3. Spring Boot Practices
- Review bean scopes and singleton safety
- Check configuration properties usage and organization
- Review transaction boundaries (`@Transactional`) for correctness
- Detect misuse of Spring annotations or framework features
- Check for hidden framework coupling that harms testability

### 4. Persistence and Database Access
- Review JPA/Hibernate/MyBatis usage for correctness
- Detect N+1 query risks
- Check fetch strategies and unnecessary eager loading
- Review entity design, dirty checking risks, and persistence context misuse
- Identify places where batch processing or query optimization may be needed
- Check whether indexes appear necessary based on query usage
- Review locking strategy where concurrent updates may occur

### 5. Concurrency and State Safety
- Identify shared mutable state in singleton beans
- Detect possible race conditions
- Review static mutable fields and in-memory state usage
- Check thread-safety of caches, counters, and shared collections
- Review asynchronous execution, scheduler code, event handlers, and listener logic
- Point out areas requiring atomicity, locking, or redesign

### 6. Transactions and Consistency
- Review transactional boundaries and propagation assumptions
- Detect missing rollback considerations
- Check whether external calls are made inside transactions
- Identify possible consistency issues in event-driven flows
- For distributed flows, review compensation, idempotency, and retry safety
- Highlight risks around outbox, saga, message duplication, and partial failure handling

### 7. Security
- Check authentication and authorization flow
- Detect missing validation on input boundaries
- Review use of secrets, tokens, credentials, and sensitive configuration
- Identify logging of sensitive data
- Check for injection risks (SQL, JPQL, command, template)
- Review file upload/download handling
- Check CORS, CSRF, session, token, and header-related weaknesses where relevant
- Review Spring Security configuration for unsafe defaults or bypass risks

### 8. API Design
- Review controller design and REST consistency
- Check request/response DTO separation
- Identify weak validation and poor error response design
- Review API versioning and backward compatibility concerns
- Highlight naming inconsistencies and unclear contracts

### 9. Error Handling and Resilience
- Review exception hierarchy and error mapping
- Check for swallowed exceptions or overly broad catches
- Identify retry risks and duplicated side effects
- Review timeout, circuit breaker, fallback, and resilience strategy where relevant
- Check whether failures are observable and diagnosable

### 10. Logging and Observability
- Review log level usage and log quality
- Check whether logs include enough operational context
- Detect over-logging or sensitive data exposure
- Review tracing/correlation id propagation if applicable
- Check metrics, health checks, and monitoring readiness

### 11. Testing
- Review unit/integration test coverage quality
- Identify critical logic with weak or missing tests
- Check whether tests are too framework-dependent or brittle
- Review branch coverage for complex business logic
- Highlight missing concurrency, transaction, and failure-path tests

### 12. Build, Configuration, and Deployment Readiness
- Review profiles, environment separation, and secret handling
- Check dangerous defaults in `application.yml` / `application-*.yml`
- Identify production-risky local/dev assumptions
- Review Docker/Kubernetes friendliness if relevant
- Check whether graceful shutdown, readiness, and liveness concerns are addressed

### 13. Messaging and Event Flow
- Review producer/consumer responsibility boundaries
- Check idempotency of consumers
- Detect missing DLQ or retry strategy
- Review event schema consistency and versioning
- Check outbox usage and transaction/event publication consistency
- Highlight duplicate consumption and ordering risks

### 14. Redis Usage
- Review key design and naming consistency
- Check TTL strategy
- Detect cache stampede / penetration / stale data risks
- Review distributed lock correctness
- Check serialization strategy and compatibility risks

### 15. Authentication and Authorization
- Review token validation flow
- Check filter ordering and bypass possibilities
- Review role/group/permission mapping consistency
- Identify authorization logic hidden in controllers or services
- Detect missing access control on sensitive endpoints
---

## Audit Instructions

When auditing:

- Be specific and evidence-based
- Prefer concrete examples from the codebase
- Reference exact files, classes, and methods when possible
- Prioritize findings by severity: Critical, High, Medium, Low
- Focus on practical production risk, not just style preferences
- Do not suggest overengineering unless justified
- Distinguish clearly between:
    - confirmed issue
    - likely issue
    - possible improvement
- If something is uncertain, explicitly mark it as uncertain

---

## Output Format

Structure the audit result as follows:

### 1. Executive Summary
- Brief summary of overall code health
- Top risks
- Most urgent fixes

### 2. Findings by Severity
For each finding, include:
- Severity
- Category
- Location
- Problem
- Why it matters
- Recommended fix

### 3. Architectural Observations
- Overall structural strengths
- Structural weaknesses
- Refactoring priorities

### 4. Production Risk Review
- Risks most likely to cause incidents in production
- Risks most likely to cause data inconsistency
- Risks most likely to affect scale/performance

### 5. Recommended Next Actions
- Immediate fixes
- Short-term refactoring
- Long-term improvements

---

## Preferred Review Style

Use the mindset of a senior backend engineer reviewing a production Spring Boot service.

Prioritize:
- correctness
- maintainability
- operational safety
- realistic production constraints

Do not focus only on formatting or superficial style issues.
Focus on issues that materially affect quality, safety, or scalability.