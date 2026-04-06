#  통합 테스트

급증하는 요청에 대하여 Consumer 레이어의 데이터 정합성을 검증  
RabbitMQ → Consumer → DB 파이프라인에서 락과 중복 검사 정상처리 여부확인

---

## 개요

```
API 서버 (202 즉시 반환)
    └─► RabbitMQ signup.queue (버퍼 역할)
            └─► Consumer 인스턴스 × N (maxConcurrentConsumers 조절)
                    └─► Redis 락 → MySQL INSERT
```

| 구간 | 병목 제거 방법 |
|------|--------------|
| API → MQ | 202 즉시 반환으로 스레드 블로킹 없음 |
| MQ → Consumer | Consumer 수 조절로 처리량 탄력적 제어 |
| Consumer → DB | 락으로 중복 방지, DB 유니크 제약으로 최종 보장 |
| 락 획득 실패 | DLQ → 재처리 (정상 요청 누락 방지) |

---

## 테스트 환경

| 항목 | 내용 |
|------|------|
| 테스트 대상 | `SignupConsumer` (RabbitMQ Consumer) |
| 락 구현체 | `InMemoryLockPort` (ReentrantLock 기반, Redis 대체) |
| 스레드 풀 | `newFixedThreadPool(100)` |
| 동기화 방식 | `CountDownLatch` (모든 스레드 동시 출발 보장) |
| 실행 환경 | Apple Mac mini (M2), JDK 25 |

---

## 테스트 케이스 1 — 동일 이메일 100개 동시 요청

### 시나리오

동일한 이메일 `duplicate@example.com`으로 100개의 스레드가 동시에 가입  
락과 중복 검사를 통해 단 1건만 저장

### 결과

```
총 요청:              100건
저장 성공:              1건 
락 획득 실패 (DLQ 이동): 99건
소요 시간:             87ms
```

### 분석

- 100개 중 1개만 락 획득에 성공 → DB 저장 1건
- 나머지 99개는 `tryLock()` 실패로 `IllegalStateException` 발생 → DLQ 이동
- 데이터 무결성 완전 보장 (중복 저장 0건)

---

## 테스트 케이스 2 — 서로 다른 이메일 100개 동시 요청

### 시나리오

각기 다른 이메일을 가진 100개의 스레드가 동시에 가입  
모든 요청이 정상 처리

### 결과

```
총 요청:   100건
저장 성공: 100건   ← 누락 없음
소요 시간: 204ms
처리량:    490 req/s (Consumer 단일 인스턴스 기준)
```

### 분석

- 키가 다르므로 락 충돌 없이 모든 요청이 병렬 처리됨
- 단일 인스턴스 기준 약 490 req/s
- `maxConcurrentConsumers` 설정으로 Consumer를 처리량 조절 가능

---

## 테스트 코드

`SignupConcurrencyTest.java`
