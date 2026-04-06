# 아키텍처 설계

## 기술 스택

| 항목 | 선택 |
|------|------|
| Language | Java 25 |
| Framework | Spring Boot 3.5.13 |
| Database | MySQL (RDS) |
| Cache / 분산락 | Redis (Sentinel) |
| 메시지 큐 | RabbitMQ |
| Build | Gradle|

---

## 핵심 설계 결정

| 쟁점          | 선택 | 이유 |
|-------------|------|------|
| 급증 트래픽 처리   | RabbitMQ 비동기 큐 | DB 직접 부하 차단, 배압 제어 |
| 동시 중복 가입 방지 | Redis 분산락 + DB 유니크 제약 | 이중 방어: 락으로 1차, DB로 최종 보장 |
| 인증 방식       | JWT (Access 15분 + Refresh 7일) | 무상태 확장성, Redis로 로그아웃·블랙리스트 처리 |
| 개인정보 암호화    | BCrypt(PW) + AES-256(이름·전화번호) | 비밀번호는 단방향, 나머지는 조회 필요로 양방향 |
| 회원 탈퇴 처리    | 분리 테이블 보관 후 기간 만료 파기 | 개인정보보호법 준수 |

---

## 1. 전체 시스템 구조

```mermaid
graph TB
    subgraph 클라이언트
        USER[서비스 이용자]
        ADMIN_USER[관리자]
    end

    subgraph 애플리케이션_서버["애플리케이션 서버 (Spring Boot 3.5)"]
        direction TB

        subgraph 인바운드_어댑터["인바운드 어댑터 (Adapter-In)"]
            AUTH_API["인증 API\n/api/v1/auth\n(회원가입·로그인·로그아웃)"]
            MEMBER_API["회원 API\n/api/v1/members\n(본인 조회·수정·탈퇴)"]
            ADMIN_API["관리자 API\n/api/v1/admin/members\n(목록·상세 조회)"]
            SECURITY["Spring Security Filter\n(JWT 검증 · 역할 인가)"]
        end

        subgraph 애플리케이션_레이어["애플리케이션 레이어 (Use Cases)"]
            REG_UC["회원가입 유스케이스\n(큐 발행)"]
            LOGIN_UC["로그인 유스케이스\n(JWT 발급)"]
            LOGOUT_UC["로그아웃 유스케이스\n(토큰 무효화)"]
            MEMBER_UC["회원정보 유스케이스\n(조회·수정·탈퇴)"]
            ADMIN_UC["관리자 유스케이스\n(회원 목록·상세)"]
        end

        subgraph 도메인_레이어["도메인 레이어"]
            MEMBER_DOMAIN["Member 도메인 엔티티\n(비밀번호 해시·개인정보 암호화 정책)"]
        end

        subgraph 아웃바운드_어댑터["아웃바운드 어댑터 (Adapter-Out)"]
            JPA_ADAPTER["JPA 어댑터\n(MemberRepository)"]
            REDIS_ADAPTER["Redis 어댑터\n(분산락·토큰 저장)"]
            MQ_PUBLISHER["MQ Publisher\n(가입 요청 발행)"]
        end

        subgraph 비동기_처리["비동기 처리"]
            MQ_CONSUMER["RabbitMQ Consumer\n(회원가입 실행)"]
        end
    end

    subgraph 인프라
        MYSQL[("MySQL (RDS)\n회원 테이블\n탈퇴회원 분리 테이블")]
        REDIS[("Redis\n· Refresh Token\n· Access Token 블랙리스트\n· 분산락")]
        RMQ["RabbitMQ\n· signup.queue\n· signup.dlq (실패 처리)"]
    end

    USER --> AUTH_API
    USER --> MEMBER_API
    ADMIN_USER --> ADMIN_API

    AUTH_API --> SECURITY
    MEMBER_API --> SECURITY
    ADMIN_API --> SECURITY

    SECURITY --> REG_UC
    SECURITY --> LOGIN_UC
    SECURITY --> LOGOUT_UC
    SECURITY --> MEMBER_UC
    SECURITY --> ADMIN_UC

    REG_UC --> MQ_PUBLISHER
    LOGIN_UC --> MEMBER_DOMAIN
    LOGOUT_UC --> MEMBER_DOMAIN
    MEMBER_UC --> MEMBER_DOMAIN
    ADMIN_UC --> MEMBER_DOMAIN

    MEMBER_DOMAIN --> JPA_ADAPTER
    MEMBER_DOMAIN --> REDIS_ADAPTER

    MQ_PUBLISHER --> RMQ
    RMQ --> MQ_CONSUMER
    MQ_CONSUMER --> JPA_ADAPTER
    MQ_CONSUMER --> REDIS_ADAPTER

    JPA_ADAPTER --> MYSQL
    REDIS_ADAPTER --> REDIS
```
---

## 2. 프로젝트 모듈 구조

```
user-management/
├── domain/
│   ├── Member.java                  # 도메인 엔티티
│   ├── WithdrawnMember.java         # 탈퇴회원 (개인정보보호법 분리 보관)
│   └── vo/                          # Email, Password, PhoneNumber 값 객체
├── application/
│   ├── port/
│   │   ├── in/                      # UseCase 인터페이스 (인바운드 포트)
│   │   └── out/                     # Repository·Cache·MQ 포트 (아웃바운드 포트)
│   └── service/                     # 유스케이스 구현체
└── adapter/
    ├── in/
    │   └── web/                     # REST Controller, Request/Response DTO
    └── out/
        ├── persistence/             # JPA Entity, Repository 구현체
        ├── cache/                   # Redis 구현체 (토큰·분산락)
        └── messaging/               # RabbitMQ Publisher / Consumer
```

---

## 3. 프로젝트 계층구조

```mermaid
graph LR
    subgraph 외부
        REST["REST Client"]
        MQ_IN["RabbitMQ"]
    end

    subgraph 인바운드_포트["포트 (In)"]
        P_REG["RegisterMemberUseCase"]
        P_LOGIN["LoginUseCase"]
        P_MEMBER["ManageMemberUseCase"]
        P_ADMIN["AdminMemberUseCase"]
    end

    subgraph 도메인["도메인 (핵심)"]
        ENTITY["Member\nWithdrawnMember"]
        DS["도메인 서비스\n(중복검증·암호화·탈퇴처리)"]
    end

    subgraph 아웃바운드_포트["포트 (Out)"]
        P_REPO["LoadMemberPort\nSaveMemberPort"]
        P_CACHE["TokenStorePort\nLockPort"]
        P_MQ_OUT["PublishSignupPort"]
    end

    subgraph 외부_시스템
        MYSQL2[("MySQL")]
        REDIS2[("Redis")]
        RMQ2["RabbitMQ"]
    end

    REST --> P_REG & P_LOGIN & P_MEMBER & P_ADMIN
    MQ_IN --> DS

    P_REG --> DS
    P_LOGIN --> DS
    P_MEMBER --> DS
    P_ADMIN --> DS

    DS --> ENTITY
    DS --> P_REPO & P_CACHE & P_MQ_OUT

    P_REPO --> MYSQL2
    P_CACHE --> REDIS2
    P_MQ_OUT --> RMQ2
```

## 4. 회원가입 흐름

```mermaid
sequenceDiagram
    participant C as 클라이언트
    participant API as REST API
    participant MQ as RabbitMQ<br/>(signup.queue)
    participant CON as MQ Consumer
    participant REDIS as Redis<br/>(분산락)
    participant DB as MySQL

    C->>API: POST /api/v1/auth/signup<br/>{email, password, name, phone}
    API->>API: 입력값 유효성 검증
    API->>MQ: 가입 요청 메시지 발행
    API-->>C: 202 Accepted<br/>{email, name}

    Note over MQ,CON: 비동기 처리 시작

    MQ->>CON: 메시지 수신

    CON->>REDIS: 분산락 획득<br/>SETNX lock:email:{email}<br/>SETNX lock:phone:{phone}

    alt 락 획득 성공
        CON->>DB: 이메일·전화번호 중복 조회
        alt 중복 없음
            CON->>CON: BCrypt 비밀번호 해시<br/>AES-256 이름·전화번호 암호화
            CON->>DB: INSERT members<br/>(unique 제약: email, phone)
            CON->>REDIS: 락 해제
            Note over CON: 가입 완료
        else 중복 존재
            CON->>REDIS: 락 해제
            Note over CON: 가입 거부 (중복)
        end
    else 락 획득 실패 (동시 요청)
        CON-->>MQ: NACK → signup.dlq 이동
        MQ->>CON: DLQ 메시지 수신 (max 2 consumers)
        CON->>DB: 이메일·전화번호 존재 여부 조회
        alt 존재
            Note over CON: 폐기 (이미 가입 완료)
        else 없음
            CON->>DB: INSERT members
            Note over CON: DLQ 복구 완료
        end
    end
```

## 5. API 목록

| 메서드 | 경로 | 역할 | 설명                                       |
|--------|------|------|------------------------------------------|
| POST | /api/v1/auth/signup | 비인증 | 회원 가입 (비동기, 202 반환, email·name 포함)       |
| POST | /api/v1/auth/login | 비인증 | 로그인 (JWT 발급, email·name 포함)              |
| POST | /api/v1/auth/logout | USER·ADMIN | 로그아웃 (토큰 무효화)                            |
| POST | /api/v1/auth/refresh | USER·ADMIN | Access Token 재발급(유효기간 만료 후에도 사용자 편의성 보장) |
| GET | /api/v1/members/me | USER | 본인 정보 조회                                 |
| PUT | /api/v1/members/me | USER | 본인 정보 수정                                 |
| DELETE | /api/v1/members/me | USER | 회원 탈퇴                                    |
| GET | /api/v1/admin/members | ADMIN | 회원 목록 조회                                 |
| GET | /api/v1/admin/members/{id} | ADMIN | 특정 회원 상세 조회                              |
