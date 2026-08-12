package com.findrestaurants.backend.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 驗證每次請求帶入的 JWT（Authorization: Bearer &lt;token&gt;）。
 * 這張票只負責「token 存不存在、合不合法」，驗證成功後把使用者 id 放進 SecurityContext 的 principal，
 * 供後續模組（T-03 起）用 @PreAuthorize 或 SecurityContextHolder 取用；不在此決定哪些路徑需要登入，
 * 那是各業務模組自己端點的職責（見 SecurityConfig 的說明）。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTH_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		String header = request.getHeader(AUTH_HEADER);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			String token = header.substring(BEARER_PREFIX.length());
			Optional<Claims> claims = jwtService.validateAndParse(token);
			if (claims.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
				Long userId = jwtService.extractUserId(claims.get());
				var authentication = new UsernamePasswordAuthenticationToken(
						userId, null, List.of());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			// token 存在但不合法（簽章錯誤/過期/格式錯誤）：不設定 Authentication，交由後續授權規則視為未登入處理
		}

		filterChain.doFilter(request, response);
	}
}
