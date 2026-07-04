package com.example.feature.flag.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(FeatureNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(
		FeatureNotFoundException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(DuplicateFeatureException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicate(
		DuplicateFeatureException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(InvalidStrategyException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidStrategy(
		InvalidStrategyException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		String message = "Validation failed";
		FieldError fieldError = exception.getBindingResult().getFieldError();
		if (fieldError != null && fieldError.getDefaultMessage() != null) {
			message = fieldError.getDefaultMessage();
		}
		return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnhandled(
		Exception exception,
		HttpServletRequest request
	) {
		return buildError(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Internal server error",
			request.getRequestURI()
		);
	}

	private ResponseEntity<ApiErrorResponse> buildError(
		HttpStatus status,
		String message,
		String path
	) {
		ApiErrorResponse response = new ApiErrorResponse(
			OffsetDateTime.now(),
			status.value(),
			status.getReasonPhrase(),
			message,
			path
		);
		return ResponseEntity.status(status).body(response);
	}
}
