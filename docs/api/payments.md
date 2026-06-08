# Payments API

## 결제 확정

```http
POST /api/payments/confirm
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
  "paymentId": 200,
  "orderId": 100,
  "portonePaymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc",
  "amount": 55000,
  "usedPointAmount": 5000
}
```

### Response

```json
{
  "success": true,
  "data": {
    "paymentId": 200,
    "orderId": 100,
    "paymentStatus": "PAID",
    "orderStatus": "PAID",
    "pgAmount": 55000,
    "usedPointAmount": 5000,
    "earnedPointAmount": 550,
    "completedAt": "2026-06-01T12:35:00"
  },
  "message": "결제 성공"
}
```

### Errors

- `400 BAD_REQUEST`: 유효하지 않은 결제 요청
- `401 UNAUTHORIZED`: 인증 실패
- `404 ORDER_NOT_FOUND`: 해당 주문 없음
- `409 ALREADY_PAID`: 이미 결제 완료된 주문
- `502 PG_ERROR`: PG사 결제 승인 실패

