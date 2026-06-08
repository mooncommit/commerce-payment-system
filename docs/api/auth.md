# Auth API

## 회원가입

```http
POST /api/auth/signup
```

### Headers

```json
{
  "Content-Type": "application/json"
}
```

### Request

```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```

### Response

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

### Errors

- `400 BAD_REQUEST`: 필수 입력값 누락 또는 형식 오류
- `409 EMAIL_ALREADY_EXISTS`: 이미 가입된 이메일
## 로그인

```http
POST /api/auth/login
```

### Headers

```json
{
  "Content-Type": "application/json"
}
```

### Request

```json
{
  "email": "test@test.com",
  "password": "password123"
}
```

### Response

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

### Errors

- `400 BAD_REQUEST`: 이메일/비밀번호 미입력
- `401 INVALID_CREDENTIALS`: 이메일 또는 비밀번호 불일치
## 토큰 재발급

```http
POST /api/auth/reissue
```

### Headers

```json
{
  "Content-Type": "application/json"
}
```

### Request

```json
{
  "refreshToken": "refresh-token"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "accessToken": "Bearer JWT Access Token",
    "refreshToken": "JWT Refresh Token"
  },
  "message": "토큰 재발급 성공"
}
```

### Errors

- `401 INVALID_TOKEN`: 유효하지 않거나 만료된 Refresh 토큰
## 로그아웃

```http
POST /api/auth/logout
```

### Headers

```json
{
  "Authorization": "Bearer {accessToken}"
}
```

### Response

```json
{
  "success": true,
  "data": null,
  "message": "로그아웃 성공"
}
```

### Errors

- `401 UNAUTHORIZED`: 인증되지 않은 사용자 (토큰 만료 등)

