# 결제 / 웹훅

## 결제 확정

Source: https://app.notion.com/p/45b707ac8f56820592f0818a707af21e

Properties:

```json
{"API Path":"/api/payments/confirm","Http Method":"POST","url":"https://app.notion.com/p/45b707ac8f56820592f0818a707af21e","기능 분류":"결제","담당자":"한예진","명세 기능":"결제 확정","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

결제 확정 api

PortOne 결제창에서 결제가 완료된 뒤, 클라이언트가 결제 결과를 서버에 확정 요청하는 API입니다.

서버는 요청받은 `portonePaymentId`로 PortOne 결제 내역을 조회하여 실제 결제 금액과 주문 결제 금액이 일치하는지 검증합니다.

검증에 성공하면 결제 상태를 COMPLETED, 주문 상태를 COMPLETED로 변경합니다.

결제가 완료되면 해당 주문에 사용된 장바구니 상품을 비웁니다.

포인트를 사용한 주문이라면 결제 확정 시점에 포인트를 차감합니다.

검증에 실패하면 결제와 주문 상태를 완료 처리하지 않습니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| - | - | - | URL Path 또는 Query Parameter 없음 |

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| Authorization | Bearer {accessToken} | ✅ | JWT 인증 토큰 |
| Content-Type | application/json | ✅ | 요청 데이터 타입 |

### **c. request body**

```json
{
  "paymentId": 200,
  "portonePaymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc"
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| paymentId | Long | ✅ | 서버에 저장된 결제 ID |
| portonePaymentId | String | ✅ | PortOne 결제창 호출에 사용한 서버 채번 결제 식별자 |

## **03. 응답(respons)**

### **a. response header**

```json

```

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
| `Content-Type` | `application/json` | 데이터 타입 |

### **b. response body**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": {
    "paymentId": 200,
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "portonePaymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc",
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
| portonePaymentId | String | PortOne 결제 식별자 |
| paymentStatus | String | 결제 상태. 성공 시 COMPLETED |
| orderStatus | String | 주문 상태. 성공 시 COMPLETED |
| totalAmount | Long | 주문 총액 |
| usedPointAmount | Long | 사용 포인트 금액 |
| pgAmount | Long | PG 실결제 금액 |
| paidAt | String | 결제 완료 일시 |

**에러 응답:**

| Status | Error Code | 설명 |
| --- | --- | --- |
| 400 | INVALID_PAYMENT_REQUEST | 결제 확정 요청 값이 잘못됨 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN_PAYMENT | 본인 결제 건이 아님 |
| 404 | PAYMENT_NOT_FOUND | 결제 정보를 찾을 수 없음 |
| 409 | PAYMENT_ALREADY_PROCESSED | 이미 처리된 결제 |
| 409 | PAYMENT_AMOUNT_MISMATCH | 서버 주문 금액과 PortOne 결제 금액이 다름 |
| 409 | PAYMENT_NOT_COMPLETED | PortOne 결제가 완료 상태가 아님 |
| 502 | PORTONE_API_ERROR | PortOne 결제 조회 실패 |

## 4. 비즈니스 규칙

로그인한 회원만 호출할 수 있습니다.

본인의 결제 건만 확정할 수 있습니다.

결제 확정 전 서버는 PortOne API로 실제 결제 상태를 조회합니다.

PortOne 결제 상태가 결제 완료가 아니면 결제를 확정하지 않습니다.

PortOne 결제 금액과 서버의 pgAmount가 다르면 결제를 확정하지 않습니다.

결제 확정 성공 시 결제 상태는 COMPLETED로 변경합니다.
결제 확정 성공 시 주문 상태는 COMPLETED로 변경하고 paidAt을 기록합니다.

결제 확정 성공 시 사용 포인트가 있다면 회원 포인트를 차감합니다.

결제 확정 성공 시 해당 주문에 포함된 장바구니 상품을 삭제합니다.

이미 확정된 결제는 다시 확정할 수 없습니다.

결제 검증 실패 시 이미 승인된 PG 결제가 있다면 PortOne 결제 취소 API를 호출합니다.
결제 검증 실패 시 결제 상태는 FAILED, 주문 상태는 CANCELED로 변경하고 선차감된 재고를 복구합니다.
결제 실패 또는 검증 실패 시 장바구니는 비우지 않습니다.

## 웹훅 수신

Source: https://app.notion.com/p/81b707ac8f568239809b01059d2583ce

Properties:

```json
{"API Path":"/api/webhooks/portone","Http Method":"POST","url":"https://app.notion.com/p/81b707ac8f568239809b01059d2583ce","기능 분류":"결제","담당자":"한예진","명세 기능":"웹훅 수신","작성 현황":"완료","참고할 부분":"이 API는 클라이언트가 직접 호출하지 않고, PortOne 서버가 호출합니다.","테스트 여부":"__NO__"}
```

## **01. 설명**

PortOne이 결제 상태 변경 웹훅을 서버에 전송한다.
시스템은 PortOne 웹훅 서명을 검증한다.
시스템은 웹훅 본문에서 portonePaymentId만 추출한다.
시스템은 본문 데이터는 신뢰하지 않는다.
시스템은 portonePaymentId를 기준으로 PortOne API에 결제 정보를 조회한다.
시스템은 결제 상태, 승인 금액, 처리 여부를 검증한다.
시스템은 이미 처리 완료된 결제라면 상태 변경 없이 200 OK를 반환한다.
시스템은 아직 처리되지 않은 결제라면 결제 확정 공통 로직을 수행한다.
시스템은 주문 완료, Payment 완료, 포인트 사용, 포인트 적립, 포인트 잔액 갱신, 장바구니 초기화를 처리한다.
시스템은 처리 결과와 관계없이 정상 수신한 웹훅에 200 OK를 반환한다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| - | - | - | URL Path 또는 Query Parameter 없음 |

### **b. request headers**

```json
{
  "Content-Type": "application/json",
  "PortOne-Webhook-Signature": "{signature}"
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| Content-Type | String | ✅ | application/json |
| PortOne-Webhook-Signature | String | ✅ | PortOne 웹훅 서명 값 |

### **c. request body**

```json
{
  "type": "Transaction.Paid",
  "paymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc"
}
```

| 이름 | 데이터타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| type | String | ✅ | PortOne 웹훅 이벤트 타입 |
| paymentId | String | ✅ | PortOne 결제 식별자. 서버의 portonePaymentId와 매칭 |

## **03. 응답(respons)**

### **a. response header**

```json

```

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
| Content-Type | String | application/json |

### **b. response body**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": null,
  "message": "웹훅 수신 성공"
}
```

| Key | 데이터타입 | 설명 |
| --- | --- | --- |
| success | Boolean | 요청 성공 여부 |
| data | null | 별도 응답 데이터 없음 |
| message | String | 응답 메시지 |

**에러 응답:**

| Status | Error Code | 설명 |
| --- | --- | --- |
| 400 | INVALID_WEBHOOK_PAYLOAD | 웹훅 요청 형식이 잘못됨 |
| 404 | PAYMENT_NOT_FOUND | 서버 결제 정보를 찾을 수 없음 |
| 409 | PAYMENT_AMOUNT_MISMATCH | 서버 결제 금액과 PortOne 결제 금액이 다름 |
| 502 | PORTONE_API_ERROR | PortOne 결제 조회 실패 |

## 4. 비즈니스 규칙

- 웹훅 요청만 믿고 결제 완료 처리하지 않습니다.
- 반드시 PortOne API로 결제 상태를 재조회합니다.
- 같은 웹훅이 여러 번 와도 중복 처리되지 않아야 합니다.
- 이미 COMPLETED인 결제에 결제 완료 웹훅이 다시 오면 상태 변경 없이 200 OK를 반환합니다.
- 웹훅 본문에 포함된 결제 상태나 금액은 그대로 신뢰하지 않고, PortOne API 재조회 결과를 기준으로 처리합니다.

결제 확정 API는 클라이언트가 PortOne 결제창 완료 후 서버에 결제 검증을 요청하는 API입니다.
웹훅 수신 API는 PortOne이 서버에 결제 상태 변경 이벤트를 알려주는 API입니다.
차이:

| **구분** | **결제 확정** | **웹훅 수신** |
| --- | --- | --- |
| 호출 주체 | 클라이언트 | PortOne |
| 호출 시점 | 결제창 완료 직후 | PortOne 상태 변경 발생 시 |
| 인증 | 사용자 JWT 필요 | 웹훅 서명 검증 |
| 목적 | 사용자 결제 결과 검증 및 확정 | 결제 상태 보정/동기화 |
| 응답 | 사용자에게 결과 반환 | PortOne에 수신 성공 반환 |
