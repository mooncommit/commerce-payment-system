# Transaction

## 기본 규칙

- DB 상태를 변경하는 Service 메서드에는 `@Transactional`을 붙인다.
- 조회 전용 메서드는 필요하면 `@Transactional(readOnly = true)`를 사용한다.
- 외부 API 호출은 가능하면 트랜잭션 밖에서 수행한다.

## 결제 흐름

- PG 조회 또는 취소 요청은 Facade/infra에서 수행한다.
- PG 결과 검증 후 내부 DB 변경은 command service에서 하나의 트랜잭션으로 처리한다.
- 결제, 주문, 재고, 포인트, 환불 상태가 함께 바뀌면 같은 트랜잭션에 둔다.

## 주의

- 트랜잭션 안에서 긴 외부 API 호출을 하지 않는다.
- 상태 변경 중 예외가 발생하면 전체 롤백되어야 한다.
