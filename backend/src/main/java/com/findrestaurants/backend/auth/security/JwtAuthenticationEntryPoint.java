package com.findrestaurants.backend.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 未帶有效 JWT 存取需登入端點時的回應處理。Spring Security 預設會安裝 AnonymousAuthenticationFilter，
 * 未登入請求因此帶有一個「匿名」Authentication（非 null），導致 ExceptionTranslationFilter 判定為
 * 「已認證但權限不足」而交給 AccessDeniedHandler 回 403 Forbidden（空 body），不符合
 * docs/phase3-api-spec.md「認證授權共通規則」要求的「未帶有效 JWT 一律回 401」。
 * 這裡自訂 AuthenticationEntryPoint，讓未帶 JWT 的請求統一回 401，格式與 common.dto.ErrorResponse 一致。
 * T-02 尚未有受保護的端點，此問題到 T-03 新增 POST/PUT/DELETE /restaurants 後才會被實際觸發，因此在此一併修正。
 *
 * 這裡手動組 JSON（而非注入 ObjectMapper/JsonMapper）：本專案（Spring Boot 4.1 / Spring 7）預設 JSON
 * 堆疊已改為 Jackson 3（`tools.jackson.*`），與 T-02 測試程式碼慣用的 `com.fasterxml.jackson`（Jackson 2，
 * 目前只透過 jjwt-jackson 以 runtime scope 存在，main 編譯期不可見）並非同一套件，直接注入型別容易踩空、
 * 也讓這個單一固定格式的錯誤訊息不必要地耦合 JSON 函式庫版本。內容僅為固定訊息與 request URI，無使用者輸入，
 * 不需要處理雙引號逸出。
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		// 訊息含中文字，需明確指定 UTF-8，否則容器預設編碼（ISO-8859-1）會把中文字元寫成亂碼。
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		String body = """
				{"timestamp":"%s","status":401,"error":"UNAUTHORIZED","code":"UNAUTHENTICATED","message":"請先登入","path":"%s"}""".formatted(
				Instant.now(), request.getRequestURI());
		response.getWriter().write(body);
	}
}
