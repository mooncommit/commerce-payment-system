# Auth API

## 회원가입

```http
POST /api/auth/signup
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
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678",
    "pointBalance": 0,
    "memberShip": "NORMAL",
    "createdAt": "2026-06-06T10:00:00"
  },
  "message": "회원가입성공"
}
```

## 로그인

```http
POST /api/auth/login
```

### Request

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "accessToken": "access-token",
    "refreshToken": "refresh-token"
  },
  "message": "로그인 되었습니다"
}
```

## 토큰 재발급

```http
POST /api/auth/reissue
```

### Request

```json
{
  "refreshToken": "refresh-token"
}
```

## 로그아웃

```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```
