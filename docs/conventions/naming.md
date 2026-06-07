# Naming

## 클래스

- Controller: `{Domain}Controller`
- Service: `{Domain}Service`, 유스케이스 조합은 `{Domain}CommandService` 또는 `{Domain}Facade`
- Repository: `{Entity}Repository`
- DTO: `{Action}{Domain}Request`, `{Action}{Domain}Response`
- Enum: 상태는 `{Domain}Status`, 타입은 `{Domain}Type`

## 메서드

- 조회: `find`, `get`, `exists`
- 생성: `create`
- 상태 변경: `markAs*`, `mark*`, `change*`
- 검증: `validate*`
- 외부 API 호출: `getPayment`, `cancelPayment`처럼 외부 동작을 드러낸다.

## DB 컬럼

- Java 필드는 camelCase를 사용한다.
- 명시가 필요한 컬럼은 `@Column(name = "snake_case")`로 DB 이름을 고정한다.
- 외부 시스템 식별자는 provider 이름을 포함한다. 예: `portonePaymentId`.
