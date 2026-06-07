# ERD

이 문서는 현재 코드의 JPA Entity를 기준으로 작성한 논리 ERD입니다.

## Tables

### members

| 컬럼 | 설명 |
| --- | --- |
| id | 회원 PK |
| email | 이메일, unique |
| password_hash | 비밀번호 해시 |
| name | 이름 |
| phone_number | 전화번호 |
| point_balance | 현재 포인트 잔액 |
| member_ship | 회원 등급 |
| created_at, updated_at | 공통 시간 |

### products

| 컬럼 | 설명 |
| --- | --- |
| id | 상품 PK |
| category_code | 카테고리 코드 |
| name | 상품명 |
| description | 설명 |
| price | 가격 |
| stock_quantity | 재고 수량 |
| sale_status | 판매 상태 |

### carts

| 컬럼 | 설명 |
| --- | --- |
| id | 장바구니 PK |
| member_id | 회원 FK |

### cart_items

| 컬럼 | 설명 |
| --- | --- |
| id | 장바구니 상품 PK |
| cart_id | 장바구니 FK |
| product_id | 상품 FK |
| quantity | 수량 |

### orders

| 컬럼 | 설명 |
| --- | --- |
| id | 주문 PK |
| order_number | 주문 번호, unique |
| member_id | 회원 FK |
| order_status | 주문 상태 |
| total_amount | 총 상품 금액 |
| used_point_amount | 사용 포인트 |
| pg_amount | PG 결제 금액 |
| earned_point_amount | 적립 예정 포인트 |
| paid_at | 결제 완료 시각 |
| canceled_at | 취소 시각 |

### order_items

| 컬럼 | 설명 |
| --- | --- |
| id | 주문 상품 PK |
| order_id | 주문 FK |
| product_id | 상품 FK |
| product_name | 주문 당시 상품명 |
| unit_price | 주문 당시 단가 |
| quantity | 수량 |
| line_total_amount | 라인 총액 |

### payments

| 컬럼 | 설명 |
| --- | --- |
| id | 결제 PK |
| order_id | 주문 FK, unique |
| portone_payment_id | PortOne 결제 ID, unique |
| portone_transaction_id | PortOne 거래 ID |
| status | 결제 상태 |
| payment_method_type | 결제 수단 타입 |
| paid_at | 결제 완료 시각 |
| failed_at | 결제 실패 시각 |
| failure_reason | 실패 사유 |

### point_ledger

| 컬럼 | 설명 |
| --- | --- |
| id | 포인트 원장 PK |
| member_id | 회원 ID |
| payment_id | 결제 ID |
| point_type | 포인트 유형 |
| amount | 변동 포인트 |
| balance_after | 변동 후 잔액 |
| reason | 사유 |

### refunds

| 컬럼 | 설명 |
| --- | --- |
| id | 환불 PK |
| payment_id | 결제 FK, unique |
| refund_pg_amount | PG 환불 금액 |
| refund_point_amount | 포인트 환불 금액 |
| refund_status | 환불 상태 |
| reason | 환불 사유 |
| refunded_at | 환불 완료 시각 |

## Relationships

```text
members 1 -- 1 carts
members 1 -- N orders
carts 1 -- N cart_items
products 1 -- N cart_items
orders 1 -- N order_items
products 1 -- N order_items
orders 1 -- 1 payments
payments 1 -- 1 refunds
members 1 -- N point_ledger (logical)
payments 1 -- N point_ledger (logical)
```

## State Machines

### OrderStatus

```text
PAYMENT_PENDING -> COMPLETED
PAYMENT_PENDING -> CANCELED
COMPLETED -> CANCELED
CANCELED -> terminal
```

### PaymentStatus

```text
PENDING -> COMPLETED
PENDING -> FAILED
COMPLETED -> CANCELED
COMPLETED -> REFUNDED
FAILED, CANCELED, REFUNDED -> terminal
```
