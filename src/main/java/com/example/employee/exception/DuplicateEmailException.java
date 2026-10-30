package com.example.employee.exception;

/**
 * Thrown when create/update would violate the unique email rule.
 *
 * Why this class exists:
 * Unique emails are a business rule. Catching it in the service (before the
 * INSERT fails) produces a clear HTTP 409 instead of a raw database error.
 */
public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException(String email) {
		super("An employee with email '" + email + "' already exists");
	}
}
