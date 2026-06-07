# Common API

## Base URL

```text
http://localhost:8080
```

## 인증

인증이 필요한 API는 `Authorization` 헤더에 Bearer token을 전달한다.

```http
Authorization: Bearer {accessToken}
```

## 성공 응답

```json
{
  "success": true,
  "data": {},
  "message": "처리 성공"
}
```

## 실패 응답

```json
{
  "success": false,
  "error": {
    "code": "PAYMENT_001",
    "message": "결제 정보를 찾을 수 없습니다."
  }
}
```

## 페이지 응답

```json
{
  "content": [],
  "page": 1,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0
}
```

## 주요 Enum

| Enum | Values |
| --- | --- |
| `MemberShip` | `NORMAL`, `VIP`, `VVIP` |
| `OrderStatus` | `PAYMENT_PENDING`, `COMPLETED`, `CANCELED` |
| `PaymentStatus` | `PENDING`, `COMPLETED`, `FAILED`, `CANCELED`, `REFUNDED` |
| `PaymentMethodType` | `CARD`, `POINT_ONLY`, `CARD_AND_POINT` |
| `PointType` | `EARN`, `USE`, `REFUND`, `REVOKE` |
| `RefundStatus` | `REQUESTED`, `COMPLETED`, `FAILED` |

## 주요 에러 코드

| Prefix | 도메인 |
| --- | --- |
| `TOKEN_` | JWT 인증 |
| `COMMON_` | 공통 |
| `MEMBER_`, `AUTH_` | 회원/인증 |
| `PRODUCT_` | 상품 |
| `CART_` | 장바구니 |
| `ORDER_` | 주문 |
| `PAYMENT_` | 결제 |
| `POINT_` | 포인트 |
| `REFUND_` | 환불 |
| `WEBHOOK_` | 웹훅 |
