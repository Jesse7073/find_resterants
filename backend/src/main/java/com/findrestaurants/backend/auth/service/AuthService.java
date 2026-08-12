package com.findrestaurants.backend.auth.service;

import com.findrestaurants.backend.auth.dto.LoginRequest;
import com.findrestaurants.backend.auth.dto.LoginResponse;
import com.findrestaurants.backend.auth.dto.RegisterRequest;
import com.findrestaurants.backend.auth.dto.RegisterResponse;
import com.findrestaurants.backend.auth.entity.User;
import com.findrestaurants.backend.auth.exception.DuplicateEmailException;
import com.findrestaurants.backend.auth.exception.InvalidCredentialsException;
import com.findrestaurants.backend.auth.repository.UserRepository;
import com.findrestaurants.backend.auth.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 註冊/登入商業邏輯：密碼雜湊/驗證、email 唯一性檢查、JWT 簽發。 */
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new DuplicateEmailException(request.email());
		}

		String passwordHash = passwordEncoder.encode(request.password());
		User saved;
		try {
			saved = userRepository.save(new User(request.email(), passwordHash));
		} catch (DataIntegrityViolationException ex) {
			// existsByEmail 檢查與 save 之間存在競速窗口（TOCTOU）；真正的唯一性保證來自 users.email 的 UNIQUE
			// constraint（V1__init_schema.sql），這裡把違反該 constraint 的情況同樣轉換成 DUPLICATE_EMAIL。
			throw new DuplicateEmailException(request.email());
		}

		return new RegisterResponse(saved.getId(), saved.getEmail());
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		String token = jwtService.generateToken(user.getId(), user.getEmail());
		return new LoginResponse(token, jwtService.getExpirationSeconds());
	}
}
