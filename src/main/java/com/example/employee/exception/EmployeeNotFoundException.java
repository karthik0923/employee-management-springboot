package com.example.employee.exception;

/**
 * Thrown when a requested employee id does not exist.
 *
 * Why this class exists:
 * Returning {@code null} from the service forces every caller to check null
 * and guess the HTTP status. An explicit exception lets
 * {@code GlobalExceptionHandler} map the case to HTTP 404 in one place.
 *
 * {@code RuntimeException} is unchecked, so service methods stay free of
 * {@code throws} clauses. Spring's transaction interceptor also rolls back
 * on runtime exceptions by default.
 */
public class EmployeeNotFoundException extends RuntimeException {

	public EmployeeNotFoundException(Long id) {
		super("Employee not found with id: " + id);
	}
}
