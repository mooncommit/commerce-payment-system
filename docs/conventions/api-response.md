# API Response

## 성공 응답

```json
{
  "success": true,
  "data": {},
  "message": "처리 성공"
}
```

## 실패 응답

```json
{
  "success": false,
  "error": {
    "code": "COMMON_001",
    "message": "입력값이 올바르지 않습니다."
  }
}
```

## 규칙

- Controller는 `ApiResponse`로 응답을 감싼다.
- 성공 시 `data`가 없으면 message만 반환할 수 있다.
- 실패 시 `data`, `message`는 생략하고 `error`를 반환한다.
