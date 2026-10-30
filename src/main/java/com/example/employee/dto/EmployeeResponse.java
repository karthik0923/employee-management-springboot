package com.example.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JSON returned to API clients.
 *
 * Why this class exists:
 * It is the public shape of an employee. If we later add a password hash or
 * internal flag on the entity, it will not leak into JSON because Jackson
 * serializes this class, not {@code Employee}.
 *
 * How it is produced:
 * {@code EmployeeMapper.toResponse} copies values from the entity after
 * the service finishes a create/read/update.
 */
public class EmployeeResponse {

	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String department;
	private BigDecimal salary;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public EmployeeResponse() {
	}

	public EmployeeResponse(Long id, String firstName, String lastName, String email, String department,
			BigDecimal salary, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.department = department;
		this.salary = salary;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
