# 전체 환불 요청

Source: https://app.notion.com/p/08c707ac8f56824499a70110f5a75567

Properties:

```json
{"API Path":"/api/payments/\\{paymentId\\}/refunds","Http Method":"POST","url":"https://app.notion.com/p/08c707ac8f56824499a70110f5a75567","기능 분류":"환불","담당자":"한예진","명세 기능":"전체 환불 요청","작성 현황":"진행 중","참고할 부분":"결제 건에 대한 환불","테스트 여부":"__NO__"}
```

## **01. 설명**

- 결제 완료된 결제 건을 전체 환불하는 API입니다.
- 본인이 결제한 결제 건만 환불할 수 있습니다.
- 이미 환불된 결제 건은 다시 환불할 수 없습니다.
- 부분 환불은 지원하지 않습니다.
- 환불이 성공하면 주문 상태는 `CANCELED`로 변경됩니다.
- 환불이 성공하면 결제 상태는 `REFUNDED`로 변경됩니다.
- 주문 상품 전체 수량만큼 재고를 다시 복구합니다.
- 결제할 때 사용한 포인트는 다시 돌려줍니다.
- 결제 완료로 적립된 포인트는 다시 회수합니다.
- 카드 결제 금액이 있으면 PortOne 결제 취소 API를 호출합니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| paymentId | Long | 필수 | 전체 환불할 결제 ID |

### 요청 예시

```plain text
POST /api/payments/200/refunds
```

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| Authorization | String | 필수 | Bearer {accessToken} 형식의 JWT 토큰 |
| Content-Type | String | 필수 | application/json |

### **c. request body**

```json
{
  "reason": "전체 주문 환불 요청"
}
```

| **이름** | **데이터타입** | 필수 여부 | **설명** |
| --- | --- | --- | --- |
| reason | String | 필수 | 환불 사유 |

## **03. 응답(response)**

### **a. response header**

```json
{
  "Content-Type": "application/json"
}
```

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
| Content-Type | String | application/json |

### **b. response body**

**성공응답 :** `200 OK`

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

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| refundId | Long | 환불 ID |
| orderId | Long | 환불 대상 주문 ID |
| orderNumber | String | 노출용 주문번호 |
| paymentId | Long | 환불 대상 결제 ID |
| refundStatus | String | 환불 상태. 성공 시 SUCCESS |
| orderStatus | String | 환불 후 주문 상태 |
| paymentStatus | String | 환불 후 결제 상태 |
| refundTotalAmount | Long | 총 환불 금액 |
| refundPointAmount | Long | 돌려주는 사용 포인트 금액 |
| refundPgAmount | Long | PortOne으로 취소할 카드 결제 금액 |
| revokedPointAmount | Long | 회수할 적립 포인트 금액 |
| reason | String | 환불 사유 |
| restoredItems | Array | 재고가 복구된 상품 목록 |
| restoredItems.productId | Long | 상품 ID |
| restoredItems.productName | String | 상품명 |
| restoredItems.restoredQuantity | Integer | 복구된 재고 수량 |
| createdAt | String | 환불 요청 생성 시각 |
| refundedAt | String | 환불 완료 시각 |

**에러 응답:**

| Status | Error Code | 설명 |
| --- | --- | --- |
| 400 | INVALID_PAYMENT_ID | paymentId 형식이 잘못됨 |
| 400 | INVALID_REFUND_REASON | 환불 사유가 비어 있음 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN_PAYMENT | 본인 결제 건이 아님 |
| 404 | PAYMENT_NOT_FOUND | 결제 정보를 찾을 수 없음 |
| 404 | ORDER_NOT_FOUND | 결제에 연결된 주문을 찾을 수 없음 |
| 409 | REFUND_NOT_ALLOWED | 결제 완료 상태가 아니라 환불할 수 없음 |
| 409 | ALREADY_REFUNDED | 이미 환불된 결제 |
| 502 | REFUND_PG_CANCEL_FAILED | PortOne PG 취소 실패 |

## **04. 비즈니스 규칙**

- 로그인한 회원만 호출할 수 있습니다.
- 본인의 결제 건만 환불할 수 있습니다.
- 결제 상태가 COMPLETED인 결제 건만 환불할 수 있습니다.
- 이미 환불된 결제 건은 다시 환불할 수 없습니다.
- 부분 환불은 지원하지 않습니다.
- 환불 금액은 클라이언트가 입력하지 않습니다.
- 서버가 주문과 결제 정보를 기준으로 전체 환불 금액을 계산합니다.
- 환불 성공 시 주문 상태는 `CANCELED`로 변경합니다.
- 환불 성공 시 결제 상태는 `REFUNDED`로 변경합니다.
- 환불 성공 시 주문 상품 전체 수량만큼 재고를 복구합니다.
- 결제할 때 사용한 포인트는 다시 돌려줍니다.
- 결제 완료로 적립된 포인트는 다시 회수합니다.
- 카드 결제 금액이 있으면 PortOne 결제 취소 API를 호출합니다.
- 포인트 전액 결제라면 PortOne 결제 취소 API를 호출하지 않습니다.
- PG 취소에 실패하면 환불 상태를 FAILED로 저장하고, 운영자가 확인할 수 있도록 실패 로그를 남깁니다.
