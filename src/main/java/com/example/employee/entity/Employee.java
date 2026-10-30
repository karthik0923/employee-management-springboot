package com.example.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity mapped to the {@code employees} table.
 *
 * Why this class exists:
 * Hibernate needs a Java class that represents one database row. This is a
 * persistence model, not an API model. Controllers never return this type
 * directly — see {@code EmployeeResponse} in the dto package.
 *
 * How it interacts with other layers:
 * Repository saves/loads this object. Service maps it to/from DTOs.
 * Hibernate generates SQL (INSERT/SELECT/UPDATE/DELETE) from the annotations.
 *
 * Interview concept — Entity vs DTO:
 * An entity is tied to the database (table, columns, lifecycle callbacks).
 * A DTO is tied to the API contract. Keeping them separate lets the table
 * change without breaking clients, and hides fields you do not want to expose.
 *
 * No Lombok is used on purpose so you can see the JPA requirement for a
 * no-arg constructor plus getters/setters.
 */
@Entity
@Table(name = "employees")
public class Employee {

	/**
	 * {@code @Id} marks the primary key.
	 * {@code IDENTITY} lets H2 auto-increment the value. After {@code save()},
	 * Hibernate populates this field with the generated id.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 50)
	private String department;

	/**
	 * Money must never be a {@code double}. {@code BigDecimal} keeps exact scale.
	 * {@code precision} / {@code scale} become DECIMAL(12,2) in H2.
	 */
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal salary;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	/**
	 * JPA requires a no-arg constructor so Hibernate can instantiate rows
	 * via reflection when loading from the database.
	 */
	public Employee() {
	}

	public Employee(String firstName, String lastName, String email, String department, BigDecimal salary) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.department = department;
		this.salary = salary;
	}

	/**
	 * JPA lifecycle callback. Hibernate calls this immediately before INSERT.
	 * This is how createdAt and updatedAt are set automatically — the controller
	 * and service never assign them.
	 */
	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	/**
	 * Called immediately before UPDATE. Only updatedAt changes.
	 */
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
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

	@Override
	public String toString() {
		return "Employee{id=" + id + ", email='" + email + "', department='" + department + "'}";
	}
}
