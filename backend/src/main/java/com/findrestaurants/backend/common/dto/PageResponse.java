package com.findrestaurants.backend.common.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * 通用分頁回應格式，供各模組列表端點共用（見 docs/phase3-modules.md，`common` 模組負責「分頁工具」）。
 * T-03 為第一個使用者（`GET /restaurants`），後續模組列表端點應直接重用，不要各自發明分頁格式。
 */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {

	public static <T, R> PageResponse<R> of(Page<T> page, Function<T, R> mapper) {
		return new PageResponse<>(
				page.getContent().stream().map(mapper).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}
}
