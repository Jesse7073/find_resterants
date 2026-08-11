-- 對應 docs/phase3-db-schema.md

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    category VARCHAR(100),
    note TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_restaurants_name ON restaurants(name);
CREATE INDEX idx_restaurants_address ON restaurants(address);

CREATE TABLE favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, restaurant_id)
);

CREATE TABLE private_tags (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, name)
);

CREATE TABLE favorite_tags (
    id BIGSERIAL PRIMARY KEY,
    favorite_id BIGINT NOT NULL REFERENCES favorites(id) ON DELETE CASCADE,
    private_tag_id BIGINT NOT NULL REFERENCES private_tags(id) ON DELETE CASCADE,
    UNIQUE (favorite_id, private_tag_id)
);

-- 公開標籤：僅由開發者透過 migration 種子資料維護（見 V2），不開放使用者新增，故無 created_by 欄位
CREATE TABLE public_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE restaurant_public_tags (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    public_tag_id BIGINT NOT NULL REFERENCES public_tags(id) ON DELETE CASCADE,
    applied_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, public_tag_id)
);

CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    favorite_id BIGINT NOT NULL UNIQUE REFERENCES favorites(id) ON DELETE CASCADE,
    content TEXT,
    rating SMALLINT CHECK (rating BETWEEN 1 AND 5),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
