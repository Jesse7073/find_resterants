package com.findrestaurants.backend.restaurant.dto;

import com.findrestaurants.backend.restaurant.entity.Restaurant;

/**
 * GET /restaurants/{id}、PUT /restaurants/{id} 的回應格式，對應 docs/phase3-api-spec.md 本票範圍：
 * {id, name, address, category, note, createdBy, isDeleted}。`isFavorited`/`publicTags` 依賴
 * favorite/tag 模組（T-05、T-07），本票尚未建立那些資料表，刻意不在此回應中加入，避免回傳假資料誤導前端。
 */
public record RestaurantResponse(
		Long id,
		String name,
		String address,
		String category,
		String note,
		Long createdBy,
		boolean isDeleted) {

	public static RestaurantResponse from(Restaurant restaurant) {
		return new RestaurantResponse(
				restaurant.getId(),
				restaurant.getName(),
				restaurant.getAddress(),
				restaurant.getCategory(),
				restaurant.getNote(),
				restaurant.getCreatedBy(),
				restaurant.isDeleted());
	}
}
