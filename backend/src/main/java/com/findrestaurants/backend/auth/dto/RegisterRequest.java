package com.findrestaurants.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * POST /api/v1/auth/register 的請求 body。
 * 密碼長度/複雜度規則未在 docs/phase2-requirements-spec.md、docs/phase3-api-spec.md 中定義，
 * 本票僅做「不可為空」的基本驗證，不自行發明未經規格明訂的密碼強度規則。
 */
public record RegisterRequest(

		@NotBlank(message = "email 不可為空")
		@Email(message = "email 格式不正確")
		String email,

		@NotBlank(message = "password 不可為空")
		String password) {
}
