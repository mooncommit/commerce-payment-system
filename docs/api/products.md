# Products API

## 상품 목록 조회

```http
GET /api/products?page=0&size=10
```

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "category": "가전제품",
        "productName": "소니 헤드폰 WH-1000XM5",
        "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
        "price": 380000,
        "quantity": 50,
        "status": "ON_SALE",
        "createdAt": "2026-06-01T15:40:00",
        "updatedAt": "2026-06-01T15:40:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "message": "상품 목록 조회 성공"
}
```

### Errors

- `500 INTERNAL_SERVER_ERROR`: 서버 내부 오류
## 상품 단 건 조회

```http
GET /api/products/{productId}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "category": "가전제품",
    "productName": "소니 헤드폰 WH-1000XM5",
    "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
    "price": 380000,
    "quantity": 50,
    "status": "ON_SALE",
    "createdAt": "2026-06-01T15:40:00",
    "updatedAt": "2026-06-01T15:40:00"
  },
  "message": "상품 단 건 조회 성공"
}
```

### Errors

- `404 PRODUCT_NOT_FOUND`: 해당 상품이 존재하지 않음
## 상품 등록

```http
POST /api/products
```

### Headers

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

### Request

```json
{
  "category": "전자제품",
  "productName": "소니 헤드폰 WH-1000XM5",
  "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
  "price": 380000,
  "quantity": 50
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "category": "전자제품",
    "productName": "소니 헤드폰 WH-1000XM5",
    "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
    "price": 380000,
    "quantity": 50,
    "status": "ON_SALE",
    "createdAt": "2026-06-01T15:40:00",
    "updatedAt": "2026-06-01T15:40:00"
  },
  "message": "상품 등록 성공"
}
```

### Errors

- `400 BAD_REQUEST`: 필수 입력값 누락
- `401 UNAUTHORIZED`: 인증 토큰 누락/만료
- `403 FORBIDDEN`: 관리자/판매자 권한 없음
## 상품 수정

```http
PUT /api/products/{productId}
```

### Headers

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

### Request

```json
{
  "category": "가전제품",
  "productName": "소니 헤드폰 WH-1000XM5",
  "price": 395000
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "category": "가전제품",
    "productName": "소니 헤드폰",
    "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
    "price": 395000,
    "quantity": 30,
    "status": "ON_SALE",
    "createdAt": "2026-06-01T15:40:00",
    "updatedAt": "2026-06-01T16:15:00"
  },
  "message": "상품 수정 성공"
}
```

### Errors

- `400 BAD_REQUEST`: 유효하지 않은 데이터 입력
- `401 UNAUTHORIZED`: 인증 토큰 누락/만료
- `403 FORBIDDEN`: 권한 없음
- `404 PRODUCT_NOT_FOUND`: 대상 상품 없음
## 상품 삭제

```http
DELETE /api/products/{productId}
```

### Headers

```json
{
  "Authorization": "Bearer {accessToken}"
}
```

### Response

```json
{
  "success": true,
  "data": null,
  "message": "상품이 성공적으로 삭제되었습니다."
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 토큰 누락/만료
- `403 FORBIDDEN`: 권한 없음
- `404 PRODUCT_NOT_FOUND`: 대상 상품 없음

