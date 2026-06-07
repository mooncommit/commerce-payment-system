# DTO

## Request

- Controller 요청 바디는 Entity가 아니라 Request DTO로 받는다.
- 필수 값은 Bean Validation을 붙인다.
- 검증 메시지는 사용자가 이해할 수 있는 문장으로 작성한다.
- 요청 DTO는 기본 생성자가 필요하면 `@NoArgsConstructor`를 사용한다.

## Response

- 응답 DTO는 필요한 필드만 노출한다.
- Entity를 그대로 반환하지 않는다.
- Entity 변환 로직이 반복되면 `from(entity)` 정적 팩토리를 둔다.
- 목록 응답은 필요한 경우 `PageResponse<T>`를 사용한다.

## 주의

- 비밀번호, 토큰 원문, 외부 API secret은 응답 DTO에 포함하지 않는다.
- 금액 필드는 현재 프로젝트 기준 `Long`을 사용한다.
