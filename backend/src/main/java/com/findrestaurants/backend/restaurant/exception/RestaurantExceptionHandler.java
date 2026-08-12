package com.findrestaurants.backend.restaurant.exception;

import com.findrestaurants.backend.common.dto.ErrorResponse;
import com.findrestaurants.backend.restaurant.controller.RestaurantController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 僅處理 restaurant 模組已知的兩種業務錯誤（RESTAURANT_NOT_FOUND、FORBIDDEN_NOT_OWNER），
 * 回應格式對應 docs/phase3-api-spec.md 的統一錯誤格式。刻意限定 assignableTypes = RestaurantController.class，
 * 沿用 T-02 AuthExceptionHandler 的作法，不做成全站例外處理框架（該範圍留給 T-10）。
 */
@RestControllerAdvice(assignableTypes = RestaurantController.class)
public class RestaurantExceptionHandler {

	@ExceptionHandler(RestaurantNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(RestaurantNotFoundException ex, HttpServletRequest request) {
		ErrorResponse body = ErrorResponse.of(
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.name(),
				"RESTAURANT_NOT_FOUND",
				"找不到指定的餐廳",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(ForbiddenNotOwnerException.class)
	public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenNotOwnerException ex, HttpServletRequest request) {
		ErrorResponse body = ErrorResponse.of(
				HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN.name(),
				"FORBIDDEN_NOT_OWNER",
				"僅原始新增者可編輯此餐廳",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}
}
