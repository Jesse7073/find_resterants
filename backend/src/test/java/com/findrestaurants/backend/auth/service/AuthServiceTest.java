package com.findrestaurants.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.findrestaurants.backend.auth.dto.LoginRequest;
import com.findrestaurants.backend.auth.dto.LoginResponse;
import com.findrestaurants.backend.auth.dto.RegisterRequest;
import com.findrestaurants.backend.auth.dto.RegisterResponse;
import com.findrestaurants.backend.auth.entity.User;
import com.findrestaurants.backend.auth.exception.DuplicateEmailException;
import com.findrestaurants.backend.auth.exception.InvalidCredentialsException;
import com.findrestaurants.backend.auth.repository.UserRepository;
import com.findrestaurants.backend.auth.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private JwtService jwtService;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		jwtService = mock(JwtService.class);
		authService = new AuthService(userRepository, passwordEncoder, jwtService);
	}

	@Test
	void register_savesHashedPasswordAndReturnsIdEmail() {
		RegisterRequest request = new RegisterRequest("user@example.com", "plaintext-password");
		when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
		when(passwordEncoder.encode("plaintext-password")).thenReturn("hashed-value");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			return newUserWithId(1L, user.getEmail(), user.getPasswordHash());
		});

		RegisterResponse response = authService.register(request);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.email()).isEqualTo("user@example.com");
		verify(passwordEncoder).encode("plaintext-password");
	}

	@Test
	void register_duplicateEmail_throwsDuplicateEmailException() {
		RegisterRequest request = new RegisterRequest("dup@example.com", "password123");
		when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(DuplicateEmailException.class);

		verify(userRepository, org.mockito.Mockito.never()).save(any());
	}

	@Test
	void register_raceConditionOnUniqueConstraint_throwsDuplicateEmailException() {
		// existsByEmail 檢查通過（此刻還沒有重複），但實際 save 時撞到 DB UNIQUE constraint（另一個並行請求先寫入）
		RegisterRequest request = new RegisterRequest("race@example.com", "password123");
		when(userRepository.existsByEmail("race@example.com")).thenReturn(false);
		when(passwordEncoder.encode(anyString())).thenReturn("hashed-value");
		when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(DuplicateEmailException.class);
	}

	@Test
	void login_correctCredentials_returnsTokenAndExpiresIn() {
		LoginRequest request = new LoginRequest("user@example.com", "correct-password");
		User user = newUserWithId(1L, "user@example.com", "hashed-value");
		when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("correct-password", "hashed-value")).thenReturn(true);
		when(jwtService.generateToken(1L, "user@example.com")).thenReturn("jwt-token-value");
		when(jwtService.getExpirationSeconds()).thenReturn(3600L);

		LoginResponse response = authService.login(request);

		assertThat(response).isEqualTo(new LoginResponse("jwt-token-value", 3600L));
	}

	@Test
	void login_emailNotFound_throwsInvalidCredentialsException() {
		LoginRequest request = new LoginRequest("missing@example.com", "whatever");
		when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void login_wrongPassword_throwsInvalidCredentialsException() {
		LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
		User user = newUserWithId(1L, "user@example.com", "hashed-value");
		when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-password", "hashed-value")).thenReturn(false);

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	/** User 的 id 由 JPA @GeneratedValue 產生，測試需要透過 reflection 建構帶 id 的假物件模擬 repository 回傳值。 */
	private static User newUserWithId(Long id, String email, String passwordHash) {
		try {
			User user = new User(email, passwordHash);
			var idField = User.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(user, id);
			return user;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
