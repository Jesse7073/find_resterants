package com.findrestaurants.backend.restaurant.dto;

/**
 * PUT /api/v1/restaurants/{id} 的請求 body，格式同 {@link CreateRestaurantRequest} 但皆為選填：
 * 未帶（null）的欄位維持原值，僅更新有帶值的欄位（見 Restaurant#applyUpdate）。
 */
public record UpdateRestaurantRequest(
		String name,
		String address,
		String category,
		String note) {
}
