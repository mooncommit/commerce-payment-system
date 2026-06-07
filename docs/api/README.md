# API 명세

이 폴더는 결제 시스템의 API 명세를 도메인별로 나눈 문서입니다.

공통 응답 형식, 인증 방식, 공통 Enum, 공통 에러 코드는 [common.md](./common.md)에만 정의합니다. 각 도메인 문서에는 해당 API의 요청/응답 데이터와 발생 가능한 에러 코드만 적습니다.

## 문서 목록

| 문서 | 설명 |
| --- | --- |
| [common.md](./common.md) | Base URL, 인증, 응답 wrapper, Enum, 에러 코드 |
| [auth.md](./auth.md) | 회원가입, 로그인, 토큰 재발급, 로그아웃 |
| [orders.md](./orders.md) | 바로 주문 생성, 장바구니 주문 생성 |
| [payments.md](./payments.md) | 결제 확정 |
| [points.md](./points.md) | 포인트 잔액 조회, 포인트 거래 내역 조회 |
| [refunds.md](./refunds.md) | 결제 환불 요청 |
| [portone.md](./portone.md) | PortOne 프론트 설정 조회 |
| [webhooks.md](./webhooks.md) | PortOne 웹훅 수신 |

## 엔드포인트 요약

| 도메인 | 기능 | Method | Path | 인증 |
| --- | --- | --- | --- | --- |
| 인증 | 회원가입 | POST | `/api/auth/signup` | 불필요 |
| 인증 | 로그인 | POST | `/api/auth/login` | 불필요 |
| 인증 | 토큰 재발급 | POST | `/api/auth/reissue` | 불필요 |
| 인증 | 로그아웃 | POST | `/api/auth/logout` | 필요 |
| 주문 | 상품 바로 주문 생성 | POST | `/api/orders` | 필요 |
| 주문 | 장바구니 주문 생성 | POST | `/api/carts/orders` | 필요 |
| 결제 | 결제 확정 | POST | `/api/payments/confirm` | 필요 |
| 포인트 | 포인트 잔액 조회 | GET | `/api/points/balance` | 필요 |
| 포인트 | 포인트 거래 내역 조회 | GET | `/api/points/history` | 필요 |
| 환불 | 결제 환불 요청 | POST | `/api/payments/{paymentId}/refunds` | 필요 |
| PortOne | 프론트 설정 조회 | GET | `/api/config/portone` | 불필요 |
| 웹훅 | PortOne 웹훅 수신 | POST | `/api/webhooks/portone` | PR #15 병합 후 반영 |

## 설계 메모

- 주문 생성 시 주문과 결제 대기 데이터를 함께 만든다.
- 결제 확정 시 PG 결제 금액과 DB 결제 금액을 검증한 뒤 주문을 확정한다.
- 포인트는 `members.point_balance`와 `point_ledger`를 함께 관리한다.
- 웹훅 상세 구현은 PR #15 병합 후 이 문서와 코드 기준을 맞춘다.
