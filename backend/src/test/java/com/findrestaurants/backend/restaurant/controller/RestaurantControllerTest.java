package com.findrestaurants.backend.restaurant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findrestaurants.backend.restaurant.dto.CreateRestaurantRequest;
import com.findrestaurants.backend.restaurant.dto.UpdateRestaurantRequest;
import com.findrestaurants.backend.restaurant.entity.Restaurant;
import com.findrestaurants.backend.restaurant.exception.ForbiddenNotOwnerException;
import com.findrestaurants.backend.restaurant.exception.RestaurantExceptionHandler;
import com.findrestaurants.backend.restaurant.exception.RestaurantNotFoundException;
import com.findrestaurants.backend.restaurant.service.RestaurantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

/**
 * 以 standalone MockMvc（不啟動完整 Spring context）測試 Controller + RestaurantExceptionHandler 的整合行為，
 * 驗證 HTTP status / 回應格式符合 docs/phase3-api-spec.md 本票範圍；商業邏輯以 mock RestaurantService 隔離。
 * @AuthenticationPrincipal 在 standalone 模式下需手動註冊 AuthenticationPrincipalArgumentResolver，
 * 並在測試中把 principal（Long userId）直接放進 SecurityContextHolder，模擬 JwtAuthenticationFilter 的行為。
 */
class RestaurantControllerTest {

	private final RestaurantService restaurantService = mock(RestaurantService.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		RestaurantController controller = new RestaurantController(restaurantService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new RestaurantExceptionHandler())
				.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
				.build();
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private void authenticateAs(Long userId) {
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
	}

	@Test
	void list_returnsPagedResultWithFavoritedFixedFalse() throws Exception {
		Restaurant restaurant = newRestaurantWithId(1L, "餐廳A", "地址A", 5L);
		var page = new PageImpl<>(List.of(restaurant), PageRequest.of(0, 20), 1);
		when(restaurantService.list(any())).thenReturn(page);

		mockMvc.perform(get("/api/v1/restaurants"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].isFavorited").value(false))
				.andExpect(jsonPath("$.content[0].isDeleted").value(false))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void getById_found_returns200WithRestaurantFields() throws Exception {
		Restaurant restaurant = newRestaurantWithId(1L, "餐廳A", "地址A", 5L);
		when(restaurantService.getById(1L)).thenReturn(restaurant);

		mockMvc.perform(get("/api/v1/restaurants/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("餐廳A"))
				.andExpect(jsonPath("$.createdBy").value(5))
				.andExpect(jsonPath("$.isDeleted").value(false));
	}

	@Test
	void getById_deletedRestaurant_returns200WithIsDeletedTrue() throws Exception {
		Restaurant restaurant = newRestaurantWithId(1L, "餐廳A", "地址A", 5L);
		restaurant.markDeleted();
		when(restaurantService.getById(1L)).thenReturn(restaurant);

		mockMvc.perform(get("/api/v1/restaurants/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isDeleted").value(true));
	}

	@Test
	void getById_missing_returns404WithRestaurantNotFoundCode() throws Exception {
		when(restaurantService.getById(99L)).thenThrow(new RestaurantNotFoundException(99L));

		mockMvc.perform(get("/api/v1/restaurants/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.code").value("RESTAURANT_NOT_FOUND"))
				.andExpect(jsonPath("$.path").value("/api/v1/restaurants/99"));
	}

	@Test
	void create_authenticated_returns201WithDuplicateCandidatesEmpty() throws Exception {
		authenticateAs(5L);
		Restaurant restaurant = newRestaurantWithId(1L, "新餐廳", "新地址", 5L);
		when(restaurantService.create(any(CreateRestaurantRequest.class), eq(5L))).thenReturn(restaurant);

		mockMvc.perform(post("/api/v1/restaurants")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new CreateRestaurantRequest("新餐廳", "新地址", null, null))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.createdBy").value(5))
				.andExpect(jsonPath("$.duplicateCandidates").isArray())
				.andExpect(jsonPath("$.duplicateCandidates").isEmpty());
	}

	@Test
	void create_blankName_returns400ValidationError() throws Exception {
		authenticateAs(5L);

		mockMvc.perform(post("/api/v1/restaurants")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new CreateRestaurantRequest("", "地址", null, null))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void update_byOwner_returns200WithUpdatedFields() throws Exception {
		authenticateAs(5L);
		Restaurant updated = newRestaurantWithId(1L, "改過的名字", "地址A", 5L);
		when(restaurantService.update(eq(1L), any(UpdateRestaurantRequest.class), eq(5L))).thenReturn(updated);

		mockMvc.perform(put("/api/v1/restaurants/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new UpdateRestaurantRequest("改過的名字", null, null, null))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("改過的名字"));
	}

	@Test
	void update_byNonOwner_returns403WithForbiddenNotOwnerCode() throws Exception {
		authenticateAs(999L);
		when(restaurantService.update(eq(1L), any(UpdateRestaurantRequest.class), eq(999L)))
				.thenThrow(new ForbiddenNotOwnerException());

		mockMvc.perform(put("/api/v1/restaurants/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new UpdateRestaurantRequest("竄改名字", null, null, null))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.code").value("FORBIDDEN_NOT_OWNER"))
				.andExpect(jsonPath("$.path").value("/api/v1/restaurants/1"));
	}

	@Test
	void update_missingRestaurant_returns404WithRestaurantNotFoundCode() throws Exception {
		authenticateAs(5L);
		when(restaurantService.update(eq(99L), any(UpdateRestaurantRequest.class), eq(5L)))
				.thenThrow(new RestaurantNotFoundException(99L));

		mockMvc.perform(put("/api/v1/restaurants/99")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new UpdateRestaurantRequest("名字", null, null, null))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESTAURANT_NOT_FOUND"));
	}

	@Test
	void delete_byOwner_returns204() throws Exception {
		authenticateAs(5L);

		mockMvc.perform(delete("/api/v1/restaurants/1"))
				.andExpect(status().isNoContent());

		verify(restaurantService).delete(1L, 5L);
	}

	@Test
	void delete_byNonOwner_returns403WithForbiddenNotOwnerCode() throws Exception {
		authenticateAs(999L);
		org.mockito.Mockito.doThrow(new ForbiddenNotOwnerException())
				.when(restaurantService).delete(anyLong(), eq(999L));

		mockMvc.perform(delete("/api/v1/restaurants/1"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN_NOT_OWNER"));
	}

	@Test
	void delete_missingRestaurant_returns404WithRestaurantNotFoundCode() throws Exception {
		authenticateAs(5L);
		org.mockito.Mockito.doThrow(new RestaurantNotFoundException(99L))
				.when(restaurantService).delete(eq(99L), eq(5L));

		mockMvc.perform(delete("/api/v1/restaurants/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESTAURANT_NOT_FOUND"));
	}

	private static Restaurant newRestaurantWithId(Long id, String name, String address, Long createdBy) {
		try {
			Restaurant restaurant = new Restaurant(name, address, "category", "note", createdBy);
			var idField = Restaurant.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(restaurant, id);
			return restaurant;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
