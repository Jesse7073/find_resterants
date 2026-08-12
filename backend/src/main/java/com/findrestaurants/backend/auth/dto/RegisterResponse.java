package com.findrestaurants.backend.auth.dto;

/** POST /api/v1/auth/register 成功時（201）的回應 body，對應 docs/phase3-api-spec.md。 */
public record RegisterResponse(Long id, String email) {
}
