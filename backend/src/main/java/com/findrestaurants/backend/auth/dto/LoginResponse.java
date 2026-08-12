package com.findrestaurants.backend.auth.dto;

/** POST /api/v1/auth/login 成功時（200）的回應 body，對應 docs/phase3-api-spec.md。 */
public record LoginResponse(String token, long expiresIn) {
}
