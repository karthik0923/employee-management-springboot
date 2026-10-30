package com.example.employee.controller;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.service.EmployeeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * HTTP adapter for employee use cases.
 *
 * Why this class exists:
 * It is the only layer that knows about HTTP: URLs, status codes, request
 * parameters, and JSON. It does not contain business rules and it does not
 * call the repository.
 *
 * Request travel path:
 * Client HTTP request -> DispatcherServlet -> this method -> EmployeeService
 * -> EmployeeRepository -> Hibernate -> H2. The return value travels back
 * the same way and Jackson writes JSON.
 *
 * Interview concept — {@code @RestController} vs {@code @Controller}:
 * {@code @RestController} already includes {@code @ResponseBody}, so method
 * return values become the response body (JSON). {@code @Controller} is for
 * server-side HTML (Thymeleaf) where you return a view name.
 *
 * {@code @RequestMapping} on the class sets the common prefix so methods
 * only declare the extra path.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	/**
	 * {@code @Valid} runs Bean Validation on the deserialized body.
	 * {@code 201 Created} plus a Location header is the REST convention for
	 * a successful POST that creates a resource.
	 */
	@PostMapping
	public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
		EmployeeResponse created = employeeService.createEmployee(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.getId())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	/**
	 * Pagination and sorting are bound automatically from query params:
	 * {@code ?page=0&size=5&sort=lastName,asc}
	 * {@code @PageableDefault} is used when the client omits those params.
	 */
	@GetMapping
	public ResponseEntity<PagedResponse<EmployeeResponse>> getAllEmployees(
			@PageableDefault(size = 10, sort = "id") Pageable pageable) {
		return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
	}

	/**
	 * {@code @PathVariable} binds the URI segment {@code {id}} to the method
	 * argument. A non-numeric id becomes 400 via the global handler.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
		return ResponseEntity.ok(employeeService.getEmployeeById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
			@Valid @RequestBody EmployeeRequest request) {
		return ResponseEntity.ok(employeeService.updateEmployee(id, request));
	}

	/**
	 * {@code 204 No Content} means success with an empty body — correct for
	 * DELETE when the client already knows the id it sent.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
		employeeService.deleteEmployee(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/department/{department}")
	public ResponseEntity<List<EmployeeResponse>> getEmployeesByDepartment(@PathVariable String department) {
		return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department));
	}

	/**
	 * {@code @RequestParam} binds {@code ?name=ada}. Missing name is 400.
	 */
	@GetMapping("/search")
	public ResponseEntity<List<EmployeeResponse>> searchEmployeesByName(@RequestParam String name) {
		return ResponseEntity.ok(employeeService.searchEmployeesByName(name));
	}
}
