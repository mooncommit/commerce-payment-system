# Entity

## 기본 규칙

- Entity는 `BaseEntity`를 상속해 생성/수정 시간을 공통 관리한다.
- 기본 생성자는 JPA용으로 `protected` 접근을 사용한다.
- 연관관계는 기본적으로 LAZY를 사용한다.
- 상태 변경은 Entity 메서드로 감싸고, 외부에서 필드를 직접 변경하지 않는다.

## 상태 머신

- 상태 전이가 있는 enum은 `canTransitTo(target)`를 제공한다.
- Entity 상태 변경 메서드는 enum의 전이 가능 여부를 검증한다.
- 잘못된 상태 전이는 `BusinessException`과 도메인 `ErrorCode`로 막는다.

## 금액/수량

- 금액은 음수가 되지 않도록 service 또는 entity에서 검증한다.
- 주문 시점의 상품명/가격처럼 변경되면 안 되는 값은 주문 항목에 스냅샷으로 저장한다.
