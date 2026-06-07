# 장바구니

## 상품 담기

Source: https://app.notion.com/p/699707ac8f5682488b53816fba25d40a

Properties:

```json
{"API Path":"/api/carts/items","Http Method":"POST","url":"https://app.notion.com/p/699707ac8f5682488b53816fba25d40a","기능 분류":"장바구니","담당자":"이동희","명세 기능":"상품 담기","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 장바구니에 상품을 담는 API 입니다.
- JWT Access Token을 통해 인증된 사용자만 요청할 수 있습니다.
- 같은 상품이 이미 장바구니에 존재하면 새 row를 생성하지 않고 기존 수량에 합산합니다.
- 요청 수량 또는 합산 수량이 상품 재고를 초과하면 실패합니다.
- 판매 중인 상품만 장바구니에 담을 수 있습니다.

## **02. 요청(Request)**

```plain text
POST /api/carts/items
```

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
|  |  | ✅ |  |

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| Authorization | Bearer {accessToken} 형식의 JWT 토큰 | ✅ | JWT 인증 토큰 |
| Content-Type | application/json | ✅ | 요청 데이터 타입 |

### **c. request body**

```json
{
  "productId": 10,
  "quantity": 2
}
```

| **이름** | **데이터타입** | 필수 여부 | **설명** |
| --- | --- | --- | --- |
| productId | Long | ✅ | 상품 ID |
| quantity | Integer | ✅ | 담을 수량 |

## **03. 응답(response)**

### **a. response header**

```json

```

### **b. response body**

**성공응답 :** `201 CREATED`

```json
{
  "success": true,
  "data": {
    "cartItemId": 1,
    "productId": 10,
    "poductName": "무선 키보드",
    "price": 30000,
    "quantity": 2,
    "totalAmount": 60000
  },
  "message": "장바구니 상품 담기 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| cartItemId | Long | 장바구니상품 ID |
| productId | Long | 상품 ID |
| poductName | String | 상품명 |
| price | Integer | 가격 |
| quantity | Integer | 수량 |
| totalAmount | Integer | 총 금액 |

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 400 | INVALID_QUANTITY | 수량이 1보다 작음 |
| 400 | OUT_OF_STOCK | 요청 수량 또는 합산 수량이 재고를 초과 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 404 | PRODUCT_NOT_FOUND | 상품없음 |
| 500 | SERVER_ERROR | 서버 내부 오류 |

## 장바구니 조회

Source: https://app.notion.com/p/43a707ac8f56834db3e7815a80533ba7

Properties:

```json
{"API Path":"/api/carts","Http Method":"GET","url":"https://app.notion.com/p/43a707ac8f56834db3e7815a80533ba7","기능 분류":"장바구니","담당자":"이동희","명세 기능":"장바구니 조회","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 현재 로그인한 회원의 장바구니 목록을 조회하는 API 입니다.
- JWT Access Token을 통해 인증된 사용자만 요청할 수 있습니다.
- 본인의 장바구니 상품만 조회할 수 있습니다.
- 장바구니에 담긴 상품 정보와 수량, 상품별 금액을 함께 조회합니다.
- 장바구니가 비어있는 경우 빈 목록을 반환합니다.

## **02. 요청(Request)**

```plain text
GET /api/carts
```

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

### **b. response body**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "cartItemId": 1,
        "productId": 1,
        "productName": "나이키 에어맥스",
        "price": 15000,
        "quantity": 2,
        "createdAt": "2026-04-27T15:19:52.026195",
        "updatedAt": "2026-04-27T15:19:52.026195"
      }
    ],
    "totalAmount": 30000
  },
  "message": "장바구니 조회 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| cartItemId | Long | 장바구니 ID |
| productId | Long | 상품 ID |
| productName | String | 상품 이름 |
| price | Integer | 가격 |
| quantity | Integer | 수량 |
| totalPrice | integer | 총금액 |
| createdAt | DateTime | 생성시간 |
| updateAt | DateTime | 수정시간 |

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 500 | SERVER_ERROR | 서버 내부 오류 |

## 장바구니 수량 변경

Source: https://app.notion.com/p/f38707ac8f5682199dd081300a1d4b2d

Properties:

```json
{"API Path":"/api/carts/items/\\{cartItemId\\}","Http Method":"PATCH","url":"https://app.notion.com/p/f38707ac8f5682199dd081300a1d4b2d","기능 분류":"장바구니","담당자":"이동희","명세 기능":"장바구니 수량 변경","작성 현황":"완료","참고할 부분":"각 아이템 수량을 개별로 변경","테스트 여부":"__NO__"}
```

## **01. 설명**

- 현재 로그인한 회원의 장바구니 상품 수량을 변경하는 API 입니다.
- JWT Access Token을 통해 인증된 사용자만 요청할 수 있습니다.
- 본인의 장바구니 상품만 수정할 수 있습니다.
- 변경하려는 수량이 상품 재고를 초과하면 실패합니다.
- quantity는 1 이상이어야 합니다.

## **02. 요청(Request)**

```plain text
PATCH /api/carts/items/{cartItemId}
```

### **c. request body**

```json
{
  "quantity": 3
}
```

### **b. response body**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": {
    "cartItemId": 1,
    "productId": 1,
    "productName": "나이키 에어맥스",
    "price": 159000,
    "quantity": 3
    "totalAmount" : 477000
  },
  "message": "장바구니 수량 변경 성공"
}
```

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 400 | INVALID_QUANTITY | 수량이 1보다 작음 |
| 400 | OUT_OF_STOCK | 변경 수량이 재고를 초과 |
| 403 | FORBIDDEN | 본인의 장바구니 상품이 아님 |
| 404 | CART_ITEM_NOT_FOUND | 장바구니 상품을 찾을 수 없음 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 500 | SERVER_ERROR | 서버 오류 |

## 장바구니 상품 개별 삭제

Source: https://app.notion.com/p/3c3707ac8f5682318e6a01253185edee

Properties:

```json
{"API Path":"/api/carts/items/\\{cartItemId\\}","Http Method":"DELETE","url":"https://app.notion.com/p/3c3707ac8f5682318e6a01253185edee","기능 분류":"장바구니","담당자":"이동희","명세 기능":"장바구니 상품 개별 삭제","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 현재 로그인한 회원의 장바구니 상품을 개별 삭제하는 API 입니다.
- JWT Access Token을 통해 인증된 사용자만 요청할 수 있습니다.
- 본인의 장바구니 상품만 삭제할 수 있습니다.
- 삭제 성공 시 응답 본문 없이 204 No Content를 반환합니다.

## **02. 요청(Request)**

```plain text
DELETE/api/carts/items/{cartItemId}
```

### **b. response body**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": null,
  "message": "장바구니 상품 삭제 성공"
}
```

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN | 본인의 장바구니 상품이 아님 |
| 404 | CART_ITEM_NOT_FOUND | 장바구니 상품을 찾을 수 없음 |
| 500 | SERVER_ERROR | 서버 오류 |
