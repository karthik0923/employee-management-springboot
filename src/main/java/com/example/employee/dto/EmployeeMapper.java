package com.example.employee.dto;

import com.example.employee.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * Translates between API DTOs and the JPA entity.
 *
 * Why this class exists:
 * The service should not be filled with {@code setFirstName}/{@code getFirstName}
 * copying. Mapping is a separate, easy-to-test responsibility. No MapStruct
 * is used so you can see every field assignment — that is what interviewers
 * expect you to understand before using a generator.
 *
 * {@code @Component} registers this class as a Spring bean so the service can
 * constructor-inject it.
 */
@Component
public class EmployeeMapper {

	public Employee toEntity(EmployeeRequest request) {
		Employee employee = new Employee();
		applyRequest(employee, request);
		return employee;
	}

	public void updateEntity(Employee employee, EmployeeRequest request) {
		applyRequest(employee, request);
	}

	public EmployeeResponse toResponse(Employee employee) {
		return new EmployeeResponse(employee.getId(), employee.getFirstName(), employee.getLastName(),
				employee.getEmail(), employee.getDepartment(), employee.getSalary(), employee.getCreatedAt(),
				employee.getUpdatedAt());
	}

	private void applyRequest(Employee employee, EmployeeRequest request) {
		employee.setFirstName(request.getFirstName().trim());
		employee.setLastName(request.getLastName().trim());
		employee.setEmail(request.getEmail().trim().toLowerCase());
		employee.setDepartment(request.getDepartment().trim());
		employee.setSalary(request.getSalary());
	}
}
