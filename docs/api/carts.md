# Carts API

## 상품 담기

```http
POST /api/carts/items
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
  "productId": 10,
  "quantity": 2
}
```

### Response

```json
{
  "success": true,
  "data": {
    "cartItemId": 1,
    "productId": 10,
    "poductName": "무선 키보드",
    "price": 30000,
    "quantity": 2,
    "totalAmount": 60000
  },
  "message": "장바구니 상품 담기 성공"
}
```

### Errors

- `400 INVALID_QUANTITY`: 수량이 1보다 작음
- `400 OUT_OF_STOCK`: 재고 부족
- `401 UNAUTHORIZED`: 인증 실패
- `404 PRODUCT_NOT_FOUND`: 상품 정보 없음
## 장바구니 조회

```http
GET /api/carts
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
  "data": {
    "items": [
      {
        "cartItemId": 1,
        "productId": 1,
        "productName": "나이키 에어맥스",
        "price": 15000,
        "quantity": 2,
        "createdAt": "2026-04-27T15:19:52.026195",
        "updatedAt": "2026-04-27T15:19:52.026195"
      }
    ],
    "totalAmount": 30000
  },
  "message": "장바구니 조회 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 실패
## 장바구니 수량 변경

```http
PATCH /api/carts/items/{cartItemId}
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
  "quantity": 3
}
```

### Response

```json
{
  "success": true,
  "data": {
    "cartItemId": 1,
    "productId": 1,
    "productName": "나이키 에어맥스",
    "price": 159000,
    "quantity": 3,
    "totalAmount" : 477000
  },
  "message": "장바구니 수량 변경 성공"
}
```

### Errors

- `400 INVALID_QUANTITY`: 수량이 1보다 작음
- `400 OUT_OF_STOCK`: 변경할 수량이 재고 초과
- `401 UNAUTHORIZED`: 인증 실패
- `403 FORBIDDEN`: 본인의 장바구니 상품 아님
- `404 CART_ITEM_NOT_FOUND`: 장바구니 상품 없음
## 장바구니 상품 개별 삭제

```http
DELETE /api/carts/items/{cartItemId}
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
  "message": "장바구니 상품 삭제 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 실패
- `403 FORBIDDEN`: 본인의 장바구니 상품 아님
- `404 CART_ITEM_NOT_FOUND`: 장바구니 상품 없음

