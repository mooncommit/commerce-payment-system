# Repository

## 기본 규칙

- Repository는 Spring Data JPA 인터페이스로 작성한다.
- 복잡한 조회나 N+1 방지가 필요한 조회는 `@Query`와 fetch join을 사용한다.
- Optional을 반환해 호출부에서 not found를 명확히 처리한다.

## 조회 메서드

- ID 기반 단건 조회: `findById`
- 연관관계 포함 조회: `findByIdWithOrder`, `findByPortonePaymentIdWithOrder`
- 존재 확인: `existsBy...`

## 주의

- Repository에서 비즈니스 예외를 직접 던지지 않는다.
- 예외 변환은 Service 계층에서 처리한다.
