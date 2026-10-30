package com.example.employee.service;

import com.example.employee.dto.EmployeeMapper;
import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.entity.Employee;
import com.example.employee.exception.DuplicateEmailException;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.repository.EmployeeRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application / business layer. This is where rules live, not HTTP and not SQL.
 *
 * Why this class exists:
 * Controllers should be thin (status codes + routing). Repositories should
 * only load and save. Duplicate-email checks, "not found" handling, and
 * mapping happen here so they can be reused by any future caller (another
 * controller, a scheduled job, etc.).
 *
 * Execution flow for create:
 * 1. Check email uniqueness via the repository.
 * 2. Map DTO -> entity (id/timestamps still empty).
 * 3. {@code save} triggers INSERT; {@code @PrePersist} fills timestamps;
 *    Hibernate fills the generated id.
 * 4. Map entity -> response DTO and return.
 *
 * {@code @Service} makes this a Spring bean discovered by component scan.
 * {@code @Transactional} starts a transaction around each public method.
 * Read methods override it with {@code readOnly = true} so Hibernate can skip
 * dirty checking.
 *
 * Interview concept — constructor injection:
 * One constructor, both dependencies {@code final}. Spring calls this
 * constructor; {@code @Autowired} is unnecessary. Field injection is avoided
 * because it hides dependencies and makes tests harder.
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final EmployeeMapper employeeMapper;

	public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
		this.employeeRepository = employeeRepository;
		this.employeeMapper = employeeMapper;
	}

	@Override
	public EmployeeResponse createEmployee(EmployeeRequest request) {
		assertEmailAvailable(request.getEmail(), null);
		Employee employee = employeeMapper.toEntity(request);
		// IDENTITY insert flushes immediately so the generated id and @PrePersist
		// timestamps are available before we map the response.
		Employee saved = employeeRepository.saveAndFlush(employee);
		return employeeMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<EmployeeResponse> getAllEmployees(Pageable pageable) {
		Page<EmployeeResponse> page = employeeRepository.findAll(pageable).map(employeeMapper::toResponse);
		return PagedResponse.from(page);
	}

	@Override
	@Transactional(readOnly = true)
	public EmployeeResponse getEmployeeById(Long id) {
		return employeeMapper.toResponse(findEmployee(id));
	}

	@Override
	public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
		Employee employee = findEmployee(id);
		assertEmailAvailable(request.getEmail(), id);
		employeeMapper.updateEntity(employee, request);
		// save() only schedules the UPDATE. @PreUpdate runs at flush, which by
		// default is at transaction commit — after this method already mapped
		// the response. saveAndFlush forces Hibernate to run @PreUpdate now so
		// updatedAt in the JSON is current. Interview topic: flush timing.
		Employee saved = employeeRepository.saveAndFlush(employee);
		return employeeMapper.toResponse(saved);
	}

	@Override
	public void deleteEmployee(Long id) {
		Employee employee = findEmployee(id);
		employeeRepository.delete(employee);
	}

	@Override
	@Transactional(readOnly = true)
	public List<EmployeeResponse> getEmployeesByDepartment(String department) {
		return employeeRepository.findByDepartmentIgnoreCase(department).stream()
				.map(employeeMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<EmployeeResponse> searchEmployeesByName(String name) {
		return employeeRepository.searchByName(name.trim()).stream()
				.map(employeeMapper::toResponse)
				.toList();
	}

	private Employee findEmployee(Long id) {
		return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
	}

	/**
	 * {@code currentEmployeeId} is null on create. On update we must ignore the
	 * employee's own email so saving without changing email still works.
	 */
	private void assertEmailAvailable(String email, Long currentEmployeeId) {
		String normalized = email.trim().toLowerCase();
		boolean taken = currentEmployeeId == null
				? employeeRepository.existsByEmailIgnoreCase(normalized)
				: employeeRepository.existsByEmailIgnoreCaseAndIdNot(normalized, currentEmployeeId);
		if (taken) {
			throw new DuplicateEmailException(normalized);
		}
	}
}
