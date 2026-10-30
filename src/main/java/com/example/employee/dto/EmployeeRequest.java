package com.example.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Incoming JSON body for create (POST) and update (PUT).
 *
 * Why this class exists:
 * Clients must not send {@code id}, {@code createdAt}, or {@code updatedAt}.
 * Those fields are owned by the server. A dedicated request DTO makes that
 * contract obvious and is the object Bean Validation inspects.
 *
 * Execution flow:
 * HTTP JSON -> Jackson deserializes into this class -> {@code @Valid} runs
 * the annotations below -> controller passes it to the service.
 *
 * Interview concept — Bean Validation is triggered only when the controller
 * parameter is annotated with {@code @Valid} (or {@code @Validated}).
 * The annotations here do nothing by themselves.
 */
public class EmployeeRequest {

	@NotBlank(message = "First name is required")
	@Size(max = 50, message = "First name must be at most 50 characters")
	private String firstName;

	@NotBlank(message = "Last name is required")
	@Size(max = 50, message = "Last name must be at most 50 characters")
	private String lastName;

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be a valid address")
	@Size(max = 100, message = "Email must be at most 100 characters")
	private String email;

	@NotBlank(message = "Department is required")
	@Size(max = 50, message = "Department must be at most 50 characters")
	private String department;

	@NotNull(message = "Salary is required")
	@PositiveOrZero(message = "Salary cannot be negative")
	private BigDecimal salary;

	public EmployeeRequest() {
	}

	public EmployeeRequest(String firstName, String lastName, String email, String department, BigDecimal salary) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.department = department;
		this.salary = salary;
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
}
