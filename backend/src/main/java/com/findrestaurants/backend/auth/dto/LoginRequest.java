package com.findrestaurants.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** POST /api/v1/auth/login 的請求 body。 */
public record LoginRequest(

		@NotBlank(message = "email 不可為空")
		String email,

		@NotBlank(message = "password 不可為空")
		String password) {
}
