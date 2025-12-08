# 1차 Product 도메인 확장 문서

## 개요

Product 도메인의 확장 계획을 정의합니다. 핵심 비즈니스 필드, 비정규화, 상태 세분화를 한번에 적용합니다.

---

## 1. ProductStatus 확장

기존 enum에 새로운 상태 값을 추가합니다.

| 구분 | 값 | 설명 | 변경  |
|------|-----|------|-----|
| 등록 | `DRAFT` | 임시저장 | 기존  |
| 등록 | `PENDING` | 심사대기 | 기존  |
| 등록 | `APPROVED` | 승인됨 | new |
| 등록 | `REJECTED` | 반려됨 | new  |
| 판매 | `SCHEDULED` | 예매예정 (승인 후, 예매 시작 전) | new  |
| 판매 | `ON_SALE` | 판매중 | 기존  |
| 종료 | `CLOSED` | 판매종료 (예매 기간 종료) | new  |
| 종료 | `COMPLETED` | 행사종료 | new  |
| 종료 | `CANCELLED` | 취소됨 | 기존  |

### 상태 전이 규칙

```
DRAFT ──→ PENDING ──→ APPROVED ──→ SCHEDULED ──→ ON_SALE ──→ CLOSED ──→ COMPLETED
  │          │           │                          │
  │          ↓           │                          │
  │       REJECTED       │                          │
  │          │           │                          │
  └──────────┴───────────┴──────────────────────────┴──────────→ CANCELLED
```

> **참고**: 매진 여부는 `SeatSummary.isSoldOut()`으로 판단합니다. 상태(Status)가 아닌 좌석 현황으로 관리합니다.

---

## 2. 신규 VO 목록

### 2.1 SaleSchedule

예매 일정을 관리하는 Value Object입니다.

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `saleStartAt` | `LocalDateTime` | ✅ | 예매 시작 시간 |
| `saleEndAt` | `LocalDateTime` | ✅ | 예매 종료 시간 |

**주요 메서드**

```java
boolean isSaleStarted()    // 예매 시작 여부
boolean isSaleEnded()      // 예매 종료 여부
boolean isInSalePeriod()   // 예매 가능 기간 여부
```

---

### 2.2 Venue

장소 정보를 비정규화하여 관리하는 Value Object입니다.

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `stageId` | `Long` | ✅ | 스테이지 ID (기존 필드 이동) |
| `stageName` | `String` | ✅ | 스테이지명 (비정규화) |
| `artHallId` | `Long` | ✅ | 아트홀 ID |
| `artHallName` | `String` | ✅ | 아트홀명 (비정규화) |
| `artHallAddress` | `String` | ✅ | 주소 (비정규화) |

**주요 메서드**

```java
Venue updateInfo(String stageName, String artHallName, String artHallAddress)  // 정보 업데이트
```

---

### 2.3 SeatSummary

좌석 현황을 관리하는 Value Object입니다.

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `totalSeats` | `Integer` | ❌ | 총 좌석 수 |
| `availableSeats` | `Integer` | ❌ | 잔여 좌석 수 |
| `updatedAt` | `LocalDateTime` | ❌ | 마지막 동기화 시간 |

**주요 메서드**

```java
int getSoldSeats()                    // 판매된 좌석 수
double getSoldRate()                  // 판매율 (%)
boolean isSoldOut()                   // 매진 여부
void decreaseAvailable(int count)     // 예매 시 차감
void increaseAvailable(int count)     // 취소 시 증가
```

---

### 2.4 ProductStats

통계 정보를 관리하는 Value Object입니다.

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `viewCount` | `Long` | ❌ | 조회수 (기본값 0) |
| `reservationCount` | `Integer` | ❌ | 예매 수 (기본값 0) |

**주요 메서드**

```java
void syncViewCount(Long count)      // Redis에서 배치 동기화
void incrementReservation()         // 예매 시 증가
void decrementReservation()         // 취소 시 감소
```

---

## 3. Product 엔티티 변경 요약

| 구분 | 필드 | 타입 | 변경 |
|------|------|------|------|
| 기본 | `id` | `Long` | 기존 |
| 기본 | `name` | `String` | 기존 |
| 기본 | `productType` | `ProductType` | 기존 |
| 기본 | `runningTime` | `Integer` | 기존 |
| 일정 | `schedule` | `Schedule` | 기존 |
| 일정 | `saleSchedule` | `SaleSchedule` | new |
| 판매자 | `sellerId` | `String` | new |
| 심사 | `rejectionReason` | `String` | new |
| 장소 | ~~`stageId`~~ | ~~`Long`~~ | ❌ 제거 → Venue로 이동 |
| 장소 | `venue` | `Venue` | new |
| 좌석 | `seatSummary` | `SeatSummary` | new |
| 통계 | `stats` | `ProductStats` | new |
| 상태 | `status` | `ProductStatus` | 기존 (값 확장) |

---

## 4. 전체 구조

```
Product
├── 기본 정보
│   ├── id: Long
│   ├── name: String
│   ├── productType: ProductType
│   └── runningTime: Integer
│
├── 판매자 정보 (new)
│   └── sellerId: String
│
├── 일정 정보
│   ├── schedule: Schedule (기존)
│   │   ├── startAt: LocalDateTime
│   │   └── endAt: LocalDateTime
│   │
│   └── saleSchedule: SaleSchedule (new)
│       ├── saleStartAt: LocalDateTime
│       └── saleEndAt: LocalDateTime
│
├── 장소 정보 (new)
│   └── venue: Venue
│       ├── stageId: Long
│       ├── stageName: String
│       ├── artHallId: Long
│       ├── artHallName: String
│       └── artHallAddress: String
│
├── 좌석 현황 (new)
│   └── seatSummary: SeatSummary
│       ├── totalSeats: Integer
│       ├── availableSeats: Integer
│       └── updatedAt: LocalDateTime
│
├── 통계 (new)
│   └── stats: ProductStats
│       ├── viewCount: Long
│       └── reservationCount: Integer
│
├── 심사 (new)
│   └── rejectionReason: String
│
└── 상태
    └── status: ProductStatus (확장)
```

---

## 5. DB 컬럼 추가

| 컬럼명 | 타입 | 출처 |
|--------|------|------|
| `seller_id` | `VARCHAR(50)` | Product |
| `sale_start_at` | `TIMESTAMP` | SaleSchedule |
| `sale_end_at` | `TIMESTAMP` | SaleSchedule |
| `rejection_reason` | `VARCHAR(500)` | Product |
| `stage_name` | `VARCHAR(100)` | Venue |
| `art_hall_id` | `BIGINT` | Venue |
| `art_hall_name` | `VARCHAR(100)` | Venue |
| `art_hall_address` | `VARCHAR(200)` | Venue |
| `total_seats` | `INT` | SeatSummary |
| `available_seats` | `INT` | SeatSummary |
| `seat_updated_at` | `TIMESTAMP` | SeatSummary |
| `view_count` | `BIGINT` | ProductStats |
| `reservation_count` | `INT` | ProductStats |

**총 13개 컬럼 추가** (기존 `stage_id`는 유지, Venue 내부에서 사용)