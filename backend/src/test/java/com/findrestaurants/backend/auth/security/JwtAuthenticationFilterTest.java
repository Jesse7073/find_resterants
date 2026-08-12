package com.findrestaurants.backend.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

	private static final String SECRET = "unit-test-secret-key-must-be-at-least-32-bytes-long";

	private final JwtService jwtService = new JwtService(SECRET, 3600L);
	private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilter_validBearerToken_setsAuthenticationWithUserId() throws Exception {
		String token = jwtService.generateToken(7L, "user@example.com");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + token);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		var authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(authentication).isNotNull();
		assertThat(authentication.getPrincipal()).isEqualTo(7L);
	}

	@Test
	void doFilter_missingAuthorizationHeader_doesNotSetAuthenticationButContinuesChain() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain spyChain = new MockFilterChain();

		filter.doFilter(request, response, spyChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(spyChain.getRequest()).isNotNull();
	}

	@Test
	void doFilter_invalidToken_doesNotSetAuthentication() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer this-is-not-a-valid-jwt");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}
}
