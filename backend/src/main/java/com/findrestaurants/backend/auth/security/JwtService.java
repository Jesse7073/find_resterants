package com.findrestaurants.backend.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 簽發與驗證工具，供 auth 模組（登入時簽發）與 JwtAuthenticationFilter（每次請求驗證）共用。
 * 密鑰/過期秒數讀取 application.properties 的 app.jwt.secret / app.jwt.expiration-seconds。
 */
@Component
public class JwtService {

	private static final String CLAIM_EMAIL = "email";

	private final SecretKey signingKey;
	private final long expirationSeconds;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-seconds}") long expirationSeconds) {
		this.signingKey = Keys.hmacShaKeyFor(deriveKeyBytes(secret));
		this.expirationSeconds = expirationSeconds;
	}

	/**
	 * HS256 要求金鑰 >= 256 bits；直接用設定值的原始位元組長度不可靠（操作者設的密鑰字串可能不夠長，
	 * 例如本專案本機開發預設值原本只有 248 bits，啟動就會噴 WeakKeyException）。用 SHA-256 雜湊
	 * 把任意長度的密鑰字串固定正規化成 256-bit 金鑰，避免這類設定失誤直接讓應用程式起不來。
	 */
	private static byte[] deriveKeyBytes(String secret) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 not available", ex);
		}
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}

	/** 簽發 JWT，subject 為使用者 id，並附帶 email claim 供後續模組取用。 */
	public String generateToken(Long userId, String email) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim(CLAIM_EMAIL, email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(expirationSeconds)))
				.signWith(signingKey)
				.compact();
	}

	/** 驗證 token 是否存在且合法（簽章正確、未過期）；不合法回傳 Optional.empty()，不拋例外中斷請求鏈。 */
	public Optional<Claims> validateAndParse(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(signingKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			return Optional.of(claims);
		} catch (JwtException | IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	public Long extractUserId(Claims claims) {
		return Long.valueOf(claims.getSubject());
	}
}
