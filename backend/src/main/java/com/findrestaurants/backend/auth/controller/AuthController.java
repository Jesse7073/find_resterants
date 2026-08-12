package com.findrestaurants.backend.auth.controller;

import com.findrestaurants.backend.auth.dto.LoginRequest;
import com.findrestaurants.backend.auth.dto.LoginResponse;
import com.findrestaurants.backend.auth.dto.RegisterRequest;
import com.findrestaurants.backend.auth.dto.RegisterResponse;
import com.findrestaurants.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 對應 docs/phase3-api-spec.md 的 Auth 端點：POST /auth/register、POST /auth/login，皆為公開端點。 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
		RegisterResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}
}
