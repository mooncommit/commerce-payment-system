# Exception

## 기본 구조

- 비즈니스 예외는 `BusinessException`을 사용한다.
- JWT 예외는 `JwtTokenException`을 사용한다.
- 에러 코드는 `ErrorCode` enum에 도메인별로 추가한다.
- 응답은 `ApiResponse.error(...)` 형식으로 내려간다.

## ErrorCode 작성

```java
PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "결제 정보를 찾을 수 없습니다.")
```

- HTTP 상태, 서비스 코드, 사용자 메시지를 함께 정의한다.
- 도메인별 prefix를 맞춘다. 예: `PAYMENT_`, `ORDER_`, `REFUND_`.
- 같은 의미의 에러 코드를 중복 생성하지 않는다.
