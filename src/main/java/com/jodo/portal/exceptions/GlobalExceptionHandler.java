package com.jodo.portal.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.context.request.WebRequest;
import java.net.http.HttpHeaders;
import java.nio.file.AccessDeniedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
			HttpServletRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
				"Could not execute statement: Duplicate entry [" + ex.getMessage() + "]", request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex,
			HttpServletRequest request) {
		StringBuilder violations = new StringBuilder();
		Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
		for (ConstraintViolation<?> violation : constraintViolations) {
			violations.append(violation.getMessage()).append("; ");
		}
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.BAD_REQUEST, violations.toString(),
				request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			org.springframework.http.HttpHeaders headers, HttpStatus status,
			org.springframework.web.context.request.WebRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed",
				request.getContextPath());

		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> {
			fieldErrors.put(error.getField(), error.getDefaultMessage());
		});
		errorDetails.put("fieldErrors", fieldErrors);
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<?> handleCustomException(CustomException ex, HttpServletRequest request) {
		if (ex.getStatusCode() > 0) {
			if (ex.getStatus() == null) {
				Map<String, Object> errorDetails = createCustomErrorResponse(ex.getStatusCode(),
						ex.getStatusDescription(), ex.getMessage(), request.getRequestURI());
				return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
			}
			Map<String, Object> errorDetails = createCustomErrorResponse(ex.getStatusCode(), ex.getStatusDescription(),
					ex.getMessage(), request.getRequestURI());
			return new ResponseEntity<>(errorDetails, ex.getStatus());
		}
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
				request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(JwtValidationException.class)
	public ResponseEntity<Map<String, Object>> handleJwtValidationException(JwtValidationException ex,
			HttpServletRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.valueOf(ex.getStatusCode()), ex.getMessage(),
				request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, HttpServletRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
				request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException ex,
			HttpServletRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
				request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex,
			HttpServletRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
				request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex,
			HttpServletRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(),
				request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(NoSuchMethodException.class)
	public ResponseEntity<Map<String, Object>> handleNoSuchMethodException(NoSuchMethodException ex,
			HttpServletRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED,
				"Method not found: " + ex.getMessage(), request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.METHOD_NOT_ALLOWED);
	}
//	@ExceptionHandler(MethodArgumentNotValidException.class)
//	public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
//	    Map<String, String> errors = new HashMap<>();
//	    ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
//	        errors.put(fieldError.getField(), fieldError.getDefaultMessage());
//	    });
//	    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex,
			HttpServletRequest request) {
		Map<String, Object> errorDetails = createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(),
				request.getRequestURI());
		return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
	}

	private Map<String, Object> createErrorResponse(HttpStatus status, String message, String path) {
		Map<String, Object> errorDetails = new LinkedHashMap<>();
		errorDetails.put("status", status.value());
		errorDetails.put("statusdescription", status.getReasonPhrase());
		errorDetails.put("message", message);
		errorDetails.put("timestamp", SF.format(new Date()));
//		errorDetails.put("path", path);
		return errorDetails;
	}

	private Map<String, Object> createCustomErrorResponse(int status, String statusDescription, String message,
			String path) {
		Map<String, Object> errorDetails = new LinkedHashMap<>();
		errorDetails.put("status", status);
		errorDetails.put("statusdescription", statusDescription);
		errorDetails.put("message", message);
		errorDetails.put("timestamp", SF.format(new Date()));
//		errorDetails.put("path", path);
		return errorDetails;
	}
}
