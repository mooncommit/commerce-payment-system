# PortOne Config API

## 프론트 설정 조회

```http
GET /api/config/portone
```

PortOne 결제창 호출에 필요한 공개 설정만 반환한다.

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

## 주의

- `apiSecret`은 서버 전용 값이므로 응답에 포함하지 않는다.
- 프론트에서 필요한 공개 값만 내려준다.
