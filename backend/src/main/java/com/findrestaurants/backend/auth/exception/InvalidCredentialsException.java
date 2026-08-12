package com.findrestaurants.backend.auth.exception;

/** 登入帳號或密碼錯誤，對應錯誤碼 INVALID_CREDENTIALS（見 docs/phase3-api-spec.md）。 */
public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("帳號或密碼錯誤");
	}
}
