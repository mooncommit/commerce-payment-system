# Points API

## 포인트 잔액 조회

```http
GET /api/points/balance
Authorization: Bearer {accessToken}
```

### Response

```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "pointBalance": 10000
  },
  "message": "포인트 잔액 조회 성공"
}
```

## 포인트 거래 내역 조회

```http
GET /api/points/history?page=1&size=10
Authorization: Bearer {accessToken}
```

### Response

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "paymentId": 1,
        "type": "EARN",
        "amount": 100,
        "reason": "주문 적립",
        "createdAt": "2026-06-06T10:00:00"
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "message": "포인트 거래내역 조회 성공"
}
```
