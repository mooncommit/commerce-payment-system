# Webhooks API

## PortOne 결제 상태 변경 웹훅 수신

```http
POST /api/payments/webhooks/portone
```

### Headers

```json
{
  "Content-Type": "application/json",
  "webhook-id": "{webhookId}",
  "webhook-timestamp": "{timestamp}",
  "webhook-signature": "{signature}"
}
```

### Request

```json
{
  "type": "Transaction.Paid",
  "data": {
    "paymentId": "payment-7f3e8d2a-8b4e-4c0b-9f51-123456789abc",
    "storeId": "store-id",
    "transactionId": "transaction-id"
  }
}
```

### Response

```json
{
  "success": true,
  "data": null,
  "message": "웹훅 수신 성공"
}
```
