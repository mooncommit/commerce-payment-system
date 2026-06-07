# Dummy Data

로컬 개발과 API 테스트에 사용할 수 있는 기본 데이터 예시입니다.

## Members

| email | password | name | phoneNumber | pointBalance |
| --- | --- | --- | --- | --- |
| user1@example.com | password123 | 홍길동 | 010-1111-1111 | 10000 |
| user2@example.com | password123 | 김철수 | 010-2222-2222 | 5000 |

## Products

| categoryCode | name | price | stockQuantity | saleStatus |
| --- | --- | ---: | ---: | --- |
| BOOK | 클린 코드 | 30000 | 10 | ON_SALE |
| DIGITAL | 무선 마우스 | 25000 | 20 | ON_SALE |
| FOOD | 드립 커피 | 12000 | 30 | ON_SALE |

## Direct Order Request

```json
{
  "productId": 1,
  "quantity": 1,
  "usePointAmount": 1000
}
```

## Cart Order Request

```json
{
  "cartItemIds": [1, 2],
  "usePointAmount": 1000
}
```

## Payment Confirm Request

```json
{
  "paymentId": 1,
  "portonePaymentId": "pay_test_001"
}
```

## Refund Request

```json
{
  "reason": "단순 변심"
}
```
