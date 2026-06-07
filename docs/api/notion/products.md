# 상품

## 상품 목록 조회

Source: https://app.notion.com/p/d5f707ac8f56833188da81604d7598d8

Properties:

```json
{"API Path":"/api/products","Http Method":"GET","url":"https://app.notion.com/p/d5f707ac8f56833188da81604d7598d8","기능 분류":"상품","담당자":"이동희","명세 기능":"상품 목록 조회","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

# 01. 설명

---

사용자가 판매 중인 전체 상품 목록을 조회할 때 사용합니다.

# 02. 요청(Request)

---

```plain text
GET /api/products
```

# 03. 응답(respons)

---

**성공응답:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "category": "가전제품",
        "productName": "소니 헤드폰 WH-1000XM5",
        "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
        "price": 380000,
        "quantity": 50,
        "status": "ON_SALE",
        "createdAt": "2026-06-01T15:40:00",
        "updatedAt": "2026-06-01T15:40:00"
      },
      {
        "id": 2,
        "category": "음식",
        "productName": "유기농 아몬드 브리즈 24팩",
        "description": "맛있고 가벼운 식물성 음료 아몬드 대용량 팩",
        "price": 180000,
        "quantity": 100,
        "status": "ON_SALE",
        "createdAt": "2026-06-01T16:00:00",
        "updatedAt": "2026-06-01T16:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "message": "상품 목록 조회 성공"
}
```

**에러 응답:**

| 이름 | 데이터타입 | 설명 |
| --- | --- | --- |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 에러 |

## 상품 단 건 조회

Source: https://app.notion.com/p/d90707ac8f56826c8a0c014d9d21e708

Properties:

```json
{"API Path":"/api/products/\\{productId\\}","Http Method":"GET","url":"https://app.notion.com/p/d90707ac8f56826c8a0c014d9d21e708","기능 분류":"상품","담당자":"이동희","명세 기능":"상품 단 건 조회","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

사용자가 특정 상품을 클릭했을 때, 해당 상품의 상세 정보를 조회하기 위해 사용합니다.

## **02. 요청(Request)**

```plain text
GET /api/products/{productId}
```

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| id | Long | ✅ | 상세 조회하고자 하는 상품의 고유 번호(PK) |

## **03. 응답(response)**

**성공응답 :** `201 CREATED`

```json
{
  "id": 1,
  "category": "가전제품",
  "productName": "소니 헤드폰 WH-1000XM5",
  "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
  "price": 380000,
  "quantity": 50,
  "status": "ON_SALE",
  "createdAt": "2026-06-01T15:40:00",
  "updatedAt": "2026-06-01T15:40:00"
}
```

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 404 | NOT_FOUND | 해당 상품 없음 |

## 상품 등록

Source: https://app.notion.com/p/73f707ac8f568291b7ba01d7225a22ce

Properties:

```json
{"API Path":"/api/products","Http Method":"POST","url":"https://app.notion.com/p/73f707ac8f568291b7ba01d7225a22ce","기능 분류":"상품","담당자":"이동희","명세 기능":"상품 등록","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

관리자(또는 판매자)가 새로운 상품을 시스템에 등록할 때 사용합니다.

## **02. 요청(Request)**

```plain text
POST /api/products/add
```

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}",
  "Content-Type": "application/json"
}
```

### **c. request body**

```json
{
  "category": "전자제품",
  "productName": "소니 헤드폰 WH-1000XM5",
  "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
  "price": 380000,
  "quantity": 50
}
```

## **03. 응답(respons)**

**성공응답 :** `201 CREATED`

```json
{
  "id": 1,
  "category": "전자제품",
  "productName": "소니 헤드폰 WH-1000XM5",
  "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
  "price": 380000,
  "quantity": 50,
  "status": "ON_SALE",
  "createdAt": "2026-06-01T15:40:00",
  "updatedAt": "2026-06-01T15:40:00"
}
```

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 400 | BAD_REQUEST | 필수 입력값 누락 또는 유효하지 않은 데이터 타입 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 403 | FORBIDDEN | 권한 없음 (일반 회원의 접근) |

## 상품 수정

Source: https://app.notion.com/p/79f707ac8f568260ba57815b2ff1cb0b

Properties:

```json
{"API Path":"/api/products/\\{productId\\}","Http Method":"PATCH","url":"https://app.notion.com/p/79f707ac8f568260ba57815b2ff1cb0b","기능 분류":"상품","담당자":"이동희","명세 기능":"상품 수정","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

관리자(또는 판매자)가 기존에 등록된 상품의 정보(카테고리, 이름, 설명, 가격, 재고)를 수정할 때 사용합니다.

## **02. 요청(Request)**

```plain text
PATCH /api/products/{id}
```

### **c. request body**

```json
{
  "category": "가전제품",
  "productName": "소니 헤드폰 WH-1000XM5",
  "price": 395000
}
```

## **03. 응답(respons)**

**성공응답 :** `201 CREATED`

```json
{
  "id": 1,
  "category": "가전제품",
  "productName": "소니 헤드폰",
  "description": "최고의 노이즈 캔슬링을 자랑하는 블루투스 헤드폰",
  "price": 395000,
  "quantity": 30,
  "status": "ON_SALE",
  "createdAt": "2026-06-01T15:40:00",
  "updatedAt": "2026-06-01T16:15:00"
}
```

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 400 | BAD_REQUEST | 유효하지 않은 데이터 입력 |
| 403 | FORBIDDEN | 권한 없음 |
| 404 | NOT_FOUND | 해당 상품 없음 |

## 상품 삭제

Source: https://app.notion.com/p/833707ac8f568254a6cd817d5cde414d

Properties:

```json
{"API Path":"/api/products/\\{productId\\}","Http Method":"DELETE","url":"https://app.notion.com/p/833707ac8f568254a6cd817d5cde414d","기능 분류":"상품","담당자":"이동희","명세 기능":"상품 삭제","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

관리자(또는 판매자)가 더 이상 판매하지 않는 상품을 삭제(또는 판매 중단) 처리할 때 사용합니다.

## **02. 요청(Request)**

```plain text
DELETE api/products/{id}
```

### **b. request headers**

```json
"Authorization": "Bearer {accessToken}"
```

## **03. 응답(respons)**

**성공응답 :** `200 OK`

```json
{
  "message": "상품이 성공적으로 삭제되었습니다.",
}
```

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 인증되지 않은 사용자 |
| 403 | FORBIDDEN | 권한 없음 |
| 404 | NOT_FOUND | 해당 상품 없음 |
