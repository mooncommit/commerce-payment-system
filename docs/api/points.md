# Points API

## 포인트 잔액 조회

```http
GET /api/points/balance
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
    "memberId": 1,
    "pointBalance": 5000
  },
  "message": "포인트 잔액 조회 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 실패
- `404 MEMBER_NOT_FOUND`: 회원 정보 없음
## 포인트 거래 내역 조회

```http
GET /api/points/history?page=0&size=10
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
        "pointId": 1,
        "pointType": "EARN",
        "amount": 1000,
        "balanceAfter": 5000,
        "reason": "주문 적립",
        "createdAt": "2026-06-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "message": "포인트 거래내역 조회 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증 실패
- `404 MEMBER_NOT_FOUND`: 회원 정보 없음

