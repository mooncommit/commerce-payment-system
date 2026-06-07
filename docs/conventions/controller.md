# Controller

## 역할

- HTTP 요청/응답 변환만 담당한다.
- 비즈니스 규칙은 Service 또는 Facade에 위임한다.
- 성공 응답은 `ApiResponse.success(...)`로 감싼다.
- 인증 회원은 `@AuthenticationPrincipal LoginMember`로 받는다.

## URI

- Base path는 `/api/{resources}` 형태를 사용한다.
- 하위 리소스는 상위 리소스 ID 아래에 둔다. 예: `/api/payments/{paymentId}/refunds`.
- 외부 연동 설정 조회처럼 도메인이 명확하지 않은 엔드포인트는 `/api/config/{provider}`를 사용한다.

## Status Code

- 생성 성공: `201 Created`
- 조회/처리 성공: `200 OK`
- 실패 응답은 `GlobalExceptionHandler`에서 공통 포맷으로 변환한다.
