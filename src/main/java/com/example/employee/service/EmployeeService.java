package com.example.employee.service;

import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Business-logic contract for employee operations.
 *
 * Why an interface:
 * The controller depends on this type, not on {@code EmployeeServiceImpl}.
 * That makes unit tests easy: Mockito can mock the interface. It also keeps
 * the API of the service small and intention-revealing.
 *
 * Interview concept — program to an interface. Spring injects the
 * implementation because {@code EmployeeServiceImpl} is the only
 * {@code @Service} that implements this interface.
 */
public interface EmployeeService {

	EmployeeResponse createEmployee(EmployeeRequest request);

	PagedResponse<EmployeeResponse> getAllEmployees(Pageable pageable);

	EmployeeResponse getEmployeeById(Long id);

	EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

	void deleteEmployee(Long id);

	List<EmployeeResponse> getEmployeesByDepartment(String department);

	List<EmployeeResponse> searchEmployeesByName(String name);
}
