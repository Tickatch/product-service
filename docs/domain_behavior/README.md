# 1차 Product 도메인 확장 행위 및 규칙

## 개요

Product 도메인 확장에 따라 추가되는 행위(메서드)와 비즈니스 규칙을 정의합니다.

---

## 1. Product 엔티티 - 추가 행위

| 구분 | 메서드 | 설명 | 관련 필드 |
|------|--------|------|-----------|
| **심사** | `approve()` | 상품 승인 | status |
| **심사** | `reject(String reason)` | 상품 반려 (사유 필수) | status, rejectionReason |
| **심사** | `resubmit()` | 반려 후 재제출 (REJECTED → DRAFT) | status, rejectionReason 초기화 |
| **장소** | `changeVenue(Venue venue)` | 장소 변경 (기존 changeStage 대체) | venue |
| **장소** | `updateVenueInfo(...)` | 이벤트로 비정규화 데이터 동기화 | venue |
| **좌석** | `decreaseAvailableSeats(int count)` | 예매 시 잔여 좌석 차감 | seatSummary |
| **좌석** | `increaseAvailableSeats(int count)` | 취소 시 잔여 좌석 복구 | seatSummary |
| **좌석** | `initializeSeatSummary(int total)` | 좌석 현황 초기화 | seatSummary |
| **통계** | `incrementViewCount()` | 조회수 증가 | stats |
| **통계** | `incrementReservationCount()` | 예매 수 증가 | stats |
| **통계** | `decrementReservationCount()` | 예매 취소 시 감소 | stats |
| **조회** | `isOwnedBy(String sellerId)` | 판매자 소유 여부 확인 | sellerId |
| **조회** | `canPurchase()` | 구매 가능 여부 (상태 + 예매 기간) | status, saleSchedule |

---

## 2. 비즈니스 규칙

### 2.1 심사 규칙

| 규칙 | 설명 |
|------|------|
| 승인 조건 | `PENDING` 상태에서만 승인 가능 |
| 반려 조건 | `PENDING` 상태에서만 반려 가능 |
| 반려 사유 필수 | 반려 시 `rejectionReason` 필수 입력 |
| 재제출 | `REJECTED` → `DRAFT` 전이 시 `rejectionReason` 초기화 |

### 2.2 예매 일정 규칙

| 규칙 | 설명 |
|------|------|
| 일정 순서 | `saleStartAt` < `saleEndAt` |
| 행사 전 마감 | `saleEndAt` ≤ `schedule.startAt` (행사 시작 전 예매 마감) |
| 예매 시작 | `saleStartAt` ≥ 현재 시간 (과거 불가) |

### 2.3 상태 전이 규칙

```
DRAFT ──→ PENDING ──→ APPROVED ──→ SCHEDULED ──→ ON_SALE ──→ CLOSED ──→ COMPLETED
                 │                                    │
                 ↓                                    │
              REJECTED                                │
                                                      ↓
                                                  CANCELLED
```

| 현재 상태 | 가능한 전이 | 조건 |
|-----------|------------|------|
| `DRAFT` | `PENDING`, `CANCELLED` | - |
| `PENDING` | `APPROVED`, `REJECTED`, `CANCELLED` | - |
| `APPROVED` | `SCHEDULED`, `CANCELLED` | 자동: saleStartAt 도래 전 |
| `REJECTED` | `DRAFT`, `CANCELLED` | 수정 후 재제출 |
| `SCHEDULED` | `ON_SALE`, `CANCELLED` | 자동: saleStartAt 도래 |
| `ON_SALE` | `CLOSED`, `CANCELLED` | - |
| `CLOSED` | `COMPLETED`, `CANCELLED` | 자동: schedule.endAt 도래 |
| `COMPLETED` | - | 최종 상태 |
| `CANCELLED` | - | 최종 상태 |

> **참고**: 매진 여부는 `SeatSummary.isSoldOut()`으로 판단합니다.

### 2.4 자동 상태 전이 (스케줄러)

| 전이 | 조건 | 주기 |
|------|------|------|
| `APPROVED` → `SCHEDULED` | 승인 완료 + 예매 시작 전 | 승인 즉시 |
| `SCHEDULED` → `ON_SALE` | saleStartAt 도래 | 매 분 |
| `ON_SALE` → `CLOSED` | saleEndAt 도래 | 매 분 |
| `CLOSED` → `COMPLETED` | schedule.endAt 도래 | 매 시간 |

### 2.5 장소 변경 규칙

| 규칙 | 설명 |
|------|------|
| 변경 가능 시점 | `schedule.startAt` 이전에만 가능 |
| 좌석 초기화 | 장소 변경 시 `seatSummary` 재설정 필요 |

### 2.6 판매자 규칙

| 규칙 | 설명 |
|------|------|
| 수정 권한 | `sellerId` 일치 시에만 수정/취소 가능 |
| 필수 값 | 상품 생성 시 `sellerId` 필수 |

---

## 3. ProductStatus - canChangeTo 확장

```java
public boolean canChangeTo(ProductStatus target) {
    return switch (this) {
        case DRAFT -> target == PENDING || target == CANCELLED;
        case PENDING -> target == APPROVED || target == REJECTED || target == CANCELLED;
        case APPROVED -> target == SCHEDULED || target == CANCELLED;
        case REJECTED -> target == DRAFT || target == CANCELLED;
        case SCHEDULED -> target == ON_SALE || target == CANCELLED;
        case ON_SALE -> target == CLOSED || target == CANCELLED;
        case CLOSED -> target == COMPLETED || target == CANCELLED;
        case COMPLETED, CANCELLED -> false;
    };
}
```

---

## 4. 서비스 레이어 변경

### ProductCommandService 추가 메서드

| 메서드 | 설명 |
|--------|------|
| `createProduct(..., sellerId, saleSchedule, venue)` | 확장된 생성 (기존 수정) |
| `approveProduct(productId)` | 상품 승인 |
| `rejectProduct(productId, reason)` | 상품 반려 |
| `resubmitProduct(productId)` | 반려 후 재제출 |
| `changeVenue(productId, venue)` | 장소 변경 (기존 changeStage 대체) |
| `updateSeatSummary(productId, total, available)` | 좌석 현황 동기화 |

### ProductQueryService 추가 메서드

| 메서드 | 설명 |
|--------|------|
| `getProductsBySeller(sellerId, pageable)` | 판매자별 상품 조회 |
| `getProductsByStatus(status, pageable)` | 상태별 상품 조회 |
| `getPendingProducts(pageable)` | 심사 대기 상품 조회 (관리자용) |

### ProductRepository 추가 메서드

| 메서드 | 설명 |
|--------|------|
| `findBySellerId(sellerId)` | 판매자별 조회 |
| `findByStatus(status)` | 상태별 조회 |
| `findByStatusAndSaleScheduleSaleStartAtBefore(status, time)` | 스케줄러용 |
| `findByStatusAndSaleScheduleSaleEndAtBefore(status, time)` | 스케줄러용 |
| `findByVenueStageId(stageId)` | 장소 이벤트 동기화용 |

---

## 5. 이벤트 리스너

| 이벤트 | 처리 |
|--------|------|
| `StageUpdatedEvent` | Product.venue 비정규화 데이터 동기화 |
| `SeatReservedEvent` | seatSummary.decreaseAvailable + stats.incrementReservation |
| `SeatReleasedEvent` | seatSummary.increaseAvailable + stats.decrementReservation |
| `ProductViewedEvent` | stats.incrementViewCount (Redis → 배치) |