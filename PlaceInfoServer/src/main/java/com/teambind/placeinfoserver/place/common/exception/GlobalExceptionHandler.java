package com.teambind.placeinfoserver.place.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleProfileException(
			CustomException ex, HttpServletRequest request) {
		ErrorCode errorCode = ex.getErrorCode();
		HttpStatus status = errorCode.getStatus();
		ErrorResponse body =
				ErrorResponse.of(
						status.value(),
						errorCode.getErrCode(),
						errorCode.getMessage(),
						request.getRequestURI());
		return ResponseEntity.status(status).body(body);
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpHeaders headers,
			org.springframework.http.HttpStatusCode status,
			WebRequest request) {
		String message =
				ex.getBindingResult().getFieldErrors().stream()
						.findFirst()
						.map(err -> err.getField() + ": " + err.getDefaultMessage())
						.orElse("Validation failed");
		ErrorResponse body =
				ErrorResponse.of(
						HttpStatus.BAD_REQUEST.value(),
						"VALIDATION_ERROR",
						message,
						request.getDescription(false));
		return ResponseEntity.badRequest().body(body);
	}
}
