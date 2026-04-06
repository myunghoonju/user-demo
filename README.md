# 회원 관리 서비스  

헥사고날 아키텍처 기반  
RabbitMQ 비동기 가입 처리, Redis 분산락·JWT 인증, AES-256 개인정보 암호화

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| Language | Java 25 |
| Framework | Spring Boot 3.5.13 |
| Database | MySQL 8 |
| Cache / 분산락 | Redis (Sentinel) |
| 메시지 큐 | RabbitMQ |
| Build | Gradle (Groovy) |

---

## 사전준비

### 1. 인프라 구동 (Docker)
docker-compose.yml 참고 (/script)
### 2. 스키마 생성
schema.sql 참고(/script)
## 애플리케이션 실행
`local` 프로파일 활성화 시 아래 계정이 자동 생성됩니다.(LocalDataSeeder.java)

| 역할 | 이메일 | 비밀번호 |
|------|--------|----------|
| ADMIN | admin@example.com | AdminPassword1! |
| USER | user1@example.com | UserPassword1! |
| USER | user2@example.com | UserPassword1! |
| USER | user3@example.com | UserPassword1! |

### 환경 변수 (선택)

기본값으로 로컬 실행 가능합니다. 

| 환경 변수 | 기본값 | 설명 |
|-----------|--------|------|
| `DB_URL` | `jdbc:mysql://localhost:3306/user_demo` | MySQL 접속 URL |
| `DB_USERNAME` | `root` | DB 사용자 |
| `DB_PASSWORD` | `1234` | DB 비밀번호 |
| `REDIS_SENTINEL_NODES` | `localhost:26379,localhost:26380,localhost:26381` | Redis Sentinel 노드 목록 |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ 호스트 |
| `JWT_SECRET` | 기본값 포함 | JWT 서명 키 (32바이트 이상) |
| `ENCRYPTION_KEY` | 기본값 포함 | AES-256 암호화 키 (32바이트) |

---

## 테스트

### 통합 테스트 (pc: mac)

```bash
./scripts/api-test.sh
```

19개 시나리오를 순서대로 실행합니다. 모두 통과 시 아래 출력이 나타납니다.

```
통과: 19 / 19
✔ 모든 테스트 통과
```

## Hands-on

`scripts/user-demo.postman_collection.json`  
Postman 또는 HTTPie Desktop에서 import하여 사용

---

## API

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | /api/v1/auth/signup | 불필요 | 회원가입 (비동기, 202 반환) |
| POST | /api/v1/auth/login | 불필요 | 로그인 (JWT 발급) |
| POST | /api/v1/auth/logout | USER·ADMIN | 로그아웃 |
| POST | /api/v1/auth/refresh | USER·ADMIN | Access Token 재발급 |
| GET | /api/v1/members/me | USER | 본인 정보 조회 |
| PUT | /api/v1/members/me | USER | 본인 정보 수정 |
| DELETE | /api/v1/members/me | USER | 회원 탈퇴 |
| GET | /api/v1/admin/members | ADMIN | 회원 목록 조회 (페이징) |
| GET | /api/v1/admin/members/{id} | ADMIN | 특정 회원 상세 조회 |

---

## 주요 설계 결정

설계 과정: [`docs/decisions.md`](docs/decisions.md)  
전체 아키텍처: [`docs/architecture.md`](docs/architecture.md)
