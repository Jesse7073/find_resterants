package com.findrestaurants.backend.auth.exception;

import com.findrestaurants.backend.auth.controller.AuthController;
import com.findrestaurants.backend.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 僅處理 auth 模組已知的兩種業務錯誤（DUPLICATE_EMAIL、INVALID_CREDENTIALS），
 * 回應格式對應 docs/phase3-api-spec.md 的統一錯誤格式。
 * 刻意限定 assignableTypes = AuthController.class，不做成全站例外處理框架（該範圍留給 T-10）。
 */
@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
		ErrorResponse body = ErrorResponse.of(
				HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.name(),
				"DUPLICATE_EMAIL",
				"此 email 已被註冊",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
		ErrorResponse body = ErrorResponse.of(
				HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED.name(),
				"INVALID_CREDENTIALS",
				"帳號或密碼錯誤",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}
}
