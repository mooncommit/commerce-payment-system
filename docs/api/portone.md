# PortOne Config API

## 프론트 설정 조회

```http
GET /api/config/portone
```

### Response

```json
{
  "success": true,
  "data": {
    "storeId": "store-id",
    "channelKey": "channel-key"
  },
  "message": "PortOne 설정 조회 성공"
}
```
