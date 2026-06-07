# Payments API

## 결제 확정

### 01. 설명

PortOne 결제창에서 결제가 완료된 뒤, 클라이언트가 결제 결과를 서버에 확정 요청하는 API입니다.

서버는 요청받은 `portonePaymentId`로 PortOne 결제 내역을 조회하여 실제 결제 상태와 결제 금액을 검증합니다. PortOne 결제 상태가 `PAID`이고, PortOne 결제 금액과 서버 주문의 `pgAmount`가 일치하면 결제 상태를 `COMPLETED`, 주문 상태를 `COMPLETED`로 변경합니다.

검증에 실패하면 결제와 주문을 완료 처리하지 않습니다. PG 결제 금액이 서버 주문 금액과 다르면 내부 결제 상태를 `FAILED`, 주문 상태를 `CANCELED`로 변경하고 선차감된 재고를 복구한 뒤, PortOne 결제 취소 API를 호출합니다.

### 02. 요청(Request)

#### a. Parameter & Querystring & URL

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| - | - | - | URL Path 또는 Query Parameter 없음 |

#### b. Request Headers

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| Authorization | Bearer token | 필수 | JWT 인증 토큰 |
| Content-Type | application/json | 필수 | 요청 데이터 타입 |

#### c. Request Body

```json
{
  "paymentId": 200,
  "portonePaymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc"
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| paymentId | Long | 필수 | 서버에 저장된 결제 ID |
| portonePaymentId | String | 필수 | PortOne 결제창 호출에 사용한 서버 채번 결제 식별자 |

### 03. 응답(Response)

#### a. Response Headers

| 이름 | 데이터타입 | 설명 |
| --- | --- | --- |
| Content-Type | application/json | 응답 데이터 타입 |

#### b. Response Body

성공 응답: `200 OK`

```json
{
  "success": true,
  "data": {
    "paymentId": 200,
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "paymentStatus": "COMPLETED",
    "orderStatus": "COMPLETED",
    "totalAmount": 60000,
    "usedPointAmount": 5000,
    "pgAmount": 55000,
    "paidAt": "2026-06-01T15:30:00"
  },
  "message": "결제 확정 성공"
}
```

| Key | 데이터타입 | 설명 |
| --- | --- | --- |
| paymentId | Long | 결제 ID |
| orderId | Long | 주문 ID |
| orderNumber | String | 노출용 주문번호 |
| paymentStatus | String | 결제 상태. 성공 시 `COMPLETED` |
| orderStatus | String | 주문 상태. 성공 시 `COMPLETED` |
| totalAmount | Long | 주문 총액 |
| usedPointAmount | Long | 주문 생성 시 사용 요청한 포인트 금액 |
| pgAmount | Long | PG 실결제 금액 |
| paidAt | String | 결제 완료 일시 |

#### c. Error Response

| HTTP Status | Error Code | 설명 |
| --- | --- | --- |
| 400 | `PAYMENT_AMOUNT_MISMATCH` | 서버 주문 금액과 PortOne 결제 금액이 다름 |
| 400 | `INVALID_PAYMENT_STATUS` | 유효하지 않은 결제 상태 전이 |
| 400 | `PAYMENT_NOT_PAID` | PortOne 결제가 완료 상태가 아님 |
| 400 | `PAYMENT_ID_MISMATCH` | 요청한 PortOne 결제 ID가 서버 결제 정보와 일치하지 않음 |
| 401 | `TOKEN_001` ~ `TOKEN_005` | 인증 토큰이 없거나 유효하지 않음 |
| 404 | `PAYMENT_NOT_FOUND` | 결제 정보를 찾을 수 없음. 본인 결제 건이 아닌 경우도 포함 |
| 409 | `ALREADY_PROCESSED_PAYMENT` | 이미 처리된 결제 |
| 500 | `PG_CANCEL_FAILED` | 금액 불일치 보정 중 PortOne 결제 취소 요청 실패 |
| 502 | `PORTONE_API_ERROR` | PortOne 결제 조회 실패 |

### 04. 비즈니스 규칙

- 로그인한 회원만 호출할 수 있습니다.
- 본인의 결제 건만 확정할 수 있습니다.
- 서버는 `paymentId`와 `portonePaymentId`가 같은 결제 정보를 가리키는지 검증합니다.
- 결제 확정 전 서버는 PortOne API로 실제 결제 상태와 금액을 조회합니다.
- PortOne 결제 상태가 `PAID`가 아니면 결제를 확정하지 않습니다.
- PortOne 결제 금액과 서버 주문의 `pgAmount`가 다르면 결제를 확정하지 않습니다.
- 결제 확정 성공 시 결제 상태는 `COMPLETED`로 변경합니다.
- 결제 확정 성공 시 주문 상태는 `COMPLETED`로 변경하고 결제 완료 시간을 기록합니다.
- 이미 확정, 실패, 취소, 환불 처리된 결제는 다시 확정할 수 없습니다.
- PortOne 결제 금액과 서버 주문 금액이 다르면 결제 상태는 `FAILED`, 주문 상태는 `CANCELED`로 변경하고 주문 생성 시 선차감한 재고를 복구합니다.
- 금액 불일치처럼 이미 승인된 PG 결제에 대한 보정이 필요한 경우 PortOne 결제 취소 API를 호출합니다.
