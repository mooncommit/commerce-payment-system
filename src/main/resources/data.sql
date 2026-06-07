-- 더미 사용자 (비밀번호: password123)
INSERT INTO members (email, password_hash, name, phone_number, point_balance, member_ship, created_at, updated_at) 
VALUES ('user1@example.com', '$2a$10$xmpKJFDRfZlMiGvh8Burj..7wMZjiIJWvbnjo9bSFgyDjUVOdlACu', '홍길동', '010-1111-1111', 10000, 'NORMAL', NOW(), NOW());

-- 더미 상품
INSERT INTO products (category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at) 
VALUES ('BOOK', '클린 코드', '개발자 필독서', 30000, 10, 'ON_SALE', NOW(), NOW());

INSERT INTO products (category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at) 
VALUES ('DIGITAL', '무선 마우스', '로지텍 무선 마우스', 25000, 20, 'ON_SALE', NOW(), NOW());
