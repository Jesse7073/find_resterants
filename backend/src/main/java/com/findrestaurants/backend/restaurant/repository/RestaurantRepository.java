package com.findrestaurants.backend.restaurant.repository;

import com.findrestaurants.backend.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

	/** 一般瀏覽/搜尋（GET /restaurants）預設排除已下架餐廳，見 docs/phase3-db-schema.md 刪除規則。 */
	Page<Restaurant> findByDeletedAtIsNull(Pageable pageable);
}
