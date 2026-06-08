-- 더미 사용자 (비밀번호: password123)
INSERT IGNORE INTO members (email, password_hash, name, phone_number, point_balance, member_ship, created_at, updated_at)
VALUES ('user1@example.com', '$2a$10$xmpKJFDRfZlMiGvh8Burj..7wMZjiIJWvbnjo9bSFgyDjUVOdlACu', '홍길동', '010-1111-1111', 10000, 'NORMAL', NOW(), NOW());

-- 더미 상품
INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (1, 'BOOK', '클린 코드', '개발자 필독서', 30000, 10, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (2, 'DIGITAL', '로지텍 무선 마우스', '로지텍 무선 마우스', 25000, 20, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (3, 'ACCESSORY', '멋쟁이 사자 키링', '귀여운 사자 인형 키링', 1000, 50, 'ON_SALE', NOW(), NOW());
