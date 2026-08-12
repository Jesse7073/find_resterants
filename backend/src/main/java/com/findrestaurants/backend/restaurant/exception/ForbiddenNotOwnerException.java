package com.findrestaurants.backend.restaurant.exception;

/**
 * 非餐廳的 `createdBy` 本人嘗試編輯/刪除，對應錯誤碼 FORBIDDEN_NOT_OWNER（見 docs/phase3-api-spec.md）。
 * 依「認證授權共通規則」，擁有權比對一律以 Service 層依 JWT 中的 user id 與資料庫紀錄比對，不信任前端傳入的值。
 */
public class ForbiddenNotOwnerException extends RuntimeException {

	public ForbiddenNotOwnerException() {
		super("僅原始新增者可編輯此餐廳");
	}
}
