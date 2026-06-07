# Validation

## Request 검증

- Controller의 request body에는 `@Valid`를 붙인다.
- Request DTO 필드에는 `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Pattern` 등을 사용한다.
- 수량, 금액처럼 DB 상태와 함께 봐야 하는 검증은 Service에서 처리한다.

## 예시

```java
@NotBlank(message = "PortOne 결제 ID는 필수입니다")
private String portonePaymentId;
```

## 주의

- 인증 회원 소유권 검증은 Bean Validation이 아니라 Service 계층에서 처리한다.
- 외부 API 응답 검증은 Facade 또는 infra adapter에서 처리한다.
