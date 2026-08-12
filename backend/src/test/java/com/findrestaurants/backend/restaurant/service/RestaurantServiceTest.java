package com.findrestaurants.backend.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.findrestaurants.backend.restaurant.dto.CreateRestaurantRequest;
import com.findrestaurants.backend.restaurant.dto.UpdateRestaurantRequest;
import com.findrestaurants.backend.restaurant.entity.Restaurant;
import com.findrestaurants.backend.restaurant.exception.ForbiddenNotOwnerException;
import com.findrestaurants.backend.restaurant.exception.RestaurantNotFoundException;
import com.findrestaurants.backend.restaurant.repository.RestaurantRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class RestaurantServiceTest {

	private RestaurantRepository restaurantRepository;
	private RestaurantService restaurantService;

	@BeforeEach
	void setUp() {
		restaurantRepository = mock(RestaurantRepository.class);
		restaurantService = new RestaurantService(restaurantRepository);
	}

	@Test
	void list_delegatesToFindByDeletedAtIsNull_excludingSoftDeletedRestaurants() {
		Pageable pageable = PageRequest.of(0, 20);
		Restaurant active = newRestaurantWithId(1L, "現存餐廳", "地址", 1L);
		Page<Restaurant> page = new PageImpl<>(List.of(active), pageable, 1);
		when(restaurantRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);

		Page<Restaurant> result = restaurantService.list(pageable);

		assertThat(result.getContent()).containsExactly(active);
		verify(restaurantRepository).findByDeletedAtIsNull(pageable);
	}

	@Test
	void getById_deletedRestaurant_stillReturned_notFilteredOut() {
		Restaurant deleted = newRestaurantWithId(1L, "已下架餐廳", "地址", 1L);
		deleted.markDeleted();
		when(restaurantRepository.findById(1L)).thenReturn(Optional.of(deleted));

		Restaurant result = restaurantService.getById(1L);

		assertThat(result.isDeleted()).isTrue();
	}

	@Test
	void getById_missing_throwsRestaurantNotFoundException() {
		when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> restaurantService.getById(99L))
				.isInstanceOf(RestaurantNotFoundException.class);
	}

	@Test
	void create_savesRestaurantWithCreatedByFromAuthenticatedUser() {
		CreateRestaurantRequest request = new CreateRestaurantRequest("店名", "地址", "category", "note");
		when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Restaurant result = restaurantService.create(request, 42L);

		assertThat(result.getName()).isEqualTo("店名");
		assertThat(result.getCreatedBy()).isEqualTo(42L);
	}

	@Test
	void update_byOwner_appliesOnlyProvidedFields() {
		Restaurant restaurant = newRestaurantWithId(1L, "舊名字", "舊地址", 7L);
		when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

		Restaurant result = restaurantService.update(
				1L, new UpdateRestaurantRequest("新名字", null, null, null), 7L);

		assertThat(result.getName()).isEqualTo("新名字");
		assertThat(result.getAddress()).isEqualTo("舊地址");
	}

	@Test
	void update_byNonOwner_throwsForbiddenNotOwnerException() {
		Restaurant restaurant = newRestaurantWithId(1L, "店名", "地址", 7L);
		when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

		assertThatThrownBy(() -> restaurantService.update(
				1L, new UpdateRestaurantRequest("新名字", null, null, null), 999L))
				.isInstanceOf(ForbiddenNotOwnerException.class);
		assertThat(restaurant.getName()).isEqualTo("店名"); // 未被非本人竄改
	}

	@Test
	void update_missingRestaurant_throwsRestaurantNotFoundException() {
		when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> restaurantService.update(
				99L, new UpdateRestaurantRequest("新名字", null, null, null), 1L))
				.isInstanceOf(RestaurantNotFoundException.class);
	}

	@Test
	void delete_byOwner_softDeletesRestaurant() {
		Restaurant restaurant = newRestaurantWithId(1L, "店名", "地址", 7L);
		when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

		restaurantService.delete(1L, 7L);

		assertThat(restaurant.isDeleted()).isTrue();
		assertThat(restaurant.getDeletedAt()).isNotNull();
	}

	@Test
	void delete_byNonOwner_throwsForbiddenNotOwnerException_andDoesNotDelete() {
		Restaurant restaurant = newRestaurantWithId(1L, "店名", "地址", 7L);
		when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

		assertThatThrownBy(() -> restaurantService.delete(1L, 999L))
				.isInstanceOf(ForbiddenNotOwnerException.class);
		assertThat(restaurant.isDeleted()).isFalse();
	}

	@Test
	void delete_missingRestaurant_throwsRestaurantNotFoundException() {
		when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> restaurantService.delete(99L, 1L))
				.isInstanceOf(RestaurantNotFoundException.class);
	}

	private static Restaurant newRestaurantWithId(Long id, String name, String address, Long createdBy) {
		return newRestaurantWithId(id, name, address, "category", createdBy);
	}

	private static Restaurant newRestaurantWithId(Long id, String name, String address, String category, Long createdBy) {
		try {
			Restaurant restaurant = new Restaurant(name, address, category, "note", createdBy);
			var idField = Restaurant.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(restaurant, id);
			return restaurant;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
