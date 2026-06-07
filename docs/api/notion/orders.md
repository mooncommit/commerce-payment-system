# 주문

## 상품 바로 주문/결제 생성

Source: https://app.notion.com/p/308707ac8f5683f39dec81790231ff12

Properties:

```json
{"API Path":"/api/orders","Http Method":"POST","url":"https://app.notion.com/p/308707ac8f5683f39dec81790231ff12","기능 분류":"주문","담당자":"문승주","명세 기능":"상품 바로 주문/결제 생성","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 상품 상세에서 바로 주문/결제를 생성하는 API입니다.
- 특정 상품 ID와 수량을 받아 주문을 생성합니다.
- 주문 생성과 함께 결제 대기 데이터도 생성합니다.
- 서버는 PortOne 결제에 사용할 `portonePaymentId`를 만들어 응답으로 내려줍니다.
- 클라이언트는 이 값으로 PortOne 결제창을 호출합니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| - | - | - | URL Path 또는 Query Parameter 없음 |

### 요청 예시

```plain text
POST /api/orders
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
  "productId": 10,
  "quantity": 1,
  "usePointAmount": 0
}
```

| **이름** | **데이터타입** | **필수여부** | **설명** |
| --- | --- | --- | --- |
| productId | Long | 필수 | 바로 주문할 상품 ID |
| quantity | Integer | 필수 | 주문할 상품 수량 |
| usePointAmount | Long | 필수 | 결제에 사용할 포인트 금액. 사용하지 않으면 0 입력 |

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

**성공응답 :** `201 CREATED`

```json
{
  "success": true,
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "paymentId": 200,
    "portonePaymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc",
    "orderStatus": "PAYMENT_PENDING",
    "paymentStatus": "PENDING",
    "totalAmount": 30000,
    "usedPointAmount": 0,
    "pgAmount": 30000,
    "items": [
      {
        "orderItemId": 1,
        "productId": 10,
        "productName": "무선 키보드",
        "unitPrice": 30000,
        "quantity": 1,
        "lineTotalAmount": 30000
      }
    ]
  },
  "message": "상품 바로 주문 및 결제 대기 생성 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| orderId | Long | 주문 ID |
| orderNumber | String | 노출용 주문번호 |
| paymentId | Long | 결제 ID |
| portonePaymentId | String | PortOne 결제창 호출에 사용할 서버가 만든 결제 ID |
| orderStatus | String | 주문 상태. 생성 직후 `PAYMENT_PENDING` |
| paymentStatus | String | 결제 상태. 생성 직후 `PENDING` |
| totalAmount | Long | 주문 총액 |
| usedPointAmount | Long | 사용 포인트 금액 |
| pgAmount | Long | 카드 결제 금액. totalAmount - usedPointAmount |
| items | Array | 주문 상품 목록 |
| items.orderItemId | Long | 주문 상품 ID |
| items.productId | Long | 상품 ID |
| items.productName | String | 주문 당시 상품명 |
| items.unitPrice | Long | 주문 당시 상품 가격 |
| items.quantity | Integer | 주문 수량 |
| items.lineTotalAmount | Long | 상품별 합계 금액 |

**에러 응답:**

| **Status** | **Error Code** | **설명** |
| --- | --- | --- |
| 400 | INVALID_PRODUCT_ID | productId 형식이 잘못됨 |
| 400 | INVALID_QUANTITY | 주문 수량이 1보다 작거나 잘못됨 |
| 400 | INVALID_POINT_AMOUNT | 사용 포인트 금액이 음수이거나 잘못됨 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 404 | PRODUCT_NOT_FOUND | 상품을 찾을 수 없음 |
| 409 | OUT_OF_STOCK | 주문 수량이 현재 재고보다 많음 |
| 409 | PRODUCT_UNAVAILABLE | 판매 중이 아닌 상품 |
| 409 | POINT_BALANCE_NOT_ENOUGH | 회원 포인트 잔액보다 사용 포인트 금액이 큼 |

## **04. 비즈니스 규칙**

- 로그인한 회원만 호출할 수 있습니다.
- 상품 상세에서 바로 주문할 때 사용하는 API입니다.
- 주문은 `productId`와 요청 수량을 기준으로 생성합니다.
- 주문과 결제 대기 데이터는 함께 생성합니다.
- 주문 생성 시 재고를 확인하고 먼저 줄입니다.
- 재고가 부족하거나 판매 중이 아닌 상품이면 주문은 생성하지 않습니다.
- 주문 상품에는 주문 당시의 상품명, 가격, 수량을 저장합니다.
- 서버는 PortOne 결제용 ID(`portonePaymentId`)를 만들어 응답으로 내려줍니다.
- 실제 카드 결제 금액은 `주문 총액 - 사용 포인트`로 계산합니다.
- 보유 포인트보다 많은 포인트는 사용할 수 없습니다.
- 결제 실패 또는 주문 취소 후 다시 결제하려면 새 주문을 생성합니다.

## 장바구니로부터 주문/결제 생성

Source: https://app.notion.com/p/eb8707ac8f56837ab38f01118d2a6063

Properties:

```json
{"API Path":"/api/carts/orders","Http Method":"POST","url":"https://app.notion.com/p/eb8707ac8f56837ab38f01118d2a6063","기능 분류":"주문","담당자":"문승주","명세 기능":"장바구니로부터 주문/결제 생성","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 장바구니에 담긴 상품으로 주문을 생성하는 API입니다.
- 선택한 장바구니 상품을 주문할 수 있습니다.
- 주문 생성과 함께 결제 대기 데이터도 생성합니다.
- 서버는 PortOne 결제에 사용할 `portonePaymentId`를 만들어 응답으로 내려줍니다.
- 클라이언트는 이 값으로 PortOne 결제창을 호출합니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| - | - | - | URL Path 또는 Query Parameter 없음 |

### 요청 예시

```plain text
POST /api/carts/orders
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
  "cartItemIds": [1, 2],
  "usePointAmount": 0
}
```

| **이름** | **데이터타입** | **필수여부** | **설명** |
| --- | --- | --- | --- |
| cartItemIds | List\<Long\> | 필수 | 주문할 장바구니 상품 ID 목록 |
| usePointAmount | Long | 필수 | 결제에 사용할 포인트 금액. 사용하지 않으면 0 입력 |

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

**성공응답 :** `201 CREATED`

```json
{
  "success": true,
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "paymentId": 200,
    "portonePaymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc",
    "orderStatus": "PAYMENT_PENDING",
    "paymentStatus": "PENDING",
    "totalAmount": 60000,
    "usedPointAmount": 5000,
    "pgAmount": 55000,
    "items": [
      {
        "orderItemId": 1,
        "productId": 10,
        "productName": "무선 키보드",
        "unitPrice": 30000,
        "quantity": 2,
        "lineTotalAmount": 60000
      }
    ]
  },
  "message": "장바구니 주문 및 결제 대기 생성 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| orderId | Long | 주문 ID |
| orderNumber | String | 노출용 주문번호 |
| paymentId | Long | 결제 ID |
| portonePaymentId | String | PortOne 결제창 호출에 사용할 서버가 만든 결제 ID |
| orderStatus | String | 주문 상태. 생성 직후 `PAYMENT_PENDING` |
| paymentStatus | String | 결제 상태. 생성 직후 `PENDING` |
| totalAmount | Long | 주문 총액 |
| usedPointAmount | Long | 사용 포인트 금액 |
| pgAmount | Long | 카드 결제 금액. totalAmount - usedPointAmount |
| items | Array | 주문 상품 목록 |
| items.orderItemId | Long | 주문 상품 ID |
| items.productId | Long | 상품 ID |
| items.productName | String | 주문 당시 상품명 |
| items.unitPrice | Long | 주문 당시 상품 가격 |
| items.quantity | Integer | 주문 수량 |
| items.lineTotalAmount | Long | 상품별 합계 금액 |

**에러 응답:**

| **Status** | **Error Code** | **설명** |
| --- | --- | --- |
| 400 | CART_EMPTY | 장바구니가 비어 있음 |
| 400 | INVALID_CART_ITEM_ID | 요청한 장바구니 상품 ID 형식이 잘못됨 |
| 400 | INVALID_POINT_AMOUNT | 사용 포인트 금액이 음수이거나 잘못됨 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN_CART_ITEM | 본인 장바구니 상품이 아님 |
| 404 | CART_ITEM_NOT_FOUND | 요청한 장바구니 상품을 찾을 수 없음 |
| 409 | OUT_OF_STOCK | 주문 수량이 현재 재고보다 많음 |
| 409 | PRODUCT_UNAVAILABLE | 판매 중이 아닌 상품이 포함됨 |
| 409 | POINT_BALANCE_NOT_ENOUGH | 회원 포인트 잔액보다 사용 포인트 금액이 큼 |

## **04. 비즈니스 규칙**

- 로그인한 회원만 호출할 수 있습니다.
- 주문은 본인 장바구니에 담긴 상품을 기준으로 생성합니다.
- `cartItemIds`에 담긴 장바구니 상품만 주문합니다.
- 주문과 결제 대기 데이터는 함께 생성합니다.
- 주문 생성 시 재고를 확인하고 먼저 줄입니다.
- 재고가 부족하거나 판매 중이 아닌 상품이 있으면 주문은 생성하지 않습니다.
- 주문 상품에는 주문 당시의 상품명, 가격, 수량을 저장합니다.
- 서버는 PortOne 결제용 ID(`portonePaymentId`)를 만들어 응답으로 내려줍니다.
- 실제 카드 결제 금액은 `주문 총액 - 사용 포인트`로 계산합니다.
- 보유 포인트보다 많은 포인트는 사용할 수 없습니다.
- 주문 생성 시 장바구니는 비우지 않고, 결제 완료 후에만 비웁니다.
- 결제 실패 또는 주문 취소 후 다시 결제하려면 새 주문을 생성합니다.

## 주문/결제 동시 생성

Source: https://app.notion.com/p/d83707ac8f568386a15d81c117d6a548

Properties:

```json
{"API Path":"**`/api/orders/checkout`**","Http Method":"POST","url":"https://app.notion.com/p/d83707ac8f568386a15d81c117d6a548","기능 분류":"결제","담당자":"한예진","명세 기능":"주문/결제 동시 생성","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 주문 생성과 결제 대기 정보를 함께 생성하는 체크아웃 API입니다.
- 주문을 `PAYMENT_PENDING `상태로 생성하고,
- 결제를 `PENDING` 상태로 생성한 뒤,
- PortOne 결제창 호출에 사용할 `portonePaymentId`를 반환합니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| 이름 | 데이터타입 | 설명 |
| --- | --- | --- |
| - | - | URL Path 또는 Query Parameter 없음 |

### **b. request headers**

```json

```

| Key | Value | 필수 | 설명 |
| --- | --- | --- | --- |
| Authorization | Bearer {accessToken} | ✅ | JWT 인증 토큰 |
| Content-Type | application/json | ✅ | 요청 Body 타입 |

### **c. request body**

```json
{
  "cartItemIds": [1, 2, 3]
}
```

| 이름 | 데이터타입 | 필수여부 | 설명 |
| --- | --- | --- | --- |
| items | List | ✅ | 주문 상품 목록 |
| productId | Long | ✅ | 상품 고유 식별자 |
| quantity | Int | ✅ | 주문 수량 |
| usePointAmount | Long | ✅ | 사용할 포인트 금액 |

## **03. 응답(respons)**

### **a. response header**

```json
{
  "Content-Type": "application/json"
}
```

| 이름 | 데이터타입 | 설명 |
| --- | --- | --- |
| Content-Type | String | application/json |

### **b. response body**

**성공응답:** `200 OK`

```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "portonePaymentId": "pay_7addd042-13de-45d1-a886-44d1884e4013",
    "totalPrice": 6900,
    "orderName": "베이직 스트라이프 삭스 3팩",
    "status": "PENDING_PAYMENT"
  },
  "message": "주문 및 결제 대기 생성 성공"
}
```

| Key | 데이터타입 | 설명 |
| --- | --- | --- |
| orderId | Long | 주문 ID |
| orderNumber | String | 노출용 주문번호 |
| paymentId | Long | 결제 ID |
| portonePaymentId | String | PortOne 결제창 호출에 사용할 서버 채번 결제 식별자 |
| orderStatus | String | 주문 상태. 생성 직후 PAYMENT_PENDING |
| paymentStatus | String | 결제 상태. 생성 직후 PENDING |
| paymentMethodType | String | 결제 수단 유형. CARD, POINT_ONLY, CARD_AND_POINT |
| totalAmount | Long | 주문 총액 |
| usedPointAmount | Long | 사용 포인트 금액 |
| pgAmount | Long | PG 실결제 금액 |
| items | Array | 주문 상품 목록 |
| items.orderItemId | Long | 주문 상품 ID |
| items.productId | Long | 상품 ID |
| items.productName | String | 주문 생성 시점 상품명 스냅샷 |
| items.unitPrice | Long | 주문 생성 시점 상품 가격 스냅샷 |
| items.quantity | Integer | 주문 수량 |
| items.lineTotalAmount | Long | 상품별 합계 금액 |

**에러 응답:**

| Status | Error Code | 설명 |
| --- | --- | --- |
| 400 | INVALID_CHECKOUT_ITEMS | 주문 상품 목록이 비어 있거나 잘못됨 |
| 400 | INVALID_POINT_AMOUNT | 사용 포인트 금액이 음수이거나 잘못됨 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 404 | PRODUCT_NOT_FOUND | 요청한 상품을 찾을 수 없음 |
| 409 | PRODUCT_UNAVAILABLE | 판매 중이 아닌 상품이 포함됨 |
| 409 | OUT_OF_STOCK | 주문 수량이 현재 재고보다 많음 |
| 409 | POINT_BALANCE_NOT_ENOUGH | 회원 포인트 잔액보다 사용 포인트 금액이 큼 |

## 주문서 미리보기

Source: https://app.notion.com/p/890707ac8f5683eb9fe38137e17ae8bf

Properties:

```json
{"API Path":"/api/orders/preview","Http Method":"GET","url":"https://app.notion.com/p/890707ac8f5683eb9fe38137e17ae8bf","기능 분류":"주문","담당자":"문승주","명세 기능":"주문서 미리보기","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 장바구니에 담긴 상품을 주문 직전에 확인하기 위한 읽기 전용 API입니다.
- 주문서 미리보기는 DB에 주문 데이터를 생성하지 않습니다.
- 상품명, 현재 판매가, 수량, 상품별 합계, 주문 총액을 계산해서 반환합니다.
- 장바구니 상품 ID 목록을 전달하지 않으면 장바구니 전체를 대상으로 미리보기를 생성합니다.
- 장바구니 상품 ID 목록을 전달하면 선택된 장바구니 상품만 대상으로 미리보기를 생성합니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| cartItemIds | List\<Long\> | 선택 | 선택 주문할 장바구니 상품 ID 목록. 없으면 장바구니 전체 조회 |

### 요청 예시

```plain text
GET /api/orders/preview
```

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}"
}
```

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| Authorization | String | 필수 | Bearer {accessToken} 형식의 JWT 토큰 |

### **c. request body**

```json
없음
```

| **이름** | **데이터타입** | 필수 여부 | **설명** |
| --- | --- | --- | --- |
| - | - | - | GET 요청이므로 request body 없음 |

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
    "items": [
      {
        "cartItemId": 1,
        "productId": 10,
        "productName": "무선 키보드",
        "price": 30000,
        "quantity": 2,
        "lineTotalAmount": 60000,
        "stockQuantity": 15,
        "saleStatus": "ON_SALE"
      }
    ],
    "totalAmount": 60000,
    "memberPointBalance": 10000
  },
  "message": "주문서 미리보기 조회 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| items | Array | 주문서에 표시할 장바구니 상품 목록 |
| items.cartItemId | Long | 장바구니 상품 ID |
| items.productId | Long | 상품 ID |
| items.productName | String | 현재 상품명 |
| items.price | Long | 현재 상품 판매가 |
| items.quantity | Integer | 장바구니에 담긴 수량 |
| items.lineTotalAmount | Long | 상품별 합계 금액. price × quantity |
| items.stockQuantity | Integer | 현재 상품 재고 수량 |
| items.saleStatus | String | 현재 상품 판매 상태 |
| totalAmount | Long | 주문서 총 상품 금액 |
| memberPointBalance | Long | 현재 회원의 사용 가능 포인트 잔액 |

**에러 응답:**

| **Status** | **Error Code** | **설명** |
| --- | --- | --- |
| 400 | CART_EMPTY | 장바구니가 비어 있음 |
| 400 | INVALID_CART_ITEM_ID | 요청한 장바구니 상품 ID 형식이 잘못됨 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN_CART_ITEM | 본인 장바구니 상품이 아님 |
| 404 | CART_ITEM_NOT_FOUND | 요청한 장바구니 상품을 찾을 수 없음 |
| 409 | OUT_OF_STOCK | 장바구니 수량이 현재 재고보다 많음 |
| 409 | PRODUCT_UNAVAILABLE | 판매 중이 아닌 상품이 포함됨 |

## **04. 비즈니스 규칙**

- 인증된 회원만 호출할 수 있습니다.
- 본인의 장바구니 상품만 조회할 수 있습니다.
- 이 API는 읽기 전용이며 주문, 결제, 재고 차감 데이터를 생성하지 않습니다.
- 응답 금액은 상품의 현재 가격 기준입니다.
- 실제 주문 상품 스냅샷은 주문/결제 생성 API 호출 시점에 저장합니다.
- 장바구니는 주문서 미리보기 단계에서 비워지지 않습니다.
- 장바구니는 결제 완료 시점에만 비워집니다.

## 주문 내역 조회

Source: https://app.notion.com/p/b63707ac8f56839284e40108a830f35a

Properties:

```json
{"API Path":"/api/orders","Http Method":"GET","url":"https://app.notion.com/p/b63707ac8f56839284e40108a830f35a","기능 분류":"주문","담당자":"문승주","명세 기능":"주문 내역 조회","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 로그인한 회원의 주문 목록을 조회하는 API입니다.
- 본인이 생성한 주문만 조회할 수 있습니다.
- 주문은 최신순으로 조회합니다.
- 응답에는 주문번호, 주문 상태, 주문 총액, 사용 포인트, 카드 결제 금액, 주문 생성일이 포함됩니다.
- 목록 조회 API이므로 주문 상품 상세 정보는 포함하지 않습니다.
- 주문 상품 상세 정보는 주문 상세 조회 API에서 확인합니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| page | Integer | 선택 | 조회할 페이지 번호. 기본값 0 |
| size | Integer | 선택 | 한 페이지에 조회할 주문 개수. 기본값 10 |

### 요청 예시

```plain text
GET /api/orders
```

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}"
}
```

### **c. request body**

```json
없음
```

## **03. 응답(response)**

**성공응답 :** `200 O`

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "orderId": 100,
        "orderNumber": "ORD-20260601-000001",
        "orderStatus": "PAID",
        "totalAmount": 60000,
        "usedPointAmount": 5000,
        "pgAmount": 55000,
        "createdAt": "2026-06-01T12:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "message": "주문 내역 조회 성공"
}
```

**에러 응답:**

| Status | Error Code | **설명** |
| --- | --- | --- |
| 400 | INVALID_PAGE_REQUEST | page 또는 size 값이 잘못됨 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |

## **04. 비즈니스 규칙**

- 로그인한 회원만 호출할 수 있습니다.
- 본인의 주문 목록만 조회할 수 있습니다.
- 다른 회원의 주문은 조회할 수 없습니다.
- 주문 목록은 최신 주문순으로 정렬합니다.
- 주문 상품 상세 정보는 목록 응답에 포함하지 않습니다.
- 주문 상품 상세 정보는 주문 상세 조회 API에서 확인합니다.
- 주문이 없으면 빈 배열을 반환합니다.

## 주문 상세 조회

Source: https://app.notion.com/p/fc4707ac8f56839ca92a817604846996

Properties:

```json
{"API Path":"/api/orders/\\{orderId\\}","Http Method":"GET","url":"https://app.notion.com/p/fc4707ac8f56839ca92a817604846996","기능 분류":"주문","담당자":"문승주","명세 기능":"주문 상세 조회","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 주문 1건의 상세 정보를 조회하는 API입니다.
- 주문 기본 정보, 주문 상품 목록, 결제 상태, 포인트 사용/적립 정보를 반환합니다.
- 주문 상품은 주문 당시 저장된 상품명과 가격을 기준으로 보여줍니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| orderId | Long | 필수 | 조회할 주문 ID |

### 요청 예시

```plain text
GET /api/orders/100
```

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}"
}
```

## **03. 응답(response)**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "orderStatus": "PAID",
    "totalAmount": 60000,
    "usedPointAmount": 5000,
    "pgAmount": 55000,
    "createdAt": "2026-06-01T12:30:00",
    "paidAt": "2026-06-01T12:35:00",
    "canceledAt": null,
    "payment": {
      "paymentId": 200,
      "paymentStatus": "PAID",
      "paymentMethodType": "CARD_AND_POINT",
      "portonePaymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc",
      "completedAt": "2026-06-01T12:35:00"
    },
    "pointSummary": {
      "usedPointAmount": 5000,
      "earnedPointAmount": 550
    },
    "items": [
      {
        "orderItemId": 1,
        "productId": 10,
        "productName": "무선 키보드",
        "unitPrice": 30000,
        "quantity": 2,
        "lineTotalAmount": 60000
      }
    ]
  },
  "message": "주문 상세 조회 성공"
}
```

**에러 응답:**

| Status | Error Code | **설명** |
| --- | --- | --- |
| 400 | INVALID_ORDER_ID | orderId 형식이 잘못됨 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN_ORDER | 본인 주문이 아님 |
| 404 | ORDER_NOT_FOUND | 주문을 찾을 수 없음 |

## **04. 비즈니스 규칙**

- 로그인한 회원만 호출할 수 있습니다.
- 본인의 주문만 조회할 수 있습니다.
- 다른 회원의 주문은 조회할 수 없습니다.
- 주문 상품은 주문 당시 저장된 상품명과 가격을 기준으로 보여줍니다.
- 상품 정보가 나중에 바뀌어도 주문 상세의 상품명과 가격은 바뀌지 않습니다.
- 주문 상세에는 주문 정보, 주문 상품 목록, 결제 상태, 포인트 사용/적립 요약이 포함됩니다.
- 환불된 주문인 경우 주문 상태와 결제 상태에 환불 결과가 반영되어야 합니다.

## 결제대기 주문 취소

Source: https://app.notion.com/p/68c707ac8f568270917f01a30d6638af

Properties:

```json
{"API Path":"/api/orders/\\{orderId\\}/cancel","Http Method":"POST","url":"https://app.notion.com/p/68c707ac8f568270917f01a30d6638af","기능 분류":"주문","담당자":"문승주","명세 기능":"결제대기 주문 취소","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 결제대기 상태의 주문을 취소하는 API입니다.
- 결제가 완료된 주문은 환불 API를 사용해야 합니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| orderId | Long | 필수 | 취소할 주문 ID |

### 요청 예시

```plain text
POST /api/orders/100/cancel
```

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}"
}
```

### **c. request body**

```json
없음
```

## **03. 응답(response)**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": {
    "orderId": 100,
    "orderNumber": "ORD-20260601-000001",
    "orderStatus": "CANCELED",
    "paymentId": 200,
    "paymentStatus": "FAILED",
    "restoredItems": [
      {
        "productId": 10,
        "productName": "무선 키보드",
        "restoredQuantity": 2
      }
    ],
    "canceledAt": "2026-06-01T13:00:00"
  },
  "message": "주문 취소 성공"
}
```

**에러 응답:**

| **Status** | **Error Code** | **설명** |
| --- | --- | --- |
| 400 | INVALID_ORDER_ID | orderId 형식이 잘못됨 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN_ORDER | 본인 주문이 아님 |
| 404 | ORDER_NOT_FOUND | 주문을 찾을 수 없음 |
| 409 | ORDER_NOT_CANCELABLE | 결제대기 상태가 아닌 주문이라 취소할 수 없음 |
| 409 | ORDER_ALREADY_CANCELED | 이미 취소된 주문 |

## **04. 비즈니스 규칙**

- 로그인한 회원만 호출할 수 있습니다.
- 본인의 결제대기 주문만 취소할 수 있습니다.
- 결제 완료 주문은 환불 API로 처리합니다.
- 주문 취소 시 주문 상태는 `CANCELED`, 결제 상태는 `FAILED`로 변경합니다.
- 주문 생성 때 줄여둔 상품 재고를 다시 복구합니다.
- 주문 취소 시 장바구니는 비우지 않습니다.
- 다시 결제하려면 새 주문을 생성해야 합니다.
