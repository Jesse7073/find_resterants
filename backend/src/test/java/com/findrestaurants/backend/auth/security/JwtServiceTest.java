package com.findrestaurants.backend.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	private static final String SECRET = "unit-test-secret-key-must-be-at-least-32-bytes-long";

	@Test
	void generateToken_thenValidateAndParse_roundTripsUserIdAndEmail() {
		JwtService jwtService = new JwtService(SECRET, 3600L);

		String token = jwtService.generateToken(42L, "user@example.com");
		Optional<Claims> claims = jwtService.validateAndParse(token);

		assertThat(claims).isPresent();
		assertThat(jwtService.extractUserId(claims.get())).isEqualTo(42L);
		assertThat(claims.get().get("email", String.class)).isEqualTo("user@example.com");
	}

	@Test
	void validateAndParse_malformedToken_returnsEmpty() {
		JwtService jwtService = new JwtService(SECRET, 3600L);

		Optional<Claims> claims = jwtService.validateAndParse("not-a-valid-jwt");

		assertThat(claims).isEmpty();
	}

	@Test
	void validateAndParse_tokenSignedWithDifferentSecret_returnsEmpty() {
		JwtService issuer = new JwtService(SECRET, 3600L);
		JwtService verifier = new JwtService("a-completely-different-secret-key-32-bytes-min", 3600L);

		String token = issuer.generateToken(1L, "user@example.com");

		assertThat(verifier.validateAndParse(token)).isEmpty();
	}

	@Test
	void validateAndParse_expiredToken_returnsEmpty() throws InterruptedException {
		// expiration-seconds = 0 秒代表簽發當下即視為已過期，避免測試依賴 sleep 造成不穩定
		JwtService jwtService = new JwtService(SECRET, 0L);

		String token = jwtService.generateToken(1L, "user@example.com");
		Thread.sleep(1000); // jjwt 以「秒」為單位比對過期時間，需跨過至少 1 秒邊界確保已過期

		assertThat(jwtService.validateAndParse(token)).isEmpty();
	}
}
