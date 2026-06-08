# Refunds API

## 전체 환불 요청

```http
POST /api/payments/{paymentId}/refunds
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
  "reason": "전체 주문 환불 요청"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "refundId": 5001,
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "paymentId": 200,
    "refundStatus": "SUCCESS",
    "orderStatus": "CANCELED",
    "paymentStatus": "REFUNDED",
    "refundTotalAmount": 60000,
    "refundPointAmount": 5000,
    "refundPgAmount": 55000,
    "revokedPointAmount": 550,
    "reason": "전체 주문 환불 요청",
    "restoredItems": [
      {
        "productId": 10,
        "productName": "무선 키보드",
        "restoredQuantity": 2
      }
    ],
    "createdAt": "2026-06-01T15:30:00",
    "refundedAt": "2026-06-01T15:30:05"
  },
  "message": "전체 환불 성공"
}
```

### Errors

- `400 INVALID_REFUND_REASON`: 환불 사유 누락
- `401 UNAUTHORIZED`: 인증 실패
- `403 FORBIDDEN_PAYMENT`: 본인 결제가 아님
- `404 PAYMENT_NOT_FOUND`: 결제 내역 없음
- `409 REFUND_NOT_ALLOWED`: 환불 가능한 상태가 아님 (결제 완료 아님)
- `409 ALREADY_REFUNDED`: 이미 환불된 결제
- `502 REFUND_PG_CANCEL_FAILED`: PG사 취소 요청 실패

