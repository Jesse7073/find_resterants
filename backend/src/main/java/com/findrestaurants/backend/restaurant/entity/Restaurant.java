package com.findrestaurants.backend.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 對應 restaurants 資料表（見 db/migration/V1__init_schema.sql）。`createdBy` 僅存 users.id（不建立
 * @ManyToOne 關聯到 auth 模組的 User entity），依 docs/phase3-modules.md 的模組邊界規則，`restaurant`
 * 不應反向依賴 `auth` 的 entity 型別，只需要 id 做擁有權比對。
 */
@Entity
@Table(name = "restaurants")
public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "category")
	private String category;

	@Column(name = "note")
	private String note;

	@Column(name = "created_by", nullable = false, updatable = false)
	private Long createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected Restaurant() {
		// JPA 需要無參數建構子
	}

	public Restaurant(String name, String address, String category, String note, Long createdBy) {
		this.name = name;
		this.address = address;
		this.category = category;
		this.note = note;
		this.createdBy = createdBy;
	}

	/**
	 * created_at/updated_at 皆為 NOT NULL 但沒有 DB 端自動賦值（見 db/migration/V1__init_schema.sql），
	 * Hibernate 的 INSERT 預設會把所有對應欄位一起送出，若不在這裡賦值會因 NOT NULL 違反丟
	 * DataIntegrityViolationException（T-02 的 User entity 已踩過這個坑，這裡不重蹈覆轍）。
	 */
	@PrePersist
	private void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	private void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 僅更新有帶值（非 null）的欄位，未帶的欄位維持原值，對應 PUT /restaurants/{id} 「只更新有帶的欄位」的語意。
	 */
	public void applyUpdate(String name, String address, String category, String note) {
		if (name != null) {
			this.name = name;
		}
		if (address != null) {
			this.address = address;
		}
		if (category != null) {
			this.category = category;
		}
		if (note != null) {
			this.note = note;
		}
	}

	public void markDeleted() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getCategory() {
		return category;
	}

	public String getNote() {
		return note;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}
}
