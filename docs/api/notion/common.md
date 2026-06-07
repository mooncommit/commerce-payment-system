# 공통 규칙 및 응답 정의

Source: https://app.notion.com/p/7e8707ac8f5682648c4601b187dd1c72

Properties:

```json
{"API Path":"","url":"https://app.notion.com/p/7e8707ac8f5682648c4601b187dd1c72","기능 분류":"공통","담당자":"한예진","명세 기능":"**공통 규칙 및 응답 정의**","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 공통 응답 핸들러
- 모든 API 응답은 공통 응답 형식을 사용합니다.
- 성공 응답은 success, data, message를 포함합니다.
- 에러 응답은 success, error를 포함합니다.
- 각 API 명세서에서는 data 내부 구조와 해당 API에서 발생 가능한 에러 코드만 작성합니다.

## URL 규칙

### 기본 규칙

- API 결로는 `/api`로 시작합니다.
- URL은 소문자로 작성합니다.
- 단어 구분은 하이픈보다 복수 명사와 path variable을 우선 사용합니다.
- 리소스 이름은 복수형으로 작성합니다.

### 예시

```plain text
GET /api/products
GET /api/products/{productId}
GET /api/carts
POST /api/carts/items
GET /api/orders/{orderId}
POST /api/orders/{orderId}/cancel
POST /api/orders/{orderId}/refund
```

### 피해야 할 예시

```plain text
/api/carts/Items
/api/orderStatusChange
/api/orders/{orderId}/status
```

## **02. 성공 응답 형식**

모든 성공 응답은 아래 형식을 기본으로 사용합니다.

```json
{
  "success": true,
  "data": {},
  "message": "요청 처리 성공"
}
```

| Key | 데이터타입 | 설명 |
| --- | --- | --- |
| success | Boolean | 요청 성공 여부 |
| data | Object / Array / null | 응답 데이터 |
| message | String | 응답 메시지 |

## **03. 에러 응답 형식**

모든 에러 응답은 아래 형식을 기본으로 사용합니다.

```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "인증되지 않거나 만료된 토큰입니다."
  }
}
```

| Key | 데이터타입 | 설명 |
| --- | --- | --- |
| success | Boolean | 요청 성공 여부. 실패 시 false |
| error | Object | 에러 정보 |
| error.code | String | 에러 코드 |
| error.message | String | 사용자 또는 개발자가 확인할 에러 메시지 |

## 04. 공통 에러 코드

| Status | Error Code | 설명 |
| --- | --- | --- |
| 400 | BAD_REQUEST | 요청 형식이 잘못됨 |
| 400 | VALIDATION_ERROR | 요청 값 검증 실패 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN | 접근 권한 없음 |
| 404 | NOT_FOUND | 요청한 리소스를 찾을 수 없음 |
| 405 | METHOD_NOT_ALLOWED | 지원하지 않는 HTTP Method |
| 409 | CONFLICT | 현재 리소스 상태와 충돌 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

## 05. 작성규칙

각 API 문서에서는 공통 응답 전체를 반복해서 쓰지 않고, 아래 내용만 작성합니다.
DB 컬럼은 snake_case, API JSON은 camelCase.

- 성공 시 HTTP Status
- data 내부 구조
- 해당 API에서만 발생하는 에러 코드
- 비즈니스 규칙

예를 들어 주문 생성 API는:

```json
{
  "success": true,
  "data": {
    "orderId": 100
  },
  "message": "주문 및 결제 대기 생성 성공"
}
```

## 06. 공통 Enum 정의

### 회원 등급

| 한글명 | Code | 설명 |
| --- | --- | --- |
| 일반 | NORMAL | 기본 회원 등급 |
| VIP | VIP | VIP 회원 등급 |
| VVIP | VVIP | VVIP 회원 등급 |

### 상품 판매 상태

| 한글명 | Code | 설명 |
| --- | --- | --- |
| 판매중 | ON_SALE | 구매 가능한 상태 |
| 품절 | SOLD_OUT | 재고가 없어 구매 불가한 상태 |

### 주문 상태

| 한글명 | Code | 설명 |
| --- | --- | --- |
| 결제대기 | PAYMENT_PENDING | 주문 생성 완료 후 결제 대기 상태 |
| 주문완료 | COMPLETED | 결제 완료 후 주문 완료 상태 |
| 주문취소 | CANCELED | 결제 실패 또는 주문 취소 상태 |

### 결제 상태

| 한글명 | Code | 설명 |
| --- | --- | --- |
| 결제대기 | PENDING | 주문 생성 완료, 결제 대기 상태 |
| 결제완료 | COMPLETED | 결제 검증 완료 및 결제 성공 상태 |
| 결제실패 | FAILED | 결제 실패 또는 결제 검증 실패 상태 |
| 결제취소 | CANCELED | 결제 완료 전 주문 취소 상태 |
| 환불완료 | REFUNDED | 결제 완료 후 환불 처리 완료 상태 |

### 결제 수단 유형

| 한글명 | Code | 설명 |
| --- | --- | --- |
| 카드 결제 | CARD | 카드 결제만 사용 |
| 포인트 전액 결제 | POINT_ONLY | 포인트로 전액 결제, PG 호출 없음 |
| 카드 + 포인트 결제 | CARD_AND_POINT | 포인트와 카드를 함께 사용 |

### 환불 상태

| 한글명 | Code | 설명 |
| --- | --- | --- |
| 환불성공 | SUCCESS | 환불 처리 성공 |
| 환불실패 | FAILED | 환불 처리 실패 |

### 포인트 거래 유형

| **한글명** | **Code** | **설명** |
| --- | --- | --- |
| 적립 | EARN | 포인트 적립 |
| 사용 | USE | 포인트 사용 |
| 사용복구 | REFUND | 사용한 포인트 복구 |
| 회수 | REVOKE | 적립된 포인트 회수 |
