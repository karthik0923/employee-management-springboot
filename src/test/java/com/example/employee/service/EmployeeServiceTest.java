package com.example.employee.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.employee.dto.EmployeeMapper;
import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.entity.Employee;
import com.example.employee.exception.DuplicateEmailException;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Unit tests for {@link EmployeeServiceImpl}.
 *
 * Why this is a unit test:
 * The Spring context is not started. The repository is a Mockito mock, so no
 * database is used. That makes these tests fast and focused on business rules:
 * mapping, not-found, duplicate email, pagination wrapper.
 *
 * Interview concept:
 * {@code @ExtendWith(MockitoExtension.class)} is the JUnit 5 way to initialize
 * {@code @Mock} fields. {@code @InjectMocks} is avoided so the real mapper
 * can be passed through the constructor — mapper has no collaborators.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

	@Mock
	private EmployeeRepository employeeRepository;

	private EmployeeService employeeService;

	@BeforeEach
	void setUp() {
		employeeService = new EmployeeServiceImpl(employeeRepository, new EmployeeMapper());
	}

	@Test
	@DisplayName("createEmployee saves a new employee when the email is free")
	void createEmployeeSavesWhenEmailIsAvailable() {
		EmployeeRequest request = sampleRequest();
		when(employeeRepository.existsByEmailIgnoreCase("ada.lovelace@example.com")).thenReturn(false);
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0), 1L));

		EmployeeResponse response = employeeService.createEmployee(request);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getEmail()).isEqualTo("ada.lovelace@example.com");
		assertThat(response.getFirstName()).isEqualTo("Ada");
		assertThat(response.getCreatedAt()).isNotNull();

		ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
		verify(employeeRepository).save(captor.capture());
		assertThat(captor.getValue().getEmail()).isEqualTo("ada.lovelace@example.com");
	}

	@Test
	@DisplayName("createEmployee rejects a duplicate email")
	void createEmployeeRejectsDuplicateEmail() {
		when(employeeRepository.existsByEmailIgnoreCase("ada.lovelace@example.com")).thenReturn(true);

		assertThatThrownBy(() -> employeeService.createEmployee(sampleRequest()))
				.isInstanceOf(DuplicateEmailException.class)
				.hasMessageContaining("ada.lovelace@example.com");

		verify(employeeRepository, never()).save(any());
	}

	@Test
	@DisplayName("getEmployeeById returns a mapped DTO")
	void getEmployeeByIdReturnsMappedDto() {
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(persistedEmployee(1L)));

		EmployeeResponse response = employeeService.getEmployeeById(1L);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getDepartment()).isEqualTo("Engineering");
	}

	@Test
	@DisplayName("getEmployeeById throws when the id is unknown")
	void getEmployeeByIdThrowsWhenMissing() {
		when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
				.isInstanceOf(EmployeeNotFoundException.class)
				.hasMessageContaining("99");
	}

	@Test
	@DisplayName("getAllEmployees wraps a Spring Data page")
	void getAllEmployeesWrapsPage() {
		Pageable pageable = PageRequest.of(0, 5, Sort.by("id"));
		when(employeeRepository.findAll(pageable))
				.thenReturn(new PageImpl<>(List.of(persistedEmployee(1L)), pageable, 1));

		PagedResponse<EmployeeResponse> response = employeeService.getAllEmployees(pageable);

		assertThat(response.getContent()).hasSize(1);
		assertThat(response.getPage()).isZero();
		assertThat(response.getSize()).isEqualTo(5);
		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.isFirst()).isTrue();
		assertThat(response.isLast()).isTrue();
	}

	@Test
	@DisplayName("updateEmployee changes fields and keeps the same id")
	void updateEmployeeChangesFields() {
		Employee existing = persistedEmployee(1L);
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("ada.lovelace@example.com", 1L)).thenReturn(false);
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeRequest request = new EmployeeRequest("Ada", "Byron", "ada.lovelace@example.com", "Research",
				new BigDecimal("150000.00"));
		EmployeeResponse response = employeeService.updateEmployee(1L, request);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getLastName()).isEqualTo("Byron");
		assertThat(response.getDepartment()).isEqualTo("Research");
		assertThat(response.getSalary()).isEqualByComparingTo("150000.00");
	}

	@Test
	@DisplayName("updateEmployee rejects an email owned by another employee")
	void updateEmployeeRejectsDuplicateEmail() {
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(persistedEmployee(1L)));
		when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("grace.hopper@example.com", 1L)).thenReturn(true);

		EmployeeRequest request = new EmployeeRequest("Ada", "Lovelace", "grace.hopper@example.com", "Engineering",
				new BigDecimal("120000.00"));

		assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
				.isInstanceOf(DuplicateEmailException.class);
		verify(employeeRepository, never()).save(any());
	}

	@Test
	@DisplayName("deleteEmployee removes an existing employee")
	void deleteEmployeeRemovesExisting() {
		Employee existing = persistedEmployee(1L);
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));

		employeeService.deleteEmployee(1L);

		verify(employeeRepository).delete(existing);
	}

	@Test
	@DisplayName("deleteEmployee throws when the id is unknown")
	void deleteEmployeeThrowsWhenMissing() {
		when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> employeeService.deleteEmployee(42L))
				.isInstanceOf(EmployeeNotFoundException.class);
		verify(employeeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("getEmployeesByDepartment delegates to a derived query")
	void getEmployeesByDepartmentFiltersCaseInsensitively() {
		when(employeeRepository.findByDepartmentIgnoreCase("Engineering"))
				.thenReturn(List.of(persistedEmployee(1L)));

		List<EmployeeResponse> results = employeeService.getEmployeesByDepartment("Engineering");

		assertThat(results).extracting(EmployeeResponse::getEmail).containsExactly("ada.lovelace@example.com");
	}

	@Test
	@DisplayName("searchEmployeesByName trims the query and maps results")
	void searchEmployeesByNameTrimsQuery() {
		when(employeeRepository.searchByName("ada")).thenReturn(List.of(persistedEmployee(1L)));

		List<EmployeeResponse> results = employeeService.searchEmployeesByName("  ada  ");

		assertThat(results).hasSize(1);
		verify(employeeRepository).searchByName("ada");
	}

	private static EmployeeRequest sampleRequest() {
		return new EmployeeRequest("Ada", "Lovelace", "ada.lovelace@example.com", "Engineering",
				new BigDecimal("120000.00"));
	}

	private static Employee persistedEmployee(Long id) {
		Employee employee = new Employee("Ada", "Lovelace", "ada.lovelace@example.com", "Engineering",
				new BigDecimal("120000.00"));
		return persisted(employee, id);
	}

	private static Employee persisted(Employee employee, Long id) {
		employee.setId(id);
		LocalDateTime now = LocalDateTime.of(2026, 10, 30, 10, 0);
		employee.setCreatedAt(now);
		employee.setUpdatedAt(now);
		return employee;
	}
}
