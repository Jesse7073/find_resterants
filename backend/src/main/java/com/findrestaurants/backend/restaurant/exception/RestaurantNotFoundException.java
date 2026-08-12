package com.findrestaurants.backend.restaurant.exception;

/** 指定 id 的餐廳不存在，對應錯誤碼 RESTAURANT_NOT_FOUND（見 docs/phase3-api-spec.md）。 */
public class RestaurantNotFoundException extends RuntimeException {

	public RestaurantNotFoundException(Long id) {
		super("找不到餐廳: " + id);
	}
}
