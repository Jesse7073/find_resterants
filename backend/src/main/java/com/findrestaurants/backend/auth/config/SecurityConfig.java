package com.findrestaurants.backend.auth.config;

import com.findrestaurants.backend.auth.security.JwtAuthenticationEntryPoint;
import com.findrestaurants.backend.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 基礎設定：stateless（無 session）、掛載 JwtAuthenticationFilter。
 *
 * 除 /api/v1/auth/** 外，T-03 新增了兩個公開端點：GET /api/v1/restaurants、GET /api/v1/restaurants/{id}
 * （依 docs/phase3-api-spec.md 標註「否」，其餘 restaurants 方法仍需登入）。刻意用 HttpMethod.GET 限定
 * matcher，不把整個 /restaurants/** 都設為公開。其餘路徑一律要求已驗證身分（anyRequest().authenticated()）。
 * 後續模組若有自己的公開端點，由該票自行在此設定新增對應的 permitAll() 規則，不在本票預先猜測。
 *
 * 另外掛載自訂 JwtAuthenticationEntryPoint：T-02 尚無受保護端點時未發現，Spring Security 預設對「未帶
 * 有效 JWT」的請求回 403（因 AnonymousAuthenticationFilter 使 Authentication 非 null），不符合
 * docs/phase3-api-spec.md 要求的 401，T-03 新增受保護的 POST/PUT/DELETE /restaurants 後一併修正。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String AUTH_PUBLIC_PATHS = "/api/v1/auth/**";
	private static final String RESTAURANTS_PATH = "/api/v1/restaurants";
	private static final String RESTAURANTS_ID_PATH = "/api/v1/restaurants/**";

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // stateless JWT API，不使用 cookie-based session，CSRF 風險不適用
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(jwtAuthenticationEntryPoint))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(AUTH_PUBLIC_PATHS).permitAll()
						.requestMatchers(HttpMethod.GET, RESTAURANTS_PATH).permitAll()
						.requestMatchers(HttpMethod.GET, RESTAURANTS_ID_PATH).permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
