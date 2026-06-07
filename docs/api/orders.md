# Orders API

## 상품 바로 주문 생성

```http
POST /api/orders
Authorization: Bearer {accessToken}
```

### Request

```json
{
  "productId": 1,
  "quantity": 2,
  "usePointAmount": 1000
}
```

### Response

```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20260606-0001",
    "paymentId": 1,
    "portonePaymentId": "pay_xxx",
    "orderStatus": "PAYMENT_PENDING",
    "paymentStatus": "PENDING",
    "totalAmount": 20000,
    "usedPointAmount": 1000,
    "pgAmount": 19000,
    "items": []
  },
  "message": "상품 바로 주문 및 결제 대기 생성 성공"
}
```

## 장바구니 주문 생성

```http
POST /api/carts/orders
Authorization: Bearer {accessToken}
```

### Request

```json
{
  "cartItemIds": [1, 2],
  "usePointAmount": 1000
}
```

### Response

`OrderCreateResponse`와 동일하다.
