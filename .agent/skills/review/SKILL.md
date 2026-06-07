---

name: review
description: "코드 리뷰, 백엔드 설계 리뷰, 컨벤션 리뷰, 문서 정합성 리뷰를 요청하면 현재 변경사항 또는 지정 파일을 기준으로 리뷰한다."
-------------------------------------------------------------------------------------

## 역할

요청된 코드 또는 현재 브랜치 변경사항을 기준으로 코드 컨벤션, 백엔드 설계 품질, 문서 정합성을 리뷰한다.

코드 수정, 리팩토링 패치 작성, 전체 구현 코드 제공, 요청하지 않은 기능 추가는 하지 않는다.

## 기본 규칙

* 실제 코드와 문서에 근거해서만 리뷰한다.
* 문제 위치, 영향, 개선 방향, 우선순위를 명확히 쓴다.
* 필요한 경우에만 짧은 예시 코드를 제시한다.
* 문서와 코드가 다르면 명확히 지적한다.
* 테스트가 필요한 항목은 따로 제안한다.
* 학습용 결제 시스템 범위를 넘는 과한 설계는 제안하지 않는다.

## 범위 확인

문서를 바로 열지 말고 먼저 리뷰 범위를 좁힌다.

```shell
git status --short
git diff --stat
git diff --name-only
```

전체 diff는 필요한 경우에만 파일 단위로 확인한다.

```shell
git diff -- <file>
```

브랜치 기준 리뷰가 필요하면 `origin/master...HEAD` 기준으로 확인한다.

```shell
git diff --stat origin/master...HEAD
git diff --name-only origin/master...HEAD
git diff origin/master...HEAD -- <file>
```

## 문서 로딩 규칙

필요한 문서만 개별적으로 읽는다. 문서 전체를 한 번에 읽지 않는다.

* `README.md`: 실행 방법, 프로젝트 범위, 환경변수 정합성 확인 시
* `docs/CODE_CONVENTION.md`: 컨벤션 리뷰 요청 시. 인덱스로만 보고 필요한 `docs/conventions/*.md`만 읽기
* `docs/api/README.md`, `docs/api/{domain}.md`: Controller, DTO, endpoint, status code, error code, auth 변경 시
* `docs/ERD.md`: Entity, Repository, schema, enum, relation, nullable, unique 변경 시

파일이 없으면 리뷰를 중단하지 말고 문서 부재로 기록한다.

## Reference 적용

관련 있는 reference만 읽는다. 경로는 이 `SKILL.md`가 있는 디렉터리 기준이다.

* `references/backend-review-checklist.md`: 계층, DTO, validation, security, transaction, idempotency
* `references/payment-domain-review-checklist.md`: 주문, 결제, 환불, 웹훅, 포인트 도메인
* `references/documentation-consistency-checklist.md`: README, API, ERD 정합성
* `references/review-output-format.md`: 사용자가 정식 리뷰 템플릿을 요청한 경우만

관련 없는 reference를 예방 차원에서 읽지 않는다.

## 리뷰 기준

우선순위는 다음 순서로 본다.

1. 위험도 높은 문제
2. 백엔드 정합성 문제
3. 문서 불일치
4. 컨벤션 문제
5. 선택적 개선점

특히 결제, 환불, 웹훅은 멱등성, 상태 전이, 금액 검증, 소유자 검증을 확인한다.

## 결과 형식

기본 형식:

```markdown
## Findings

### High

-

### Medium

-

### Low

-

## Open Questions / Assumptions

-

## Summary

-

## Final Verdict

Approve | Approve with Comments | Request Changes | Blocked
```

## 과한 설계 제한

필요성이 명확하지 않으면 아래 제안은 피하고 Spring Boot + JPA + MySQL 구조 안에서 해결책을 제안한다.

* MSA
* Kafka
* Redis 분산락
* CQRS/Event Sourcing
* 복잡한 DDD 구조
* 다중 PG 추상화
* 운영급 모니터링 시스템
