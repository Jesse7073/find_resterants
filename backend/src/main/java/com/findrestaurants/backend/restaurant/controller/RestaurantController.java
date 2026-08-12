package com.findrestaurants.backend.restaurant.controller;

import com.findrestaurants.backend.common.dto.PageResponse;
import com.findrestaurants.backend.restaurant.dto.CreateRestaurantRequest;
import com.findrestaurants.backend.restaurant.dto.RestaurantCreateResponse;
import com.findrestaurants.backend.restaurant.dto.RestaurantListItemResponse;
import com.findrestaurants.backend.restaurant.dto.RestaurantResponse;
import com.findrestaurants.backend.restaurant.dto.UpdateRestaurantRequest;
import com.findrestaurants.backend.restaurant.entity.Restaurant;
import com.findrestaurants.backend.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 對應 docs/phase3-api-spec.md 的 Restaurants 端點中屬於 T-03 範圍的基本 CRUD：
 * GET /restaurants（基本分頁，不含關鍵字/標籤篩選，那是 T-09）、GET /restaurants/{id}、POST、PUT、DELETE。
 * `/restaurants/parse` 與新增時的 duplicateCandidates 偵測邏輯是 T-04 的範圍，不在此 controller 實作。
 */
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;

	private final RestaurantService restaurantService;

	public RestaurantController(RestaurantService restaurantService) {
		this.restaurantService = restaurantService;
	}

	@GetMapping
	public ResponseEntity<PageResponse<RestaurantListItemResponse>> list(
			@RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = "" + DEFAULT_SIZE) int size) {
		var pageable = PageRequest.of(page, size, Sort.by("id").ascending());
		var restaurantPage = restaurantService.list(pageable);
		return ResponseEntity.ok(PageResponse.of(restaurantPage, RestaurantListItemResponse::from));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RestaurantResponse> getById(@PathVariable Long id) {
		Restaurant restaurant = restaurantService.getById(id);
		return ResponseEntity.ok(RestaurantResponse.from(restaurant));
	}

	@PostMapping
	public ResponseEntity<RestaurantCreateResponse> create(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody CreateRestaurantRequest request) {
		Restaurant restaurant = restaurantService.create(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(RestaurantCreateResponse.from(restaurant));
	}

	@PutMapping("/{id}")
	public ResponseEntity<RestaurantResponse> update(
			@AuthenticationPrincipal Long userId,
			@PathVariable Long id,
			@RequestBody UpdateRestaurantRequest request) {
		Restaurant restaurant = restaurantService.update(id, request, userId);
		return ResponseEntity.ok(RestaurantResponse.from(restaurant));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
		restaurantService.delete(id, userId);
		return ResponseEntity.noContent().build();
	}
}
