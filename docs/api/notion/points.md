# 포인트

## 포인트 잔액 조회

Source: https://app.notion.com/p/9f4707ac8f568201903a01455cbc5c4c

Properties:

```json
{"API Path":"/api/points/balance","Http Method":"GET","url":"https://app.notion.com/p/9f4707ac8f568201903a01455cbc5c4c","기능 분류":"포인트","담당자":"윤영범","명세 기능":"포인트 잔액 조회","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 현재 로그인한 회원의 포인트 잔액을 조회하는 API 입니다.
- JWT Access Token을 통해 인증된 사용자만 요청할 수 있습니다.
- 회원 테이블의 point_balance(포인트 잔액 스냅샷)를 조회합니다.
- 다른 회원의 포인트 정보는 조회할 수 없습니다.

## **02. 요청(Request)**

```plain text
GET /api/points/balance
```

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
|  |  | ✅ |  |

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}"
}
```

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| Authorization | String | ✅ | Bearer {accessToken} 형식의 JWT 토큰 |

### **c. request body**

```json

```

| **이름** | **데이터타입** | 필수 여부 | **설명** |
| --- | --- | --- | --- |
|  |  | ✅ |  |

## **03. 응답(respons)**

### **a. response header**

```json

```

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
|  |  |  |
|  |  |  |

### **b. response body**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "pointBalance": 10000
  },
  "message": "포인트 잔액 조회 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| memberId | INT | 조회한 사람의 ID |
| pointBalance | INT | 포인트잔액 스냅샷 |

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 404 | MEMBER_NOT_FOUND | 회원을 찾을 수 없음 |
| 500 | ServerError | 서버에러 |

## 포인트 거래 내역 조회

Source: https://app.notion.com/p/818707ac8f568368873101b8febc42dd

Properties:

```json
{"API Path":"/api/points/history","Http Method":"GET","url":"https://app.notion.com/p/818707ac8f568368873101b8febc42dd","기능 분류":"포인트","담당자":"윤영범","명세 기능":"포인트 거래 내역 조회","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 현재 로그인한 회원의 포인트 거래 내역을 조회하는 API 입니다.
- JWT Access Token을 통해 인증된 사용자만 요청할 수 있습니다.
- 적립(EARN), 사용(USE), 사용복구(REFUND), 회수(REVOKE) 내역을 조회할 수 있습니다.
- 최신 거래 내역 순으로 조회합니다.
- 페이지네이션을 지원합니다.

## **02. 요청(Request)**

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| page | Integer | 선택 | 조회할 페이지 번호. 기본값 0 |
| size | Integer | 선택 | 한 페이지에 조회할 포인트내역 개수. 기본값 10 |

### 요청 예시

```plain text
GET /api/points/history
GET /api/points/history?page=0&size=10
```

### **b. request headers**

```json
{
  "Authorization": "Bearer {accessToken}"
}
```

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| Authorization | String | ✅ | Bearer {accessToken} 형식의 JWT 토큰 |

### **c. request body**

```json

```

| **이름** | **데이터타입** | 필수 여부 | **설명** |
| --- | --- | --- | --- |
|  |  | ✅ |  |

## **03. 응답(respons)**

### **a. response header**

```json

```

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
|  |  |  |
|  |  |  |

### **b. response body**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "pointId": 1,
        "pointType": "EARN",
        "amount": 1000,
        "balanceAfter": 5000,
        "reason": "주문 적립",
        "createdAt": "2026-06-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "message": "포인트 거래내역 조회 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| pointId | INT | 포인트 ID |
| pointType | PointType | EARN,USE,REFUND,REVOKE |
| amount | INT | 거래포인트 |
| balanceAfter | INT | 거래후 잔액 |
| reason | String | 거래 설명 |
| createdAt | DateTime | 거래 발생 시각 |

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 404 | MEMBER_NOT_FOUND | 회원을 찾을 수 없음 |
| 500 | ServerError | 서버에러 |
