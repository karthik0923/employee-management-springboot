# 7-Day Spring Boot Learning Plan

Goal: be able to **draw the request path**, **explain every annotation in this project**, and **write a similar CRUD API without AI** in an interview.

Rules for the week:

- Read code before articles.
- After each day, close the IDE and explain the topic out loud for 5 minutes.
- Tasks marked **without AI** mean you type them yourself. Struggle is the point.
- Do not skip Day 1 even if you already "know Java".

---

## Day 1 — Spring Boot mental model and project layout

### Topics to study

- What `@SpringBootApplication` expands to
- Component scanning from `com.example.employee`
- Maven starters in `pom.xml` (why `webmvc`, `data-jpa`, `validation`, `h2console`)
- `application.properties` as configuration, not code
- Embedded Tomcat: there is no external server to install

### Classes and methods to inspect

- `EmployeeManagementApplication.main`
- `pom.xml` dependencies
- `src/main/resources/application.properties`
- Package folders under `src/main/java/com/example/employee`

### Request flow to trace

None yet. Instead, start the app and watch the logs.

```bash
./mvnw spring-boot:run
```

Find these lines in the log:

- Tomcat started on port 8080
- H2 console available
- Hibernate DDL / sample data loader

Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console) with JDBC URL `jdbc:h2:mem:employeedb`.

### Interview questions

1. What does `@SpringBootApplication` do?
2. What is a starter?
3. How does Boot know it should start Tomcat?
4. What is auto-configuration in one sentence?
5. Where does component scan start?

### Practical exercises

- Change `server.port` to `8081`, restart, confirm the new port.
- Set `app.seed-data.enabled=false`, restart, confirm the `employees` table is empty, then set it back.

### Without AI

Draw a box diagram: Main class → ApplicationContext → DispatcherServlet → Tomcat. Label which piece comes from Spring Framework vs Spring Boot.

---

## Day 2 — REST controller, HTTP, DTOs

### Topics to study

- `@RestController` vs `@Controller`
- `@RequestMapping` on class vs method
- `@RequestBody`, `@PathVariable`, `@RequestParam`
- `ResponseEntity` and status codes 200 / 201 / 204 / 400 / 404 / 409
- Why `EmployeeRequest` does not contain `id` or timestamps

### Classes and methods to inspect

- `EmployeeController` (every method)
- `dto/EmployeeRequest`
- `dto/EmployeeResponse`
- `dto/PagedResponse`

### Request flow to trace

`POST /api/employees`

1. JSON arrives.
2. Jackson creates `EmployeeRequest`.
3. `@Valid` runs.
4. `createEmployee` on the service is called.
5. `201` + `Location` header go back.

Call it with curl or Postman. Then call `GET /api/employees/{id}` using the Location header.

### Interview questions

1. Why not return the JPA entity from the controller?
2. Difference between PUT and POST?
3. Why 201 instead of 200 for create?
4. Why 204 for delete?
5. When do you use `@RequestParam` instead of `@PathVariable`?

### Practical exercises

- Send POST with a missing email and read the 400 JSON.
- Send GET `/api/employees/9999` and read the 404 JSON.
- Send GET `/api/employees?page=0&size=2&sort=salary,desc`.

### Without AI

On paper, write the seven endpoint signatures (HTTP method, URL, request type, response type, status codes) from memory. Then check `EmployeeController`.

---

## Day 3 — Service layer, DI, transactions

### Topics to study

- Constructor injection, `final` fields, no `@Autowired`
- Programming to the `EmployeeService` interface
- `@Service` as a stereotype
- `@Transactional` and `readOnly = true`
- Business rules: not-found, duplicate email
- Mapper as a translator, not a second service

### Classes and methods to inspect

- `EmployeeService`
- `EmployeeServiceImpl` — especially `createEmployee`, `updateEmployee`, `findEmployee`, `assertEmailAvailable`
- `EmployeeMapper`

### Request flow to trace

`PUT /api/employees/{id}`

Follow `updateEmployee`: load entity → email check excluding self → map fields → `save` → map response.

### Interview questions

1. Why constructor injection over field injection?
2. What happens if a `@Transactional` method calls another method on `this`?
3. Why does not-found throw an exception instead of returning `null`?
4. Why is the controller not allowed to call the repository?
5. What does Spring inject when the constructor type is an interface?

### Practical exercises

- Add a breakpoint in `createEmployee` and debug a POST.
- Log `employeeService.getClass()` in the controller constructor (temporarily) and see the CGLIB/JDK proxy name. Remove the log after.

### Without AI

Write (in a notebook) a `DepartmentService.createDepartment` method that: checks unique name, maps DTO → entity, saves, returns a response DTO. Do not look at `EmployeeServiceImpl` until you finish.

---

## Day 4 — JPA, Hibernate, H2, repository methods

### Topics to study

- `@Entity`, `@Id`, `@GeneratedValue(IDENTITY)`, `@Column`
- `@PrePersist` / `@PreUpdate` for timestamps
- `JpaRepository` methods you did not write
- Derived query: `findByDepartmentIgnoreCase`
- JPQL: `searchByName`
- `BigDecimal` + `precision`/`scale`
- Unique email column vs service-level check

### Classes and methods to inspect

- `entity/Employee`
- `repository/EmployeeRepository`
- `config/EmployeeDataLoader`
- SQL in the console after `GET /api/employees` (`spring.jpa.show-sql=true`)

### Request flow to trace

`GET /api/employees/department/Engineering`

Controller → `getEmployeesByDepartment` → `findByDepartmentIgnoreCase` → Hibernate SQL → list of entities → mapper → JSON array.

Watch the generated SQL in the application log.

### Interview questions

1. JPA vs Hibernate vs Spring Data JPA?
2. Why does JPA need a no-arg constructor?
3. `findById` vs `getReferenceById`?
4. What SQL does `findByDepartmentIgnoreCase` become?
5. Why is `ddl-auto=update` wrong for production?

### Practical exercises

- Open H2 console and run `SELECT * FROM EMPLOYEES;`.
- Add (then delete) a derived method `findByEmailIgnoreCase` and call it from a temporary test if you want — or just predict the SQL.

### Without AI

Write JPQL that finds employees with salary greater than a given amount, ordered by salary descending. Do not paste it into the project until you can explain every token (`SELECT`, `FROM Employee`, `WHERE`, `:minSalary`).

---

## Day 5 — Validation, exceptions, HTTP errors

### Topics to study

- Jakarta Bean Validation annotations on the DTO
- `@Valid` as the trigger
- `@RestControllerAdvice` + `@ExceptionHandler`
- Custom exceptions: `EmployeeNotFoundException`, `DuplicateEmailException`
- Error JSON shape: `ErrorResponse`
- 400 vs 404 vs 409 vs 500

### Classes and methods to inspect

- `EmployeeRequest` annotations
- `exception/GlobalExceptionHandler`
- Both custom exception classes
- `dto/ErrorResponse`

### Request flow to trace

Two flows:

1. Invalid POST body → `MethodArgumentNotValidException` → 400 + `fieldErrors`.
2. `GET /api/employees/99` when missing → service throws → 404.

### Interview questions

1. Do validation annotations run without `@Valid`?
2. Why `@NotBlank` for names and `@NotNull` for salary?
3. Checked vs unchecked exceptions in Spring services?
4. Why a global handler instead of try/catch in the controller?
5. When is 409 a better choice than 400?

### Practical exercises

- POST with salary `-1`, empty firstName, and `email: "x"`. Confirm three field errors.
- POST the same email twice. Confirm 409.
- POST malformed JSON (`{`). Confirm 400 "invalid JSON".

### Without AI

From memory, write a `GlobalExceptionHandler` method that maps a hypothetical `DepartmentNotFoundException` to 404 using `ErrorResponse`. Then compare with the real handler.

---

## Day 6 — Pagination, sorting, and testing

### Topics to study

- `Pageable` query parameters (`page`, `size`, `sort`)
- 0-based pages
- `Page.map` and `PagedResponse.from`
- Unit test: Mockito, no Spring (`EmployeeServiceTest`)
- Slice test: `@WebMvcTest` + `@MockitoBean` (`EmployeeControllerTest`)
- `@SpringBootTest` context load
- Spring Boot 4: `@MockitoBean`, new `@WebMvcTest` package

### Classes and methods to inspect

- `EmployeeController.getAllEmployees`
- `EmployeeServiceImpl.getAllEmployees`
- `EmployeeServiceTest` (all tests)
- `EmployeeControllerTest` (all tests)

### Request flow to trace

`GET /api/employees?page=0&size=3&sort=lastName,asc`

Then read `getAllEmployeesReturnsPage` in the controller test and see how `MockMvc` simulates that call without Tomcat.

### Interview questions

1. Unit test vs integration test vs slice test?
2. Why mock the repository in service tests but the service in controller tests?
3. What does `@WebMvcTest` not load?
4. Why was `@MockBean` replaced?
5. Why two SQL queries for a page (data + count)?

### Practical exercises

```bash
./mvnw test
```

Break a test on purpose (change an expected status from 201 to 200), run tests, read the failure, revert.

### Without AI

Write a new unit test: `updateEmployee` throws `EmployeeNotFoundException` when `findById` is empty. Do not look at existing tests until yours compiles. Then compare.

---

## Day 7 — Interview rehearsal and a small independent feature

### Topics to study

- Re-read `LEARNING_GUIDE.md` sections 4, 6, 7, 10, 18, 21
- Bean lifecycle + `@Transactional` proxy (say it out loud)
- Auto-configuration in your own words
- PUT vs PATCH, idempotency, DTO vs entity

### Classes and methods to inspect

Re-walk the full path for **one** endpoint of your choice, from controller method to H2 SQL, without notes.

### Request flow to trace

`DELETE /api/employees/{id}` including the 404 case.

### Interview questions (simulate a 30-minute round)

Answer each out loud, 60–90 seconds:

1. Walk me through `POST /api/employees`.
2. How is dependency injection used in this project?
3. How do you handle errors globally?
4. How does Spring Data implement `EmployeeRepository`?
5. How would you add pagination to the department search?
6. How would you switch H2 to PostgreSQL?
7. How do you test the controller without a database?
8. What would you add before production (validation of department names, Flyway, security)?

### Practical exercises — pick one **without AI**

Implement **one** of these on a branch, yourself:

1. `GET /api/employees/department/{department}` with pagination (`Pageable`).
2. A `PATCH /api/employees/{id}` that updates only non-null fields.
3. Reject salary greater than `1_000_000` with a validation annotation.
4. A repository method `findBySalaryGreaterThanEqual` plus a new endpoint.

Then write **one** service unit test and **one** controller test for it.

### End-of-week check

You are ready for the next lesson (security, Flyway, or PostgreSQL) only if you can do all five:

1. Draw Controller → Service → Repository → Hibernate → H2.
2. Explain constructor injection without saying "Spring magic".
3. Explain why DTOs exist.
4. Explain `@Valid` + `@RestControllerAdvice`.
5. Explain the difference between `EmployeeServiceTest` and `EmployeeControllerTest`.

If any item is fuzzy, repeat that day's "without AI" task before adding new topics.
