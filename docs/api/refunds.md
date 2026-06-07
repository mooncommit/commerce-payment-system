# Refunds API

## 결제 환불 요청

### 01. 설명

결제 완료된 결제 건을 전체 환불하는 API입니다.

본인이 결제한 결제 건만 환불할 수 있습니다. 결제 상태가 `COMPLETED`인 결제만 환불할 수 있으며, 이미 환불 완료된 결제 건은 다시 환불할 수 없습니다. 현재 부분 환불은 지원하지 않습니다.

환불 요청이 들어오면 서버는 먼저 `Refund` 이력을 `REQUESTED` 상태로 저장합니다. 이후 PortOne 결제 취소 API를 호출하고, PG 취소가 성공하면 내부 후처리를 수행합니다.

환불 후처리에서는 결제 상태를 `REFUNDED`, 주문 상태를 `CANCELED`로 변경합니다. 주문 상품 전체 수량만큼 재고를 복구하고, 결제할 때 사용한 포인트를 돌려주며, 결제 완료로 적립된 포인트를 회수합니다. 마지막으로 환불 이력을 `COMPLETED` 상태로 변경합니다.

PortOne 결제 취소 API 호출에 실패하면 환불 이력을 `FAILED` 상태로 저장하고, 결제와 주문 상태는 변경하지 않습니다.

카드 결제 금액이 없거나 결제 수단이 `POINT_ONLY`인 결제는 PortOne 결제 취소 API를 호출하지 않고 내부 환불 후처리만 수행합니다.

### 02. 요청(Request)

#### a. Parameter & Querystring & URL

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| paymentId | Long | 필수 | 전체 환불할 결제 ID |

요청 예시:

```http
POST /api/payments/200/refunds
```

#### b. Request Headers

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| Authorization | String | 필수 | Bearer token 형식의 JWT 인증 토큰 |
| Content-Type | String | 필수 | `application/json` |

#### c. Request Body

```json
{
  "reason": "전체 주문 환불 요청"
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| reason | String | 필수 | 환불 사유 |

### 03. 응답(Response)

#### a. Response Headers

| 이름 | 데이터타입 | 설명 |
| --- | --- | --- |
| Content-Type | String | `application/json` |

#### b. Response Body

성공 응답: `200 OK`

```json
{
  "success": true,
  "data": {
    "refundId": 5001,
    "paymentId": 200,
    "orderId": 100,
    "refundPgAmount": 55000,
    "refundPointAmount": 5000,
    "refundStatus": "COMPLETED",
    "orderStatus": "CANCELED",
    "paymentStatus": "REFUNDED",
    "reason": "전체 주문 환불 요청",
    "createdAt": "2026-06-01T15:30:00",
    "refundedAt": "2026-06-01T15:30:05"
  },
  "message": "전체 환불 성공"
}
```

| Key | 데이터타입 | 설명 |
| --- | --- | --- |
| refundId | Long | 환불 ID |
| paymentId | Long | 환불 대상 결제 ID |
| orderId | Long | 환불 대상 주문 ID |
| refundPgAmount | Long | PG 환불 금액 |
| refundPointAmount | Long | 복구할 포인트 금액 |
| refundStatus | String | 환불 상태. 성공 시 `COMPLETED` |
| orderStatus | String | 환불 후 주문 상태. 성공 시 `CANCELED` |
| paymentStatus | String | 환불 후 결제 상태. 성공 시 `REFUNDED` |
| reason | String | 환불 사유 |
| createdAt | String | 환불 요청 생성 일시 |
| refundedAt | String | 환불 완료 일시 |

#### c. Error Response

| HTTP Status | Error Code | 설명 |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 요청 형식이 잘못되었거나 환불 사유가 비어 있음 |
| 400 | `INVALID_REFUND_STATUS` | 결제 완료 상태가 아니어서 환불할 수 없음 |
| 401 | `TOKEN_001` ~ `TOKEN_005` | 인증 토큰이 없거나 유효하지 않음 |
| 404 | `PAYMENT_NOT_FOUND` | 결제 정보를 찾을 수 없음. 본인 결제 건이 아닌 경우도 포함 |
| 404 | `REFUND_NOT_FOUND` | 환불 이력을 찾을 수 없음 |
| 500 | `PG_CANCEL_FAILED` | PortOne 결제 취소 요청 실패 |

### 04. 비즈니스 규칙

- 로그인한 회원만 호출할 수 있습니다.
- 본인의 결제 건만 환불할 수 있습니다.
- 결제 상태가 `COMPLETED`인 결제 건만 환불할 수 있습니다.
- 이미 환불 완료된 결제 건은 다시 환불할 수 없습니다.
- 부분 환불은 지원하지 않습니다.
- 환불 금액은 클라이언트가 입력하지 않습니다.
- 서버가 주문과 결제 정보를 기준으로 전체 환불 금액을 계산합니다.
- 환불 요청 시 `Refund` 이력을 먼저 `REQUESTED` 상태로 저장합니다.
- 카드 결제 금액이 있으면 PortOne 결제 취소 API를 호출합니다.
- 포인트 전액 결제라면 PortOne 결제 취소 API를 호출하지 않습니다.
- PG 취소 성공 시 주문 상태는 `CANCELED`로 변경합니다.
- PG 취소 성공 시 결제 상태는 `REFUNDED`로 변경합니다.
- PG 취소 성공 시 주문 상품 전체 수량만큼 재고를 복구합니다.
- PG 취소 성공 시 결제할 때 사용한 포인트를 다시 돌려줍니다.
- PG 취소 성공 시 결제 완료로 적립된 포인트를 다시 회수합니다.
- 내부 후처리까지 성공하면 환불 상태를 `COMPLETED`로 변경합니다.
- PG 취소에 실패하면 환불 상태를 `FAILED`로 저장하고, 결제와 주문 상태는 변경하지 않습니다.
