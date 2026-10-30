package com.example.employee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Consistent error JSON returned by {@code GlobalExceptionHandler}.
 *
 * Why this class exists:
 * Without it, Spring Boot's default error body changes between versions and
 * is harder for clients to parse. One shape for every error is an interview
 * favorite: timestamp, status, error, message, path, optional field errors.
 *
 * {@code NON_NULL} omits {@code fieldErrors} for non-validation failures so
 * 404 responses stay small.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

	private LocalDateTime timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
	private Map<String, String> fieldErrors;

	public ErrorResponse() {
	}

	public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path,
			Map<String, String> fieldErrors) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
		this.fieldErrors = fieldErrors;
	}

	public static ErrorResponse of(int status, String error, String message, String path) {
		return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(Map<String, String> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}
}
