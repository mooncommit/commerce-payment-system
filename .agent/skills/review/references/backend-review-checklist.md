# Backend Review Checklist

## Layering

* Controller / Service / Repository 책임 분리
* Entity ↔ DTO 분리
* 외부 API Client 역할 분리
* 도메인 의존성 순환 여부

## Validation

* Request DTO 검증
* 음수 금액, 0원 결제 방지
* 빈 상품 목록 방지
* 수량 검증
* 리소스 소유자 검증
* Enum 파라미터 검증

## Security

* 인증/인가 적용
* 본인 리소스만 접근 가능
* 민감 정보 응답 노출 여부
* 민감 정보 로그 노출 여부

## Transaction

* 결제 성공 시 상태 일관성
* 결제 실패 시 롤백
* 환불 시 상태/포인트 일관성
* 외부 API 연동 실패 처리

## Idempotency

다음 중복 요청을 고려하는가

* payment confirm 중복
* client confirm + webhook 동시 처리
* webhook 재전송
* 중복 환불 요청

권장

* portone_payment_id unique
* 상태 기반 중복 처리
* 처리 완료 요청 재호출 시 안전 응답
