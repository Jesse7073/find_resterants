package com.findrestaurants.backend.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

/** POST /api/v1/restaurants 的請求 body；`category`/`note` 為選填（見 restaurants 表定義，皆為 NULLABLE）。 */
public record CreateRestaurantRequest(

		@NotBlank(message = "name 不可為空")
		String name,

		@NotBlank(message = "address 不可為空")
		String address,

		String category,

		String note) {
}
