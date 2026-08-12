package com.findrestaurants.backend.auth.exception;

/** 註冊時 email 已被使用，對應錯誤碼 DUPLICATE_EMAIL（見 docs/phase3-api-spec.md）。 */
public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException(String email) {
		super("此 email 已被註冊: " + email);
	}
}
