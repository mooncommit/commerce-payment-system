# Follow Payment System

커머스 주문부터 결제, 포인트, 환불까지 이어지는 결제 도메인을 구현한 Spring Boot 기반 백엔드 프로젝트입니다. 주문 생성 시 재고를 선차감하고, PortOne 연동을 통해 카드 결제와 포인트 복합 결제를 처리하며, 웹훅과 클라이언트 결제 확정 요청이 중복되거나 순서가 바뀌어도 동일한 최종 상태로 수렴하도록 설계합니다.

> Spring Boot + JPA + MySQL 기반의 백엔드 API 서버로 구성되어 있습니다.

---

## 주요 기능

- JWT 기반 회원 인증과 소유권 검증
- 상품 조회, 장바구니, 주문 생성 및 재고 선차감
- PortOne 카드 결제, 포인트 결제, 복합 결제
- 결제 확정 API와 웹훅의 멱등 처리
- 포인트 원장 기반 적립/사용/복구 관리
- 포인트/PG 결제 비율 기반 부분 환불
- 멤버십 등급 및 구독 결제 확장 설계

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Framework** | Spring Boot 4.0.6 |
| **Language** | Java 17 |
| **ORM** | Spring Data JPA (Hibernate) |
| **Database** | MySQL 8 |
| **Security** | Spring Security + JWT |
| **Build Tool** | Gradle |

---

## 프로젝트 구조

```text
commerce-payment-system/
├── build.gradle                  # 프로젝트 빌드 설정
├── .env.template                 # 환경변수 템플릿 파일
└── src/
    ├── main/java/com/example/commercepaymentsystem/
    │   ├── CommercePaymentSystemApplication.java # 메인 애플리케이션
    │   ├── domain/                               # 주문, 결제, 포인트, 환불, 회원, 상품 도메인
    │   ├── global/                               # 공통 응답, 예외 처리, Security 등
    │   └── infra/                                # PortOne 외부 연동 웹훅 및 클라이언트
    ├── main/resources/
    │   ├── application.properties         # 공통 애플리케이션 설정 및 프로필
    │   ├── application-local.properties   # 로컬 개발용 설정 (ddl-auto 등)
    │   ├── application-prod.properties    # 운영 환경용 설정
    │   └── data.sql                       # 로컬용 초기 더미 데이터 주입
    └── test/http/
        └── *.http                         # IntelliJ API 테스트 스크립트 모음
```

---

## 패키지 및 제공 코드 설명

### `CommercePaymentSystemApplication.java`
Spring Boot 메인 클래스입니다. `@SpringBootApplication` 어노테이션으로 컴포넌트 스캔, 자동 설정, 설정 클래스 등록을 한 번에 처리합니다.

### `global/entity/BaseEntity.java`
모든 도메인 엔티티가 상속받는 **공통 시간 추적 엔티티**입니다. `@MappedSuperclass`로 선언되어 있으며, 생성 시각(`createdAt`)과 수정 시각(`updatedAt`)을 자동으로 기록합니다.

### 환경 설정 분리 (`application*.properties`)
- `application.properties`: `.env` 파일을 자동으로 불러오며 `local` 프로필을 기본으로 사용합니다.
- `application-local.properties`: 로컬 개발을 위해 DB 테이블을 자동 생성(`ddl-auto=create`)하고, `data.sql`을 실행해 더미 데이터를 세팅합니다.
- `application-prod.properties`: 운영 서버용 설정으로 DDL 자동 생성을 비활성화하여 데이터 유실을 방지합니다.

---

## 문서

- [ERD](./docs/ERD.md)
- [API 명세](./docs/api/README.md)
- [코드 컨벤션](./docs/CODE_CONVENTION.md)

---

## 시작하기

### 1. 환경 변수 세팅 (.env)

본 프로젝트는 결제 기능 실행 전 포트원 API 키 등 민감한 환경 변수 설정이 필요합니다.

1. 프로젝트 최상단(루트) 경로에 **`.env`** 파일을 생성합니다.
2. `.env.template` 파일의 내용을 복사하여 `.env`에 붙여넣고 본인의 API 키와 DB 비밀번호를 입력합니다.

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

Windows:
```powershell
.\gradlew.bat bootRun
```

### 3. API 테스트
서버 구동 후, `src/test/http/` 폴더 내에 있는 `.http` 스크립트를 통해 로그인, 주문, 결제, 환불 전체 시나리오를 인텔리제이 내부에서 바로 테스트할 수 있습니다.
