# Employee Management API — Learning Guide

This project is a small Spring Boot REST API built so you can **read real code** and connect it to interview questions. Do not memorize this file first. Use it as a map: read a section, then open the class it names, then trace one HTTP request.

Stack: **Java 21**, **Spring Boot 4.1.2**, **Maven**, **Spring Web MVC**, **Spring Data JPA**, **Hibernate**, **H2**, **Jakarta Bean Validation**, **JUnit 5**, **Mockito**.

---

## 1. Project architecture

The app uses a classic **layered architecture**:

```
Client (browser, Postman, curl)
        |
        v
EmployeeController     HTTP only: URLs, status codes, JSON
        |
        v
EmployeeService        Business rules: uniqueness, not-found, mapping
        |
        v
EmployeeRepository     Data access: no SQL written by us for CRUD
        |
        v
Hibernate / JPA        Turns entities into SQL
        |
        v
H2 in-memory database
```

Rules that interviewers care about:

1. **Controller does not call the repository.**
2. **Service does not know about HTTP** (`ResponseEntity`, `@GetMapping`, status codes).
3. **Repository does not contain business rules.**
4. **Entities are not returned from controllers.** DTOs are.

Why layers? Each layer can be tested and replaced independently. You can mock the service when testing the controller. You can mock the repository when testing the service. That is the whole point.

---

## 2. Package-by-package explanation

Base package: `com.example.employee`. `@SpringBootApplication` component-scans this package and every sub-package.

| Package | What lives here | Problem it solves |
| --- | --- | --- |
| *(root)* | `EmployeeManagementApplication` | Starts the Spring context and embedded Tomcat. |
| `controller` | `EmployeeController` | Maps HTTP to service calls. |
| `service` | `EmployeeService`, `EmployeeServiceImpl` | Business logic and transaction boundaries. |
| `repository` | `EmployeeRepository` | Database access via Spring Data JPA. |
| `entity` | `Employee` | Table mapping (`employees`). |
| `dto` | Request/response objects, mapper, error/page wrappers | API contract separate from the database model. |
| `exception` | Custom exceptions + `GlobalExceptionHandler` | Consistent HTTP errors. |
| `config` | `EmployeeDataLoader` | Optional sample data at startup. |

How to study a package: pick one incoming request, for example `GET /api/employees/1`, and follow it through **controller → service → repository → entity → DTO**.

---

## 3. Important Spring annotations used in this project

### Bootstrap and configuration

| Annotation | Purpose |
| --- | --- |
| `@SpringBootApplication` | Combines `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`. |
| `@Component` | Generic Spring bean. Used on `EmployeeMapper` and `EmployeeDataLoader`. |
| `@Value("${property:default}")` | Injects a property from `application.properties`. |

### Web layer

| Annotation | Purpose |
| --- | --- |
| `@RestController` | Controller whose return values are the HTTP body (JSON). |
| `@RequestMapping` | Shared URL prefix (`/api/employees`). |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | HTTP method + path. |
| `@RequestBody` | JSON body → Java object (Jackson). |
| `@PathVariable` | `{id}` segment in the URL. |
| `@RequestParam` | Query string, e.g. `?name=ada`. |
| `@Valid` | Triggers Bean Validation on the request body. |
| `@PageableDefault` | Default `page`, `size`, and `sort` when the client omits them. |

### Business and persistence

| Annotation | Purpose |
| --- | --- |
| `@Service` | Business-layer bean. Also a `@Component` with a clearer meaning. |
| `@Transactional` | Each public method runs in a database transaction. |
| `@Entity` / `@Table` / `@Id` / `@GeneratedValue` / `@Column` | JPA mapping. |
| `@PrePersist` / `@PreUpdate` | JPA lifecycle callbacks for timestamps. |
| `@Query` / `@Param` | Custom JPQL on the repository. |

### Exception handling

| Annotation | Purpose |
| --- | --- |
| `@RestControllerAdvice` | Global interceptor for exceptions thrown from controllers. |
| `@ExceptionHandler` | Maps one exception type to an HTTP response. |

### Validation (Jakarta)

| Annotation | Purpose |
| --- | --- |
| `@NotBlank` | Not null, not empty, not only whitespace. |
| `@NotNull` | Not null (use for non-String types like `BigDecimal`). |
| `@Email` | Basic email format. |
| `@PositiveOrZero` | `>= 0`. |
| `@Size` | Length limits that match the database columns. |

### Testing

| Annotation | Purpose |
| --- | --- |
| `@SpringBootTest` | Full application context (slow, catches wiring errors). |
| `@WebMvcTest` | Slice: MVC + one controller. Service is mocked. |
| `@MockitoBean` | Spring Boot 4 replacement for `@MockBean`. Replaces a bean in the test context. |
| `@ExtendWith(MockitoExtension.class)` | JUnit 5 + Mockito without starting Spring. |
| `@Mock` | Mockito mock field. |
| `@Test` / `@DisplayName` / `@BeforeEach` | JUnit 5. |

---

## 4. Dependency Injection and Inversion of Control (IoC)

**Without Spring**, `EmployeeController` would do `new EmployeeServiceImpl(new EmployeeRepositoryImpl(), new EmployeeMapper())`. The controller would own its dependencies. That is hard to test and hard to change.

**Inversion of Control** means Spring owns object creation. You declare *what* you need; Spring decides *when* and *how* to provide it.

**Dependency Injection** is the mechanism: Spring passes collaborators in.

This project uses **constructor injection**:

```java
public EmployeeController(EmployeeService employeeService) {
    this.employeeService = employeeService;
}
```

Interview points:

- A class with **one constructor** does not need `@Autowired`. Spring uses that constructor automatically.
- Dependencies are `final`. They cannot be overwritten after construction.
- **Field injection** (`@Autowired` on a field) is avoided: it hides dependencies, breaks `final`, and needs reflection in tests.
- The controller depends on `EmployeeService` (the interface), not `EmployeeServiceImpl`. Spring injects the only implementing bean.

Spring's **ApplicationContext** is the IoC container. At startup it:

1. Scans for `@Component` / `@Service` / `@Repository` / `@RestController`.
2. Creates beans (usually singletons).
3. Injects constructors.
4. Calls lifecycle callbacks.
5. Starts Tomcat.

---

## 5. Spring bean lifecycle (basics)

Typical singleton bean lifecycle:

1. **Instantiation** — Spring calls the constructor.
2. **Populate properties** — remaining injection (`@Value`, setters if any).
3. **Aware interfaces** — e.g. `ApplicationContextAware` (we do not use these).
4. **BeanPostProcessor (before init)** — proxies may be created here (`@Transactional` uses a proxy).
5. **`@PostConstruct` / `InitializingBean`** — custom init.
6. **Bean is ready** — other beans can use it.
7. **On shutdown** — `@PreDestroy`, then destruction.

Interview gold:

- Default scope is **singleton**: one instance per Spring context.
- `@Transactional` on `EmployeeServiceImpl` means callers (the controller) receive a **JDK or CGLIB proxy**, not the raw class. The proxy starts/commits/rolls back the transaction, then delegates to the real method.
- `CommandLineRunner` beans (`EmployeeDataLoader`) run **after** the context is up.

You do not need to memorize every callback. You do need to know: **constructor → inject → init → in use → destroy**, and that **proxies** explain transactions and some "self-invocation" bugs.

---

## 6. How Spring Boot auto-configuration works

Spring Boot looks at the **classpath** and **your properties**, then creates beans you would otherwise write by hand.

Examples in this project:

| On the classpath | Boot auto-configures |
| --- | --- |
| `spring-boot-starter-webmvc` | DispatcherServlet, Tomcat, Jackson JSON, `HttpMessageConverters` |
| `spring-boot-starter-data-jpa` | `DataSource`, `EntityManagerFactory`, `TransactionManager`, repository proxies |
| `h2` + in-memory URL | Embedded H2 DataSource |
| `spring-boot-starter-validation` | `LocalValidatorFactoryBean` so `@Valid` works |
| `spring-boot-h2console` + `spring.h2.console.enabled=true` | H2 web console servlet |

`@EnableAutoConfiguration` (inside `@SpringBootApplication`) loads `AutoConfiguration.imports`. Each auto-config class is wrapped in `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, and similar.

Interview answer in one sentence:

> Auto-configuration is conditional bean registration based on what is on the classpath and what you already defined.

You can **override** any auto-configured bean by defining your own `@Bean` of the same type. Boot backs off because of `@ConditionalOnMissingBean`.

---

## 7. How an HTTP request travels from controller to database

Example: `PUT /api/employees/1` with a JSON body.

1. **Tomcat** receives the TCP request.
2. **DispatcherServlet** (Spring MVC front controller) takes over.
3. **HandlerMapping** finds `EmployeeController.updateEmployee` because of `@PutMapping("/{id}")`.
4. **HttpMessageConverter** (Jackson 3 `JsonMapper`) reads JSON into `EmployeeRequest`.
5. **`@Valid`** runs Jakarta validation. Failure → `MethodArgumentNotValidException` → `GlobalExceptionHandler` → **400**.
6. `{id}` is converted to `Long`. `"abc"` → `MethodArgumentTypeMismatchException` → **400**.
7. Controller calls `employeeService.updateEmployee(1, request)`.
8. The **transaction proxy** starts a transaction.
9. Service loads the entity with `findById`. Empty → `EmployeeNotFoundException` → **404**.
10. Service checks email uniqueness. Taken → `DuplicateEmailException` → **409**.
11. Mapper copies DTO fields onto the entity.
12. `repository.save(employee)` → Hibernate dirty-checks the persistent entity.
13. `@PreUpdate` sets `updatedAt`.
14. Transaction commits → **UPDATE** SQL is sent to H2.
15. Mapper builds `EmployeeResponse`.
16. Controller wraps it in `ResponseEntity.ok(...)`.
17. Jackson writes JSON. Client sees **200**.

Draw this on paper before the interview. Interviewers love "walk me through a request".

---

## 8. How Spring Data JPA works

`EmployeeRepository` is an **interface**. There is no class that implements it in `src/main/java`.

At startup, Spring Data:

1. Sees `extends JpaRepository<Employee, Long>`.
2. Creates a **JDK proxy**.
3. Implements `save`, `findById`, `findAll(Pageable)`, `delete`, `count`, etc.
4. Parses extra method names (`findByDepartmentIgnoreCase`) into a query.
5. Uses `@Query` JPQL as written for `searchByName`.

`JpaRepository` vs `CrudRepository` vs `PagingAndSortingRepository`:

- `CrudRepository` — basic CRUD.
- `PagingAndSortingRepository` — adds `findAll(Pageable)`.
- `JpaRepository` — both, plus JPA helpers (`flush`, `saveAndFlush`, `deleteInBatch`). We extend this one.

`findById` returns `Optional<Employee>`. Never call `get()` without checking. This project uses `orElseThrow`.

---

## 9. How Hibernate interacts with H2

- **JPA** is the spec (`jakarta.persistence.*`).
- **Hibernate** is the implementation Spring Boot uses.
- **H2** is the actual database process running in the JVM.

Flow:

1. `spring.datasource.url=jdbc:h2:mem:employeedb` → Boot creates a DataSource.
2. Hibernate reads `@Entity` classes and, with `ddl-auto=update`, creates/updates the `employees` table.
3. `EntityManager` is the JPA interface; Hibernate's `Session` is the implementation.
4. `save()` on a new entity → INSERT. `save()` on a loaded entity → UPDATE if dirty.
5. SQL is logged because `spring.jpa.show-sql=true`.

`open-in-view=false` is set on purpose. The default (`true`) keeps the Hibernate session open during JSON serialization, which hides `LazyInitializationException` and is considered a bad default. Our entity has no lazy collections, so we do not need Open Session in View.

H2 console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

- JDBC URL: `jdbc:h2:mem:employeedb`
- User: `sa`
- Password: empty

---

## 10. DTO vs Entity

| | Entity (`Employee`) | DTO (`EmployeeRequest` / `EmployeeResponse`) |
| --- | --- | --- |
| Purpose | Persistence | API contract |
| Annotations | `@Entity`, `@Column` | `@NotBlank`, `@Email` |
| Id / timestamps | Yes | Request: no. Response: yes (read-only) |
| Exposed to clients | No | Yes |
| Jackson serializes this | Should not | Yes |

Why not return the entity?

- You might add a `password` column later and accidentally leak it.
- Entity graphs (lazy relations) break during JSON serialization.
- API versioning is easier when the JSON shape is a DTO.

`EmployeeMapper` is the translator. No MapStruct so you can see every assignment.

---

## 11. `@RestController` vs `@Controller`

| | `@Controller` | `@RestController` |
| --- | --- | --- |
| Typical return | View name (`"employees"`) | Data (`EmployeeResponse`) |
| Body written? | Only if you add `@ResponseBody` | Always (`@ResponseBody` is included) |
| Used with | Thymeleaf / JSP | REST APIs |

This project is a REST API, so every controller is `@RestController`.

`@RestController` = `@Controller` + `@ResponseBody`.

---

## 12. `@Service` vs `@Repository`

Both are specialized `@Component` annotations. Component scan picks them up the same way.

| | `@Service` | `@Repository` |
| --- | --- | --- |
| Layer | Business | Persistence |
| Extra behavior | Documentation + AOP pointcut | Spring translates persistence exceptions into `DataAccessException` |

Our repository interface does **not** need `@Repository`. Spring Data adds it on the generated proxy.

Stereotype annotations exist so humans (and tools) can see the layer. Use the most specific one.

---

## 13. `@RequestBody` vs `@PathVariable` vs `@RequestParam`

Using `PUT /api/employees/1?notify=true` with a JSON body as an example:

- `@PathVariable Long id` → `1` (part of the URL path).
- `@RequestParam boolean notify` → `true` (query string).
- `@RequestBody EmployeeRequest request` → JSON body.

Memory trick:

- **Path** = resource identity (`/employees/1`).
- **Param** = optional filters (`?name=ada&page=0`).
- **Body** = the document you are creating or replacing.

POST/PUT use `@RequestBody`. GET/DELETE usually do not have a body.

---

## 14. `ResponseEntity` and HTTP status codes

`ResponseEntity<T>` is a body **plus** status **plus** headers.

| Operation | Status | Why |
| --- | --- | --- |
| Create | **201 Created** | New resource. `Location` header points to `/api/employees/{id}`. |
| Get / list / search / update | **200 OK** | Success with a body. |
| Delete | **204 No Content** | Success, no body. |
| Validation / bad id / missing param | **400 Bad Request** | Client sent something unusable. |
| Not found | **404 Not Found** | Id does not exist. |
| Duplicate email | **409 Conflict** | Request is valid but clashes with current data. |
| Unhandled bug | **500 Internal Server Error** | Our fault, not the client's. |

Interview trap: returning **200** for create, or **200** with an empty body for delete. Prefer 201 and 204.

---

## 15. Exception handling

`GlobalExceptionHandler` is annotated with `@RestControllerAdvice`. It is applied to every `@RestController`.

Matching is **most specific type wins**:

1. `EmployeeNotFoundException` → 404
2. `DuplicateEmailException` → 409
3. `MethodArgumentNotValidException` → 400 + `fieldErrors`
4. `HttpMessageNotReadableException` → 400 (bad JSON)
5. `Exception` → 500 (last resort)

The service **throws**. The handler **converts**. Controllers stay free of try/catch.

`EmployeeNotFoundException` extends `RuntimeException` (unchecked) so method signatures stay clean and `@Transactional` rolls back by default.

---

## 16. Bean Validation

Annotations live on `EmployeeRequest`. They do nothing until the controller parameter is annotated with `@Valid`.

Flow:

1. Jackson builds `EmployeeRequest`.
2. `LocalValidatorFactoryBean` validates it.
3. Failures become `FieldError`s.
4. Spring throws `MethodArgumentNotValidException`.
5. Our handler returns 400 with a map of field → message.

`@NotBlank` is for `String`. `@NotNull` is for objects like `BigDecimal` (an empty JSON field becomes `null`, not `""`).

This is **Jakarta** validation (`jakarta.validation.constraints`), not the old `javax` package.

---

## 17. Pagination and sorting

`GET /api/employees?page=0&size=5&sort=lastName,asc`

Spring Data web support binds those query params to a `Pageable` argument. `@PageableDefault(size = 10, sort = "id")` fills in missing values.

`JpaRepository.findAll(pageable)` runs:

- a data query with `LIMIT` / `OFFSET` (or equivalent)
- a count query for `totalElements`

We wrap `Page` in `PagedResponse` so the JSON is stable and small:

- `content` — the rows
- `page`, `size`, `totalElements`, `totalPages`
- `first`, `last`

Pages are **0-based**. `page=0` is the first page. This surprises frontend developers; mention it in interviews.

---

## 18. Unit testing vs integration testing

| | Unit (`EmployeeServiceTest`) | Controller slice (`EmployeeControllerTest`) | Smoke (`EmployeeManagementApplicationTests`) |
| --- | --- | --- | --- |
| Starts Spring? | No | Partial (MVC only) | Full context |
| Database? | No | No | H2 yes |
| What is mocked? | Repository | Service (`@MockitoBean`) | Nothing |
| Speed | Fastest | Fast | Slowest |
| Catches | Business rules | HTTP, validation, JSON, status | Wiring / auto-config mistakes |

"Controller integration test" in this project means `@WebMvcTest`: the web layer is real, collaborators are mocks. A **full** integration test would be `@SpringBootTest` + `@AutoConfigureMockMvc` (or TestRestTemplate) hitting a real H2. We kept that to a context-load smoke test so the suite stays focused.

---

## 19. JUnit 5 and Mockito basics

**JUnit 5** (`org.junit.jupiter.api`):

- `@Test` marks a test method.
- `@BeforeEach` runs before each test (fresh service instance).
- `@DisplayName` is what you read in the report.
- `assertThat` comes from AssertJ (included in Spring Boot test starters).

**Mockito**:

- `@Mock` creates a fake `EmployeeRepository`.
- `when(...).thenReturn(...)` stubs a call.
- `when(...).thenThrow(...)` stubs an exception.
- `verify(...)` asserts a method was called (or `never()`).
- `ArgumentCaptor` inspects the entity passed to `save`.

Service tests use `@ExtendWith(MockitoExtension.class)` — **no Spring**.

Controller tests use `@MockitoBean` — Spring replaces the `EmployeeService` bean in a tiny test context.

Spring Boot 4 / Spring Framework 7: **`@MockBean` is gone. Use `@MockitoBean`.**

---

## 20. How to run the application

```bash
./mvnw spring-boot:run
```

Useful URLs:

- API base: `http://localhost:8080/api/employees`
- H2 console: `http://localhost:8080/h2-console`

Disable sample data:

```properties
app.seed-data.enabled=false
```

Or:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--app.seed-data.enabled=false
```

Sample calls:

```bash
curl http://localhost:8080/api/employees
curl http://localhost:8080/api/employees/1
curl "http://localhost:8080/api/employees?page=0&size=3&sort=salary,desc"
curl http://localhost:8080/api/employees/department/Engineering
curl "http://localhost:8080/api/employees/search?name=ada"

curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Edsger","lastName":"Dijkstra","email":"edsger.dijkstra@example.com","department":"Research","salary":145000}'

curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada.lovelace@example.com","department":"Research","salary":125000}'

curl -X DELETE http://localhost:8080/api/employees/8
```

---

## 21. Common Spring Boot interview questions (with answers)

**Q: What is Spring Boot, and how is it different from Spring Framework?**  
A: Spring Framework is the core (IoC, MVC, Transactions). Spring Boot is opinionated auto-configuration + starters + an embedded server so you can run a JAR with `main()`. This project is Boot; under it you are still using Spring MVC and Spring Data.

**Q: What does `@SpringBootApplication` do?**  
A: It is `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.

**Q: Explain IoC and DI.**  
A: IoC: the container creates and wires objects. DI: the container injects collaborators, preferably via constructors.

**Q: Singleton vs prototype?**  
A: Singleton (default): one bean per context. Prototype: a new instance every time the bean is requested. Controllers and services are singletons — they must be stateless (no request fields).

**Q: Why constructor injection?**  
A: Required dependencies are obvious, fields can be `final`, tests can `new` the class, and missing beans fail at startup rather than NPE later.

**Q: `@RestController` vs `@Controller`?**  
A: See section 11. REST vs HTML views.

**Q: `@RequestBody` vs `@PathVariable` vs `@RequestParam`?**  
A: See section 13.

**Q: How does `@Transactional` work?**  
A: A proxy opens a transaction, calls the method, commits on success, rolls back on runtime exceptions. Self-invocation (`this.method()`) bypasses the proxy — a classic trick question.

**Q: What is the difference between `getById` / `getReferenceById` and `findById`?**  
A: `findById` hits the DB and returns `Optional`. `getReferenceById` may return a lazy proxy without a SELECT; accessing it after the session closes can fail. This project uses `findById`.

**Q: Dirty checking?**  
A: Hibernate tracks loaded entities. If you change a field inside a transaction, it issues UPDATE on flush/commit even without calling `save`. We still call `save` because it is explicit and easier to follow.

**Q: `save` vs `saveAndFlush`?**  
A: `save` schedules SQL; flush happens at commit. `@PreUpdate` therefore would run *after* we mapped `EmployeeResponse`, so `updatedAt` would look stale in the JSON. This project uses `saveAndFlush` so timestamps are populated before the response is built. `IDENTITY` inserts already flush to fetch the generated id.

**Q: How do you handle exceptions in REST APIs?**  
A: `@RestControllerAdvice` + `@ExceptionHandler`. Do not scatter try/catch in controllers.

**Q: Bean Validation vs database constraints?**  
A: Validation is a fast fail for HTTP 400. Database unique constraints are the last line of defense. This project does both: `@Email`/`@NotBlank` and a unique column + service-level duplicate check for 409.

**Q: Why `BigDecimal` for salary?**  
A: `double` cannot represent money exactly (`0.1 + 0.2`). `BigDecimal` with `precision`/`scale` maps to `DECIMAL`.

**Q: What is Open Session in View?**  
A: Holding the Hibernate session open until the view/JSON is rendered. Convenient, hides lazy-load bugs, holds DB connections longer. We disable it.

**Q: How does pagination work?**  
A: `Pageable` bound from query params; repository returns `Page`; we map to `PagedResponse`. Two SQL statements: data + count.

**Q: Unit test vs integration test?**  
A: See section 18.

**Q: How do you mock a Spring bean in a `@WebMvcTest` on Boot 4?**  
A: `@MockitoBean`. Not `@MockBean` (removed).

**Q: What is a Spring Data derived query?**  
A: A method name Spring parses, e.g. `findByDepartmentIgnoreCase`. No implementation, no SQL from you.

**Q: JPQL vs native SQL?**  
A: JPQL uses entity names/fields (`SELECT e FROM Employee e`). Native SQL uses tables/columns and is database-specific. We used JPQL for name search.

**Q: PUT vs PATCH?**  
A: PUT replaces the whole resource (our `EmployeeRequest` has every updatable field). PATCH would apply a partial update. Interviewers often ask this even if you only implemented PUT.

**Q: Idempotency?**  
A: GET, PUT, DELETE should be safe to retry. POST create is not (a retry can create two rows unless you have idempotency keys). Duplicate email protection makes a retried POST return 409 instead of a second row.

**Q: What happens if two layers need the same logic?**  
A: Put it in the service. That is why the controller is thin.

**Q: How does Spring Boot 4 change testing packages?**  
A: `@WebMvcTest` moved to `org.springframework.boot.webmvc.test.autoconfigure`. Starters split: `spring-boot-starter-webmvc` and `spring-boot-starter-webmvc-test`. Jackson 3 is the default JSON library (`JsonMapper`).

**Q: Component scan default?**  
A: The package of the `@SpringBootApplication` class and below. If you put a controller in a *different* root package, it will not be found unless you customize `@ComponentScan`.

---

## 22. Suggested reading order inside the code

Do not start at the entity. Start at the HTTP boundary, because that is how users (and interviewers) think.

1. `EmployeeController`
2. `EmployeeRequest` + `EmployeeResponse`
3. `EmployeeService` / `EmployeeServiceImpl`
4. `EmployeeMapper`
5. `Employee` entity (`@PrePersist`)
6. `EmployeeRepository`
7. `GlobalExceptionHandler`
8. `EmployeeDataLoader`
9. `application.properties`
10. `EmployeeServiceTest` then `EmployeeControllerTest`

Pair this with `DAY_BY_DAY_LEARNING_PLAN.md`.
