package com.findrestaurants.backend.restaurant.dto;

import com.findrestaurants.backend.restaurant.entity.Restaurant;
import java.util.List;

/**
 * POST /restaurants 的回應格式：{@link RestaurantResponse} 的欄位 + `duplicateCandidates`。
 * 相似餐廳偵測邏輯是 T-04 的範圍，這裡固定回傳空陣列，只先把欄位加上維持 API 形狀穩定給 T-04 接手。
 */
public record RestaurantCreateResponse(
		Long id,
		String name,
		String address,
		String category,
		String note,
		Long createdBy,
		boolean isDeleted,
		List<Object> duplicateCandidates) {

	public static RestaurantCreateResponse from(Restaurant restaurant) {
		return new RestaurantCreateResponse(
				restaurant.getId(),
				restaurant.getName(),
				restaurant.getAddress(),
				restaurant.getCategory(),
				restaurant.getNote(),
				restaurant.getCreatedBy(),
				restaurant.isDeleted(),
				List.of());
	}
}
