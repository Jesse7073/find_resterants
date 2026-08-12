package com.findrestaurants.backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** 對應 users 資料表（見 db/migration/V1__init_schema.sql），密碼一律以 bcrypt 雜湊儲存。 */
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected User() {
		// JPA 需要無參數建構子
	}

	public User(String email, String passwordHash) {
		this.email = email;
		this.passwordHash = passwordHash;
	}

	/**
	 * created_at 是 NOT NULL 欄位，但 Hibernate 的 INSERT 預設會把所有對應欄位一起送出（即使值是 null），
	 * 不會自動讓資料庫的 DEFAULT now() 生效；沒有這個 callback 的話存檔會因為 NOT NULL 違反而丟
	 * DataIntegrityViolationException，並被 AuthService 誤判成 email 重複。
	 */
	@PrePersist
	private void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
