package com.findrestaurants.backend.restaurant.dto;

import com.findrestaurants.backend.restaurant.entity.Restaurant;

/**
 * GET /restaurants 分頁列表中每一筆的回應格式。`isFavorited` 依賴 favorite 模組（T-05 才會建立
 * favorites 資料表），本票固定回傳 false，待 T-05 完成後由該票接手改為實際查詢結果。
 */
public record RestaurantListItemResponse(
		Long id,
		String name,
		String address,
		String category,
		String note,
		Long createdBy,
		boolean isDeleted,
		boolean isFavorited) {

	public static RestaurantListItemResponse from(Restaurant restaurant) {
		return new RestaurantListItemResponse(
				restaurant.getId(),
				restaurant.getName(),
				restaurant.getAddress(),
				restaurant.getCategory(),
				restaurant.getNote(),
				restaurant.getCreatedBy(),
				restaurant.isDeleted(),
				false);
	}
}
