-- 더미 사용자 (비밀번호: password123)
INSERT INTO members (email, password_hash, name, phone_number, point_balance, member_ship, created_at, updated_at) 
VALUES ('user1@example.com', '$2a$10$xmpKJFDRfZlMiGvh8Burj..7wMZjiIJWvbnjo9bSFgyDjUVOdlACu', '홍길동', '010-1111-1111', 10000, 'NORMAL', NOW(), NOW());

-- 더미 상품
INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (1, 'BOOK', '클린 코드', '개발자 필독서', 30000, 10, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (2, 'DIGITAL', '로지텍 무선 마우스', '로지텍 무선 마우스', 25000, 20, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (3, 'ACCESSORY', '멋쟁이 사자 키링', '귀여운 사자 인형 키링', 15000, 50, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (4, 'ACCESSORY', '가죽 키링', '고급스러운 가죽 키링', 1800, 30, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (5, 'DIGITAL', '블루투스 헤드폰', '노이즈 캔슬링 무선 헤드폰', 150000, 15, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (6, 'FASHION', '캐주얼 후드티', '편안한 오버핏 후드티', 45000, 40, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (7, 'FASHION', '바람막이 자켓', '생활 방수 바람막이', 60000, 25, 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO products (id, category_code, name, description, price, stock_quantity, sale_status, created_at, updated_at)
VALUES (8, 'DIGITAL', '기계식 키보드', '청축 기계식 키보드', 85000, 10, 'ON_SALE', NOW(), NOW());
