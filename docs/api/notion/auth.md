# 인증

## 회원가입

Source: https://app.notion.com/p/1d3707ac8f56825a935e0126101b3635

Properties:

```json
{"API Path":"/api/auth/signup","Http Method":"POST","url":"https://app.notion.com/p/1d3707ac8f56825a935e0126101b3635","기능 분류":"인증","담당자":"윤영범","명세 기능":"회원가입","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 회원가입을 위한 API 입니다
- 이메일은 UK 로 중복이메일 허용하지 않습니다
- 비밀번호는 암호화 저장합니다
- 회원가입시 point_balance(포인트내역) 0 으로 자동생성됩니다
- membership_level = NORMAL 로 자동생성됩니다

## **02. 요청(Request)**

```plain text
POST /api/auth/signup
```

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
|  |  |  |

### **b. request headers**

```json

{
  "Content-Type": "application/json"
}

```

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
| Content-Type | String | application/json |

### **c. request body**

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
| email | String | 이메일 |
| password | String | 비밀번호 |
| name | String | 이름 |
| phone_number | String | 전화번호 |

### **d. Validation 정책**

### email

```plain text
이메일 형식
중복 불가
```

### password

```plain text
8자 이상
영문 + 숫자 포함
```

### phone_number

```plain text
010-1234-5678 형식
```

## **03. 응답(respons)**

### **a. response header**

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
| Content-Type | String | application/json |

### **b. response body**

**성공응답:** `201 Created`

```json
{
  "success": true,
  "data": {
    "memberId": 1,
    "email": "test@test.com",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678",
    "pointBalance": 0,
    "membershipLevel": "NORMAL",
    "createdAt": "2026-06-01T12:00:00"
  },
  "message": "회원가입 성공"
}
```

| **이름** | 데이터타입 | **설명** |
| --- | --- | --- |
| member_id | Long | 유저아이디 |
| email | String | 이메일 |
| name | String | 이름 |
| phone_number | String | 전화번호 |
| point_balance | Integer | 포인트스냅샷 |
| membership_level | String | 멤버쉽등급 |
| created_at | DateTime | 생성시간 |

**에러 응답:**

| **이름** | **설명** |
| --- | --- |
| `400` | 필수값누락/형식오류 |
| `409` | 중복 이메일 |

## 로그인

Source: https://app.notion.com/p/85d707ac8f568299b22701ac45b1637c

Properties:

```json
{"API Path":"/api/auth/login","Http Method":"POST","url":"https://app.notion.com/p/85d707ac8f568299b22701ac45b1637c","기능 분류":"인증","담당자":"윤영범","명세 기능":"로그인","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 회원 로그인 API 입니다.
- 이메일과 비밀번호를 이용하여 회원 인증을 수행합니다.
- 인증 성공 시 JWT Access Token을 발급합니다.
- 발급된 Access Token은 이후 인증이 필요한 API 요청 시 Authorization Header에 포함하여 사용합니다.
- Refresh Token은 Access Token 만료 시 재발급 API에서 사용합니다.
- 이메일 또는 비밀번호가 일치하지 않는 경우 로그인이 실패합니다.

## **02. 요청(Request)**

```plain text
POST /api/auth/login
```

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
|  |  | ✅ |  |

### **b. request headers**

```json

{
  "Content-Type": "application/json"
}

```

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| Content-Type | String | ✅ | application/js |

### **c. request body**

```json
{
  "email": "test@test.com",
  "password": "password123"
}
```

| **이름** | **데이터타입** | 필수 여부 | **설명** |
| --- | --- | --- | --- |
| email | String | ✅ | 이메일 |
| password | String | ✅ | 비밀번호 |

## **03. 응답(respons)**

### **a. response header**

```json

```

| **이름** | **데이터타입** | **설명** |
| --- | --- | --- |
|  |  |  |

### **b. response body**

**성공응답 :** `200 OK`

```json
{
  "success": true,
  "data": {
    "accessToken": "Bearer JWT Access Token",
    "refreshToken": "JWT Refresh Token",
    "memberId": 1,
    "email": "test@test.com",
    "name": "홍길동"
  },
  "message": "로그인 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
| accessToken | String | 인증/인가 API 요청에 사용하는 JWT Access Token |
| refreshToken | String | Access Token 재발급에 사용하는 JWT Refresh Token |
| memberId | Long | 유저아이디 |
| email | String | 이메일 |
| name | String | 이름 |

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 400 | BAD REQUEST | `요청값 오류` |

## 로그아웃

Source: https://app.notion.com/p/01b707ac8f56832fa914012fd8548aa0

Properties:

```json
{"API Path":"/api/auth/logout","Http Method":"POST","url":"https://app.notion.com/p/01b707ac8f56832fa914012fd8548aa0","기능 분류":"인증","담당자":"윤영범","명세 기능":"로그아웃","작성 현황":"완료","참고할 부분":"","테스트 여부":"__NO__"}
```

## **01. 설명**

- 로그인한 회원이 로그아웃하기 위한 API 입니다.
- JWT Access Token을 통해 인증된 사용자만 요청할 수 있습니다.
- 로그아웃 시 클라이언트는 저장된 JWT 토큰을 삭제합니다.
- 서버는 토큰을 이용해 사용자를 식별한 후 로그아웃을 처리합니다.
- 본 프로젝트는 Stateless JWT 인증 방식을 사용하며 서버 세션은 생성하지 않습니다.

## **02. 요청(Request)**

```plain text
POST /api/auth/logout
```

### **a. Parameter & Querystring & URL**

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
|  |  | ✅ |  |

### **b. request headers**

```json

{
  "Authorization": "Bearer {accessToken}",
}
```

| **이름** | **데이터타입** | 필수여부 | **설명** |
| --- | --- | --- | --- |
| Authorization | String | ✅ | Bearer AccessToken |

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
  "data": null,
  "message": "로그아웃 성공"
}
```

| Key | **데이터타입** | **설명** |
| --- | --- | --- |
|  |  |  |
|  |  |  |

**에러 응답:**

| Status | Description | **설명** |
| --- | --- | --- |
| 200 | OK | 로그아웃성공 |
| 401 | UNAUTHORIZED | 인증되지 않거나 만료된 토큰 |
| 500 | SERVER ERROR | 서버내부오류버내부오류 |
