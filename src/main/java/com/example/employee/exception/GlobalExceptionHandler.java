package com.example.employee.exception;

import com.example.employee.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Central place that turns exceptions into HTTP responses.
 *
 * Why this class exists:
 * Without it, every controller would need try/catch blocks and the JSON error
 * shape would be inconsistent. {@code @RestControllerAdvice} is a Spring AOP
 * interceptor around all {@code @RestController} methods.
 *
 * Execution flow:
 * Controller/service throws -> DispatcherServlet finds a matching
 * {@code @ExceptionHandler} in this class -> method returns ResponseEntity
 * -> Jackson writes ErrorResponse as JSON.
 *
 * Interview concept:
 * {@code @RestControllerAdvice} = {@code @ControllerAdvice} + {@code @ResponseBody}.
 * Prefer this over {@code @ResponseStatus} on the exception class when you
 * need a custom body, not just a status code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(EmployeeNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException exception,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException exception,
			HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), null);
	}

	/**
	 * Triggered when {@code @Valid} fails on {@code @RequestBody}.
	 * {@code fieldErrors} maps each invalid field to its message so the client
	 * can highlight form inputs.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
		return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Request body is missing or contains invalid JSON", request.getRequestURI(),
				null);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestParam(MissingServletRequestParameterException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
			HttpServletRequest request) {
		String message = "Parameter '" + exception.getName() + "' has an invalid value: " + exception.getValue();
		return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
		log.error("Unhandled exception for {}", request.getRequestURI(), exception);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI(), null);
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path,
			Map<String, String> fieldErrors) {
		ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, path);
		body.setFieldErrors(fieldErrors);
		return ResponseEntity.status(status).body(body);
	}
}
