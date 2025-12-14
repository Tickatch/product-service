# 좌석 예매 동시성 제어 전략

## 개요

티켓 예매 시스템에서 **좌석 차감**은 동시성 문제가 발생하기 쉬운 핵심 연산이다. 여러 사용자가 동시에 같은 상품의 좌석을 예매할 경우, 적절한 동시성 제어 없이는 **오버부킹**(실제 좌석보다 많은 예매)이 발생할 수 있다.

본 문서는 이 문제를 해결하기 위한 다양한 방안을 검토하고, **비관적 락(Pessimistic Locking)**을 선택한 근거를 정리한다.

---

## 문제 상황

```
시나리오: 잔여 좌석 1석, 사용자 A와 B가 동시에 예매 시도

[동시성 제어 없음]
사용자 A                    사용자 B
    │                          │
SELECT available = 1           │
    │                     SELECT available = 1
    │ (1 >= 1 ✓)               │ (1 >= 1 ✓)
UPDATE available = 0           │
    │                     UPDATE available = -1  ← 오버부킹!
    ✓                          ✓
```

---

## 동시성 제어 방안 비교

### 1. 비관적 락 (Pessimistic Locking)

**방식**: 데이터를 읽을 때 락을 걸어 다른 트랜잭션의 접근을 차단한다.

```sql
-- PostgreSQL / MySQL 공통
SELECT * FROM product WHERE id = 1 FOR UPDATE;
```

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdForUpdate(@Param("id") Long id);
```

| 장점 | 단점 |
|------|------|
| 구현이 단순하고 직관적 | 락 대기로 인한 처리량 감소 |
| 데이터 정합성 확실히 보장 | 데드락 가능성 (단일 테이블은 낮음) |
| 재시도 로직 불필요 | DB 커넥션 점유 시간 증가 |

---

### 2. 낙관적 락 (Optimistic Locking)

**방식**: 버전 컬럼을 두고, 업데이트 시점에 충돌을 감지한다.

```java
@Entity
public class Product {
    @Version
    private Long version;
}
```

```sql
UPDATE product 
SET available_seats = 99, version = 2 
WHERE id = 1 AND version = 1;
-- affected rows = 0 → 충돌 발생 → 재시도
```

| 장점 | 단점 |
|------|------|
| 락 대기 없음, 높은 처리량 | 충돌 시 재시도 로직 필요 |
| 읽기 위주 시스템에 적합 | 충돌 빈번하면 성능 저하 |
| 데드락 없음 | 사용자 경험 불안정 (재시도 중 실패 가능) |

---

### 3. 분산 락 (Distributed Lock)

**방식**: Redis, ZooKeeper 등 외부 시스템으로 락을 관리한다.

```java
// Redisson 예시
RLock lock = redisson.getLock("product:" + productId);
try {
    lock.lock(10, TimeUnit.SECONDS);
    // 좌석 차감 로직
} finally {
    lock.unlock();
}
```

| 장점 | 단점 |
|------|------|
| MSA 환경에서 여러 인스턴스 간 동기화 | 인프라 의존성 증가 |
| DB 부하 분산 | 네트워크 지연, 장애 포인트 추가 |
| 세밀한 타임아웃 제어 | 구현 복잡도 높음 |

---

### 4. DB 원자적 연산 (Atomic Update)

**방식**: 단일 UPDATE 문으로 조건 검사와 갱신을 동시에 수행한다.

```sql
UPDATE product 
SET available_seats = available_seats - 1 
WHERE id = 1 AND available_seats >= 1;
-- affected rows = 0 → 좌석 부족
```

| 장점 | 단점 |
|------|------|
| 가장 빠름, 락 불필요 | 비즈니스 로직이 SQL에 종속 |
| 단순한 연산에 최적 | 복잡한 검증 로직 적용 어려움 |
| 데드락 없음 | 도메인 모델 무력화 |

---

## PostgreSQL vs MySQL 동작 방식

### MVCC (Multi-Version Concurrency Control)

두 DB 모두 MVCC를 지원하여, **일반 SELECT는 락과 무관하게 스냅샷을 읽는다**.

| 구분 | PostgreSQL | MySQL (InnoDB) |
|------|------------|----------------|
| **스냅샷 시점** | 트랜잭션 시작 시 (기본) | 첫 번째 SELECT 시 |
| **격리 수준 기본값** | READ COMMITTED | REPEATABLE READ |
| **락 걸린 행 SELECT** | ✅ 스냅샷 읽기 (대기 없음) | ✅ 스냅샷 읽기 (대기 없음) |
| **락 걸린 행 FOR UPDATE** | ❌ 대기 | ❌ 대기 |

### 핵심 동작

```
트랜잭션 A (예매)              트랜잭션 B (조회)
      │                            │
SELECT ... FOR UPDATE              │
      │ ← 락 획득                   │
      │                       SELECT * FROM product
      │                            │ ← 스냅샷 읽기 (대기 없음)
      │                            ✓
UPDATE available_seats             │
COMMIT                             │
      │ ← 락 해제                   │
      ✓                            │
```

**결론**: 비관적 락을 사용해도 **조회 API는 영향받지 않는다**.

---

## 선택: 비관적 락

### 선택 근거

| 기준 | 판단 |
|------|------|
| **데이터 정합성** | 오버부킹은 비즈니스 치명적 → 최우선 |
| **트래픽 패턴** | 인기 공연은 동시 예매 집중 → 충돌 빈번 예상 |
| **실시간성 vs 안정성** | 약간의 대기는 허용, 데이터 손실은 불허 |
| **구현 복잡도** | 재시도 로직 없이 단순하게 유지 |
| **인프라 의존성** | Redis 등 추가 인프라 없이 DB만으로 해결 |

### 방안별 적합성 평가

| 방안 | 오버부킹 방지 | 구현 난이도 | 조회 영향 | 재시도 필요 | 적합도 |
|------|:---:|:---:|:---:|:---:|:---:|
| **비관적 락** | ✅ 확실 | 낮음 | 없음 | ❌ | ⭐⭐⭐ |
| 낙관적 락 | ✅ | 중간 | 없음 | ✅ | ⭐⭐ |
| 분산 락 | ✅ | 높음 | 없음 | ❌ | ⭐ |
| 원자적 연산 | ✅ | 낮음 | 없음 | ❌ | ⭐⭐ |

### 낙관적 락을 선택하지 않은 이유

1. **충돌 시 재시도 로직 필요**
    - 재시도 횟수, 간격, 최종 실패 처리 등 복잡도 증가
    - 사용자 경험 불안정 (예매 버튼 눌렀는데 "다시 시도하세요")

2. **인기 공연의 높은 충돌률**
    - 티켓팅 오픈 시 수백~수천 명 동시 접속
    - 충돌 → 재시도 → 또 충돌 악순환 가능

3. **비관적 락의 "느림"은 허용 가능**
    - 락 대기 시간은 수~수십 ms 수준
    - 사용자 입장에서 체감 차이 미미
    - 오히려 "순차 처리"가 공정성 보장

### 원자적 연산을 선택하지 않은 이유

1. **도메인 로직 분산**
    - `canPurchase()`, `isOnSale()` 등 검증이 SQL로 이동
    - 비즈니스 규칙 변경 시 SQL 수정 필요

2. **테스트 어려움**
    - 단위 테스트로 도메인 로직 검증 불가
    - 통합 테스트 의존도 증가

---

## 구현

### Repository

```java
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
```

### Service

```java
@Transactional
public void decreaseAvailableSeats(Long productId, int count) {
    // 비관적 락으로 조회 → 다른 트랜잭션 대기
    Product product = productRepository.findByIdForUpdate(productId)
        .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
    
    // 도메인에서 검증 및 차감
    product.decreaseAvailableSeats(count);
    
    // 트랜잭션 종료 시 락 해제
}
```

### 조회 (락 영향 없음)

```java
@Transactional(readOnly = true)
public ProductResponse getProduct(Long productId) {
    // 일반 SELECT → MVCC 스냅샷 읽기, 락 대기 없음
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
    
    return ProductResponse.from(product);
}
```

---

## 추가 고려사항

### 락 타임아웃 설정

장시간 락 점유 방지를 위해 타임아웃을 설정할 수 있다.

```java
@QueryHints({
    @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
})
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdForUpdate(@Param("id") Long id);
```

### 데드락 방지

단일 테이블 단일 행 락이므로 데드락 가능성은 낮다. 만약 여러 테이블을 락 걸어야 한다면 **락 순서를 통일**한다.

```java
// 항상 Product → Reservation 순서로 락
Product product = productRepository.findByIdForUpdate(productId);
Reservation reservation = reservationRepository.findByIdForUpdate(reservationId);
```

### 향후 확장

트래픽이 급증하여 DB 락이 병목이 되면:

1. **읽기 전용 복제본** 분리 (조회는 이미 락 영향 없음)
2. **Redis 분산 락**으로 전환 검토
3. **이벤트 기반 비동기 처리** (예매 요청 큐잉)

---

## 결론

| 선택 | 비관적 락 (PESSIMISTIC_WRITE) |
|------|------------------------------|
| **이유** | 오버부킹 방지가 최우선, 재시도 로직 없이 순차 처리로 안정성 확보 |
| **트레이드오프** | 약간의 처리량 감소 허용 (체감 미미) |
| **조회 영향** | 없음 (MVCC 스냅샷 읽기) |
| **구현** | `findByIdForUpdate()` + 도메인 검증 |

**"느리더라도 정확하게"** - 티켓 예매에서 오버부킹은 환불, 보상, 신뢰 손실로 이어진다. 실시간성보다 **데이터 정합성**을 우선하는 것이 올바른 선택이다.