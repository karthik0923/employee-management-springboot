package com.example.employee.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.exception.DuplicateEmailException;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.service.EmployeeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller slice tests. Spring starts a narrow context: the MVC stack,
 * {@link EmployeeController}, and {@code @RestControllerAdvice}. The service
 * is replaced with {@code @MockitoBean} so these tests assert HTTP behavior,
 * not database behavior.
 *
 * Interview concept — unit vs this test:
 * {@code EmployeeServiceTest} never starts Spring. This class starts just
 * enough Spring to exercise routing, validation, JSON, and status codes.
 * That is why it is called a controller integration / slice test.
 *
 * Spring Boot 4 notes:
 * {@code @WebMvcTest} lives in {@code org.springframework.boot.webmvc.test.autoconfigure}.
 * {@code @MockBean} was removed; use {@code @MockitoBean} from Spring Framework 7.
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@Test
	@DisplayName("POST /api/employees returns 201 and a Location header")
	void createEmployeeReturnsCreated() throws Exception {
		when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(sampleResponse(1L));

		mockMvc.perform(post("/api/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validEmployeeJson("ada.lovelace@example.com")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/employees/1"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.email").value("ada.lovelace@example.com"));
	}

	@Test
	@DisplayName("POST /api/employees returns 400 when Bean Validation fails")
	void createEmployeeReturnsBadRequestForInvalidBody() throws Exception {
		mockMvc.perform(post("/api/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "firstName": "",
						  "lastName": "Lovelace",
						  "email": "not-an-email",
						  "department": "Engineering",
						  "salary": -10
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors.firstName").exists())
				.andExpect(jsonPath("$.fieldErrors.email").exists())
				.andExpect(jsonPath("$.fieldErrors.salary").exists());
	}

	@Test
	@DisplayName("POST /api/employees returns 409 when the email already exists")
	void createEmployeeReturnsConflictForDuplicateEmail() throws Exception {
		when(employeeService.createEmployee(any(EmployeeRequest.class)))
				.thenThrow(new DuplicateEmailException("ada.lovelace@example.com"));

		mockMvc.perform(post("/api/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validEmployeeJson("ada.lovelace@example.com")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	@DisplayName("GET /api/employees returns a paginated payload")
	void getAllEmployeesReturnsPage() throws Exception {
		PagedResponse<EmployeeResponse> page = new PagedResponse<>(List.of(sampleResponse(1L)), 0, 10, 1, 1, true, true);
		when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/employees").param("page", "0").param("size", "10").param("sort", "lastName,asc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].firstName").value("Ada"))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.page").value(0));
	}

	@Test
	@DisplayName("GET /api/employees/{id} returns 200 when found")
	void getEmployeeByIdReturnsOk() throws Exception {
		when(employeeService.getEmployeeById(1L)).thenReturn(sampleResponse(1L));

		mockMvc.perform(get("/api/employees/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.department").value("Engineering"));
	}

	@Test
	@DisplayName("GET /api/employees/{id} returns 404 when missing")
	void getEmployeeByIdReturnsNotFound() throws Exception {
		when(employeeService.getEmployeeById(99L)).thenThrow(new EmployeeNotFoundException(99L));

		mockMvc.perform(get("/api/employees/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
	}

	@Test
	@DisplayName("GET /api/employees/{id} returns 400 for a non-numeric id")
	void getEmployeeByIdReturnsBadRequestForInvalidId() throws Exception {
		mockMvc.perform(get("/api/employees/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@DisplayName("PUT /api/employees/{id} returns the updated employee")
	void updateEmployeeReturnsOk() throws Exception {
		EmployeeResponse updated = sampleResponse(1L);
		updated.setDepartment("Research");
		when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(updated);

		mockMvc.perform(put("/api/employees/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validEmployeeJson("ada.lovelace@example.com")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.department").value("Research"));
	}

	@Test
	@DisplayName("DELETE /api/employees/{id} returns 204")
	void deleteEmployeeReturnsNoContent() throws Exception {
		doNothing().when(employeeService).deleteEmployee(1L);

		mockMvc.perform(delete("/api/employees/1"))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("DELETE /api/employees/{id} returns 404 when missing")
	void deleteEmployeeReturnsNotFound() throws Exception {
		doThrow(new EmployeeNotFoundException(99L)).when(employeeService).deleteEmployee(99L);

		mockMvc.perform(delete("/api/employees/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/employees/department/{department} returns matches")
	void getEmployeesByDepartmentReturnsMatches() throws Exception {
		when(employeeService.getEmployeesByDepartment("Engineering")).thenReturn(List.of(sampleResponse(1L)));

		mockMvc.perform(get("/api/employees/department/Engineering"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].department").value("Engineering"));
	}

	@Test
	@DisplayName("GET /api/employees/search?name= returns matches")
	void searchEmployeesByNameReturnsMatches() throws Exception {
		when(employeeService.searchEmployeesByName("ada")).thenReturn(List.of(sampleResponse(1L)));

		mockMvc.perform(get("/api/employees/search").param("name", "ada"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].firstName").value("Ada"));
	}

	@Test
	@DisplayName("GET /api/employees/search without name returns 400")
	void searchEmployeesByNameRequiresName() throws Exception {
		mockMvc.perform(get("/api/employees/search"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	private static EmployeeResponse sampleResponse(Long id) {
		LocalDateTime now = LocalDateTime.of(2026, 10, 30, 10, 0);
		return new EmployeeResponse(id, "Ada", "Lovelace", "ada.lovelace@example.com", "Engineering",
				new BigDecimal("120000.00"), now, now);
	}

	private static String validEmployeeJson(String email) {
		return """
				{
				  "firstName": "Ada",
				  "lastName": "Lovelace",
				  "email": "%s",
				  "department": "Engineering",
				  "salary": 120000.00
				}
				""".formatted(email);
	}
}
