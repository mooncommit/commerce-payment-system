# Package Structure

## 기본 구조

```text
com.example.commercepaymentsystem
├── domain
│   └── {domain}
│       ├── controller
│       ├── dto
│       ├── entity
│       ├── enums
│       ├── facade
│       ├── repository
│       └── service
├── global
│   ├── config
│   ├── dto
│   ├── entity
│   ├── exception
│   └── response
└── infra
    └── {external-system}
```

## 규칙

- 비즈니스 도메인은 `domain/{domain}` 아래에 둔다.
- 외부 시스템 연동은 `infra/{provider}` 아래에 둔다.
- 공통 응답, 예외, 설정, BaseEntity는 `global` 아래에 둔다.
- 도메인 서비스가 외부 구현체를 직접 알 필요가 없으면 `port` 인터페이스를 둔다.
- 단순 CRUD가 아닌 여러 도메인을 묶는 흐름은 `facade` 또는 command 성격의 service로 분리한다.
