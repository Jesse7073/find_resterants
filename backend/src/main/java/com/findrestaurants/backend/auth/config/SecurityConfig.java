package com.findrestaurants.backend.auth.config;

import com.findrestaurants.backend.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * 這張票只放行 /api/v1/auth/** 這個目前唯一存在的公開端點群組；其餘路徑一律要求已驗證身分
 * （anyRequest().authenticated()）。後續模組（T-03 起）若有自己的公開端點（例如 GET /restaurants、
 * GET /public-tags 依 docs/phase3-api-spec.md 標註「否」），由該票自行在此設定新增對應的
 * permitAll() 規則，不在本票預先猜測。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String AUTH_PUBLIC_PATHS = "/api/v1/auth/**";

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable()) // stateless JWT API，不使用 cookie-based session，CSRF 風險不適用
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(AUTH_PUBLIC_PATHS).permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
