# Orders API

## 상품 바로 주문 생성

```http
POST /api/orders
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
  "orderItems": [
    {
      "productId": 10,
      "quantity": 2
    }
  ]
}
```

### Response

```json
{
  "success": true,
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "totalAmount": 60000,
    "orderStatus": "PENDING_PAYMENT",
    "paymentId": 200,
    "createdAt": "2026-06-01T12:30:00"
  },
  "message": "주문 생성 성공"
}
```

### Errors

- `400 OUT_OF_STOCK`: 상품 재고 부족
- `400 POINT_BALANCE_NOT_ENOUGH`: 보유 포인트보다 많은 금액 사용 시도
- `401 UNAUTHORIZED`: 인증 실패
- `404 PRODUCT_NOT_FOUND`: 상품 정보 없음
## 장바구니 주문 생성

```http
POST /api/carts/orders
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
  "cartItemIds": [1, 2, 3]
}
```

### Response

```json
{
  "success": true,
  "data": {
    "orderId": 101,
    "orderNumber": "ORD-20260601-000002",
    "totalAmount": 120000,
    "orderStatus": "PENDING_PAYMENT",
    "paymentId": 201,
    "createdAt": "2026-06-01T13:00:00"
  },
  "message": "장바구니 주문 생성 성공"
}
```

### Errors

- `400 OUT_OF_STOCK`: 장바구니 상품 재고 부족
- `401 UNAUTHORIZED`: 인증 실패
- `404 CART_ITEM_NOT_FOUND`: 유효하지 않은 장바구니 아이템
## 주문서 미리보기

```http
GET /api/orders/preview
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
        "productId": 10,
        "productName": "무선 키보드",
        "unitPrice": 30000,
        "quantity": 2,
        "lineTotalAmount": 60000
      }
    ],
    "totalOrderAmount": 60000,
    "pointBalance": 5000
  },
  "message": "주문서 미리보기 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 실패
## 주문 내역 조회

```http
GET /api/orders?page=0&size=10
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
    "content": [
      {
        "orderId": 100,
        "orderNumber": "ORD-20260601-000001",
        "orderStatus": "PAID",
        "totalAmount": 60000,
        "usedPointAmount": 5000,
        "pgAmount": 55000,
        "createdAt": "2026-06-01T12:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "message": "주문 내역 조회 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 실패
## 주문 상세 조회

```http
GET /api/orders/{orderId}
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
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "orderStatus": "PAID",
    "totalAmount": 60000,
    "usedPointAmount": 5000,
    "pgAmount": 55000,
    "createdAt": "2026-06-01T12:30:00",
    "paidAt": "2026-06-01T12:35:00",
    "canceledAt": null,
    "payment": {
      "paymentId": 200,
      "paymentStatus": "PAID",
      "paymentMethodType": "CARD_AND_POINT",
      "portonePaymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc",
      "completedAt": "2026-06-01T12:35:00"
    },
    "pointSummary": {
      "usedPointAmount": 5000,
      "earnedPointAmount": 550
    },
    "items": [
      {
        "orderItemId": 1,
        "productId": 10,
        "productName": "무선 키보드",
        "unitPrice": 30000,
        "quantity": 2,
        "lineTotalAmount": 60000
      }
    ]
  },
  "message": "주문 상세 조회 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 실패
- `404 ORDER_NOT_FOUND`: 해당 주문 없음
## 결제대기 주문 취소

```http
PATCH /api/orders/{orderId}/status
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
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "orderStatus": "CANCELED",
    "paymentId": 200,
    "paymentStatus": "FAILED",
    "restoredItems": [
      {
        "productId": 10,
        "productName": "무선 키보드",
        "restoredQuantity": 2
      }
    ],
    "canceledAt": "2026-06-01T13:00:00"
  },
  "message": "주문 취소 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 실패
- `403 FORBIDDEN_ORDER`: 본인의 주문이 아님
- `404 ORDER_NOT_FOUND`: 해당 주문 없음
- `409 ORDER_NOT_CANCELABLE`: 결제대기 상태가 아니어 취소 불가

