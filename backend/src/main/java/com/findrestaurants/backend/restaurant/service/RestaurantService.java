package com.findrestaurants.backend.restaurant.service;

import com.findrestaurants.backend.restaurant.dto.CreateRestaurantRequest;
import com.findrestaurants.backend.restaurant.dto.UpdateRestaurantRequest;
import com.findrestaurants.backend.restaurant.entity.Restaurant;
import com.findrestaurants.backend.restaurant.exception.ForbiddenNotOwnerException;
import com.findrestaurants.backend.restaurant.exception.RestaurantNotFoundException;
import com.findrestaurants.backend.restaurant.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 餐廳 CRUD 商業邏輯（T-03 範圍）：擁有權比對、軟刪除、分頁列表排除已下架餐廳。
 * 複製文字解析、相似餐廳偵測（T-04）與關鍵字/標籤複合搜尋（T-09）不在本服務範圍內。
 */
@Service
public class RestaurantService {

	private final RestaurantRepository restaurantRepository;

	public RestaurantService(RestaurantRepository restaurantRepository) {
		this.restaurantRepository = restaurantRepository;
	}

	@Transactional(readOnly = true)
	public Page<Restaurant> list(Pageable pageable) {
		return restaurantRepository.findByDeletedAtIsNull(pageable);
	}

	/** 已下架的餐廳仍可查詢到（isDeleted:true），不在此過濾，見 docs/phase3-api-spec.md。 */
	@Transactional(readOnly = true)
	public Restaurant getById(Long id) {
		return restaurantRepository.findById(id)
				.orElseThrow(() -> new RestaurantNotFoundException(id));
	}

	@Transactional
	public Restaurant create(CreateRestaurantRequest request, Long userId) {
		Restaurant restaurant = new Restaurant(
				request.name(), request.address(), request.category(), request.note(), userId);
		return restaurantRepository.save(restaurant);
	}

	@Transactional
	public Restaurant update(Long id, UpdateRestaurantRequest request, Long userId) {
		Restaurant restaurant = getById(id);
		requireOwner(restaurant, userId);
		restaurant.applyUpdate(request.name(), request.address(), request.category(), request.note());
		return restaurant;
	}

	@Transactional
	public void delete(Long id, Long userId) {
		Restaurant restaurant = getById(id);
		requireOwner(restaurant, userId);
		restaurant.markDeleted();
	}

	/** 擁有權比對一律以資料庫紀錄的 createdBy 與 JWT 解出的 userId 比對，不信任前端傳入的值。 */
	private void requireOwner(Restaurant restaurant, Long userId) {
		if (!restaurant.getCreatedBy().equals(userId)) {
			throw new ForbiddenNotOwnerException();
		}
	}
}
