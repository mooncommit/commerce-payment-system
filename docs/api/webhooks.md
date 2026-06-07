# Webhooks API

## PortOne 웹훅 수신

### 01. 설명

PortOne이 결제 상태 변경 웹훅을 서버에 전송하는 API입니다.

시스템은 PortOne Standard Webhooks 헤더(`webhook-id`, `webhook-timestamp`, `webhook-signature`)와 요청 본문을 이용해 웹훅 서명을 검증합니다. 서명 검증은 PortOne Server SDK가 수행하며, HMAC-SHA256 서명 검증과 타임스탬프 검증을 함께 처리합니다.

서명 검증에 성공하면 PortOne SDK가 웹훅 본문을 타입별 `Webhook` 객체로 변환합니다. 시스템은 처리 대상 이벤트에서 `paymentId`를 꺼내 서버의 `portonePaymentId`로 사용합니다.

시스템은 웹훅 본문에 포함된 결제 상태나 금액을 그대로 신뢰하지 않습니다. `portonePaymentId`를 기준으로 PortOne API에 결제 정보를 재조회하고, 재조회 결과의 결제 상태와 승인 금액을 검증합니다.

`Transaction.Paid` 이벤트이고 PortOne 결제 상태가 `PAID`이며 승인 금액이 서버 주문의 `pgAmount`와 일치하면, 아직 처리되지 않은 결제에 한해 결제 상태를 `COMPLETED`, 주문 상태를 `COMPLETED`로 변경합니다. 이미 `COMPLETED`인 결제라면 상태 변경 없이 처리 완료로 기록합니다.

같은 `webhook-id`가 다시 들어오면 중복 이벤트로 보고 상태 변경 없이 `200 OK`를 반환합니다.

처리 대상이 아닌 이벤트나 취소 웹훅은 현재 내부 결제/주문 상태에 자동 반영하지 않고 `IGNORED`로 기록합니다. 처리 중 실패한 이벤트는 `FAILED`로 기록합니다. 다만 PortOne 재전송 정책을 고려해 정상 수신한 웹훅 요청에는 `200 OK`를 반환합니다.

### 웹훅이란?

웹훅은 PortOne이 결제 상태 변경을 서버에 알려주는 HTTP 콜백입니다.

결제 승인 과정에서 클라이언트가 결제 확정 API를 호출하지 못하거나 네트워크 장애가 발생하면, 서버와 PG의 결제 상태가 달라질 수 있습니다. 웹훅은 이런 상태 불일치를 보정하기 위한 보조 경로입니다.

웹훅 엔드포인트는 외부에 공개되므로 서명 검증으로 발신자를 검증하고, 타임스탬프 검증으로 재전송 공격을 방어합니다. 또한 PortOne 재시도 정책 때문에 동일 이벤트가 여러 번 들어올 수 있으므로 `webhook-id` 중복 검사와 DB unique 제약, 결제 상태 검증으로 멱등성을 보장합니다.

### 02. 요청(Request)

#### a. Parameter & Querystring & URL

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| - | - | - | URL Path 또는 Query Parameter 없음 |

#### b. Request Headers

```json
{
  "Content-Type": "application/json",
  "webhook-id": "{webhookId}",
  "webhook-timestamp": "{timestamp}",
  "webhook-signature": "{signature}"
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| Content-Type | String | 필수 | `application/json` |
| webhook-id | String | 필수 | PortOne 웹훅 이벤트 식별자. 중복 처리 방지에 사용 |
| webhook-timestamp | String | 필수 | 웹훅 생성 시각. 타임스탬프 검증에 사용 |
| webhook-signature | String | 필수 | PortOne 웹훅 서명 값 |

#### c. Request Body

PortOne SDK가 타입별 `Webhook` 객체로 역직렬화하므로 서버 코드에서 raw body를 직접 신뢰하지 않습니다.

예시:

```json
{
  "type": "Transaction.Paid",
  "data": {
    "paymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc",
    "storeId": "store-id",
    "transactionId": "transaction-id"
  }
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| type | String | 필수 | PortOne 웹훅 이벤트 타입 |
| data.paymentId | String | 필수 | PortOne 결제 식별자. 서버의 `portonePaymentId`와 매칭 |
| data.storeId | String | 필수 | PortOne 상점 식별자 |
| data.transactionId | String | 조건부 | PortOne 거래 식별자 |

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
  "message": "웹훅 수신 성공"
}
```

| Key | 데이터타입 | 설명 |
| --- | --- | --- |
| success | Boolean | 요청 성공 여부 |
| message | String | 응답 메시지 |

### 04. 내부 처리 결과

웹훅 처리 결과는 외부 응답 status code가 아니라 `webhook_events` 처리 상태로 기록합니다.

| 내부 결과 | 조건 | 처리 |
| --- | --- | --- |
| `RECEIVED` | 신규 웹훅 수신 직후 | 원본 payload와 이벤트 타입 저장 |
| `PROCESSED` | 결제 완료 웹훅 처리 성공 또는 이미 완료된 결제 | 결제/주문 상태 변경 완료 또는 생략 |
| `IGNORED` | 처리 대상이 아닌 이벤트, PG 상태 불일치, 취소 웹훅 | 내부 결제/주문 상태 변경 없음 |
| `FAILED` | 금액 불일치, 결제 조회 실패, 서버 처리 예외 | 실패 사유 기록 |
| 중복 수신 | 같은 `webhook-id` 재수신 | 신규 저장 및 상태 변경 없이 200 OK 반환 |

### 05. 비즈니스 규칙

- 웹훅 요청 본문만 믿고 결제 완료 처리하지 않습니다.
- 반드시 PortOne API로 결제 상태와 결제 금액을 재조회합니다.
- 같은 `webhook-id`가 여러 번 와도 중복 처리하지 않습니다.
- 이미 `COMPLETED`인 결제에 결제 완료 웹훅이 다시 오면 상태 변경 없이 처리 완료로 기록합니다.
- PortOne 결제 상태가 `PAID`가 아니면 내부 상태를 변경하지 않고 `IGNORED`로 기록합니다.
- PortOne 결제 금액과 서버 주문의 `pgAmount`가 다르면 내부 상태를 변경하지 않고 `FAILED`로 기록합니다.
- 취소 웹훅은 현재 자동 반영하지 않고 `IGNORED`로 기록합니다.
- 처리 중 예외가 발생해도 웹훅 이벤트에는 실패 사유를 기록하고, 컨트롤러 응답은 `200 OK`를 반환합니다.

### 06. 결제 확정 API와의 차이

| 항목 | 결제 확정 API | 웹훅 수신 API |
| --- | --- | --- |
| 호출 주체 | 클라이언트 | PortOne |
| 목적 | 결제창 완료 후 즉시 서버 결제 확정 요청 | 클라이언트 누락/네트워크 장애에 따른 상태 보정 |
| 인증 방식 | JWT 인증 | PortOne 웹훅 서명 검증 |
| 결제 조회 | 서버가 PortOne API 재조회 | 서버가 PortOne API 재조회 |
| 성공 처리 | 결제 `COMPLETED`, 주문 `COMPLETED` | 미처리 결제인 경우 결제 `COMPLETED`, 주문 `COMPLETED` |
| 실패 응답 | 비즈니스 에러 status code 반환 | 실패 사유를 내부 기록하고 외부에는 200 OK 반환 |
| 중복 처리 | 이미 처리된 결제는 에러 | 이미 완료된 결제 또는 중복 webhook-id는 상태 변경 없이 200 OK |
