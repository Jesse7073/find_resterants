package com.findrestaurants.backend.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findrestaurants.backend.auth.dto.LoginRequest;
import com.findrestaurants.backend.auth.dto.LoginResponse;
import com.findrestaurants.backend.auth.dto.RegisterRequest;
import com.findrestaurants.backend.auth.dto.RegisterResponse;
import com.findrestaurants.backend.auth.exception.AuthExceptionHandler;
import com.findrestaurants.backend.auth.exception.DuplicateEmailException;
import com.findrestaurants.backend.auth.exception.InvalidCredentialsException;
import com.findrestaurants.backend.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 以 standalone MockMvc（不啟動完整 Spring context）測試 Controller + AuthExceptionHandler 的整合行為，
 * 驗證 HTTP status / 回應格式符合 docs/phase3-api-spec.md，AuthService 商業邏輯以 mock 隔離（已於 AuthServiceTest 覆蓋）。
 */
class AuthControllerTest {

	private final AuthService authService = mock(AuthService.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		AuthController controller = new AuthController(authService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new AuthExceptionHandler())
				.build();
	}

	@Test
	void register_success_returns201WithIdAndEmail() throws Exception {
		when(authService.register(any(RegisterRequest.class)))
				.thenReturn(new RegisterResponse(1L, "user@example.com"));

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterRequest("user@example.com", "password123"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.email").value("user@example.com"));
	}

	@Test
	void register_duplicateEmail_returns409WithErrorFormat() throws Exception {
		when(authService.register(any(RegisterRequest.class)))
				.thenThrow(new DuplicateEmailException("dup@example.com"));

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterRequest("dup@example.com", "password123"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
				.andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	void login_success_returns200WithTokenAndExpiresIn() throws Exception {
		when(authService.login(any(LoginRequest.class)))
				.thenReturn(new LoginResponse("jwt-token-value", 3600L));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "correct-password"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("jwt-token-value"))
				.andExpect(jsonPath("$.expiresIn").value(3600));
	}

	@Test
	void login_invalidCredentials_returns401WithErrorFormat() throws Exception {
		when(authService.login(any(LoginRequest.class)))
				.thenThrow(new InvalidCredentialsException());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "wrong-password"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
				.andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
	}

	@Test
	void register_blankEmail_returns400ValidationError() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterRequest("", "password123"))))
				.andExpect(status().isBadRequest());
	}
}
