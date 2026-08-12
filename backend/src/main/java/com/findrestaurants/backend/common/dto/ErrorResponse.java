package com.findrestaurants.backend.common.dto;

import java.time.Instant;

/**
 * 統一錯誤回應格式，對應 docs/phase3-api-spec.md 的「錯誤處理格式（統一）」。
 * T-02 僅使用本類別處理 auth 模組已知的錯誤情境；完整的全站例外處理框架留待 T-10 統一收斂。
 */
public record ErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String code,
		String message,
		String path) {

	public static ErrorResponse of(int status, String error, String code, String message, String path) {
		return new ErrorResponse(Instant.now(), status, error, code, message, path);
	}
}
