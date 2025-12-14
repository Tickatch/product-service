# Tickatch Product Service

티켓 예매 플랫폼 **Tickatch**의 상품(공연) 관리 마이크로서비스입니다.

## 프로젝트 소개

Tickatch는 콘서트, 뮤지컬, 연극, 스포츠 등 다양한 공연의 티켓 예매를 지원하는 플랫폼입니다. Product Service는 공연 상품의 생성, 수정, 상태 관리, 심사 프로세스를 담당하며, 이벤트 기반 아키텍처를 통해 다른 서비스와 통신합니다.

> 🚧 **MVP 단계** - 현재 핵심 기능 개발 중입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.x |
| Language | Java 21+ |
| Database | PostgreSQL |
| Messaging | RabbitMQ |
| Query | QueryDSL |
| Communication | OpenFeign |
| Security | Spring Security |

## 아키텍처

### 시스템 구성

```
┌─────────────────────────────────────────────────────────────┐
│                        Tickatch Platform                     │
├─────────────┬─────────────┬─────────────┬───────────────────┤
│   Product   │ Reservation │   Ticket    │  ReservationSeat  │
│   Service   │   Service   │   Service   │      Service      │
└──────┬──────┴──────┬──────┴──────┬──────┴─────────┬─────────┘
       │             │             │                │
       └─────────────┴──────┬──────┴────────────────┘
                            │
                      RabbitMQ
```

### 레이어 구조

```
product-service/
├── presentation/           # API 컨트롤러, Request/Response DTO
├── application/            # 서비스 레이어 (CQRS)
│   ├── service/
│   │   ├── ProductCommandService
│   │   └── ProductQueryService
│   ├── dto/               # Command DTO
│   └── messaging/         # 이벤트 발행
├── domain/                 # 엔티티, VO, 리포지토리 인터페이스
│   ├── Product            # Aggregate Root
│   ├── SeatGrade          # 좌석 등급 엔티티
│   ├── vo/                # Value Objects
│   └── ProductRepository
├── infrastructure/         # 리포지토리 구현, 외부 연동
│   ├── client/            # Feign Client
│   ├── messaging/         # RabbitMQ Consumer/Publisher
│   └── scheduler/         # 상태 전이 스케줄러
└── global/                 # 공통 설정, 예외 처리
```

## 도메인 모델

### Product (Aggregate Root)

상품의 전체 라이프사이클을 관리하는 핵심 엔티티입니다.

```
Product
├── 기본 정보
│   ├── sellerId          # 판매자 ID
│   ├── name              # 상품명 (최대 50자)
│   ├── productType       # 상품 타입 (CONCERT, MUSICAL, PLAY, SPORTS)
│   └── runningTime       # 상영 시간 (분)
│
├── 일정 정보
│   ├── Schedule          # 행사 일정 (startAt, endAt)
│   └── SaleSchedule      # 예매 일정 (saleStartAt, saleEndAt)
│
├── 장소 정보
│   └── Venue             # 스테이지/공연장 정보
│
├── 좌석 정보
│   ├── SeatSummary       # 좌석 현황 (총합)
│   └── List<SeatGrade>   # 등급별 좌석
│
├── 콘텐츠/정책
│   ├── ProductContent    # 상세 설명, 이미지, 출연진 등
│   ├── AgeRestriction    # 관람 제한
│   ├── BookingPolicy     # 예매 정책
│   ├── AdmissionPolicy   # 입장 정책
│   └── RefundPolicy      # 환불 정책
│
├── 통계
│   └── ProductStats      # 조회수, 예매수
│
└── 상태
    ├── status            # 상품 상태
    └── rejectionReason   # 반려 사유
```

### Value Objects

| VO | 설명 | 주요 필드 |
|----|------|----------|
| **Schedule** | 행사 일정 | startAt, endAt |
| **SaleSchedule** | 예매 일정 | saleStartAt, saleEndAt |
| **Venue** | 장소 정보 | stageId, stageName, artHallId, artHallName, artHallAddress |
| **SeatSummary** | 좌석 현황 | totalSeats, availableSeats |
| **ProductContent** | 상품 콘텐츠 | description, posterImageUrl, detailImageUrls, castInfo, notice, organizer, agency |
| **AgeRestriction** | 관람 제한 | ageRating (ALL/TWELVE/FIFTEEN/NINETEEN), restrictionNotice |
| **BookingPolicy** | 예매 정책 | maxTicketsPerPerson (1~10), idVerificationRequired, transferable |
| **AdmissionPolicy** | 입장 정책 | admissionMinutesBefore, lateEntryAllowed, hasIntermission, photographyAllowed, foodAllowed |
| **RefundPolicy** | 환불 정책 | cancellable, cancelDeadlineDays, refundPolicyText |
| **ProductStats** | 통계 | viewCount, reservationCount |

### SeatGrade (좌석 등급)

등급별 좌석 정보를 관리하는 엔티티입니다. Product에 종속됩니다.

| 필드 | 설명 |
|------|------|
| gradeName | 등급명 (VIP, R, S 등) |
| price | 가격 |
| totalSeats | 총 좌석수 |
| availableSeats | 잔여 좌석수 |
| displayOrder | 표시 순서 |

## 상품 상태 (ProductStatus)

### 상태 종류

| 상태 | 설명 | 수정 가능 |
|------|------|:--------:|
| `DRAFT` | 임시저장 - 판매자가 작성 중 | ✅ |
| `PENDING` | 심사대기 - 관리자 승인 대기 | ❌ |
| `APPROVED` | 승인됨 - 심사 통과 | ❌ |
| `REJECTED` | 반려됨 - 수정 후 재제출 가능 | ✅ |
| `SCHEDULED` | 예매예정 - 예매 시작 대기 | ❌ |
| `ON_SALE` | 판매중 - 예매 진행 중 | ❌ |
| `CLOSED` | 판매종료 - 예매 기간 종료 | ❌ |
| `COMPLETED` | 행사종료 - 공연 완료 (최종) | ❌ |
| `CANCELLED` | 취소됨 (최종) | ❌ |

### 상태 전이 다이어그램

```
                              ┌─────────────────────────────────────────────┐
                              │                 CANCELLED                    │
                              └─────────────────────────────────────────────┘
                                ↑       ↑       ↑       ↑       ↑       ↑
                                │       │       │       │       │       │
┌───────┐     ┌─────────┐     ┌─────────┐     ┌───────────┐     ┌────────┐     ┌───────────┐     ┌───────────┐
│ DRAFT │────→│ PENDING │────→│ APPROVED│────→│ SCHEDULED │────→│ ON_SALE│────→│  CLOSED   │────→│ COMPLETED │
└───────┘     └─────────┘     └─────────┘     └───────────┘     └────────┘     └───────────┘     └───────────┘
    ↑              │                               [자동]          [자동]           [자동]
    │              ↓
    │         ┌──────────┐
    └─────────│ REJECTED │
              └──────────┘
```

### 자동 상태 전이 (스케줄러)

| 전이 | 조건 | 실행 주기 |
|------|------|----------|
| SCHEDULED → ON_SALE | 판매 시작 시간 도래 | 매 분 |
| ON_SALE → CLOSED | 판매 종료 시간 도래 | 매 분 |
| CLOSED → COMPLETED | 행사 종료 시간 도래 | 매 시간 |

## 주요 기능

### 상품 관리
- 상품 생성 (DRAFT 상태로 시작)
- 상품 정보 수정 (DRAFT/REJECTED 상태에서만 가능)
- 상품 조회 (단건/목록)
- 상품 취소

### 심사 프로세스
- 심사 제출 (DRAFT → PENDING)
- 승인 (PENDING → APPROVED)
- 반려 (PENDING → REJECTED, 사유 필수)
- 재제출 (REJECTED → DRAFT)

### 좌석 등급 관리
- 등급 추가/제거 (DRAFT/REJECTED 상태에서만)
- 등급별 잔여 좌석 차감/복구
- 자동 SeatSummary 재계산

### 정책 관리
- 예매 정책: 1인당 최대 매수, 본인확인, 양도 가능 여부
- 입장 정책: 입장 시간, 지각 입장, 촬영/음식물 반입
- 환불 정책: 취소 가능 여부, 취소 마감일
- 관람 제한: 연령 등급 (전체/12/15/19세)

### 장소 변경
- 행사 시작 전까지 장소 변경 가능

## API 명세

Base URL: `/api/v1/products`

### 조회

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| GET | `/` | 상품 목록 조회 | ❌ |
| GET | `/{id}` | 상품 상세 조회 | ❌ |

### 생성/수정

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| POST | `/` | 상품 생성 | ✅ |
| PUT | `/{id}` | 상품 수정 | ✅ |

### 심사

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| POST | `/{id}/submit` | 심사 제출 (DRAFT → PENDING) | ✅ |
| POST | `/{id}/approve` | 승인 (PENDING → APPROVED) | ✅ |
| POST | `/{id}/reject` | 반려 (PENDING → REJECTED) | ✅ |
| POST | `/{id}/resubmit` | 재제출 (REJECTED → DRAFT) | ✅ |

### 상태 관리

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| POST | `/{id}/schedule` | 예매 예정 (APPROVED → SCHEDULED) | ✅ |
| POST | `/{id}/start-sale` | 판매 시작 (SCHEDULED → ON_SALE) | ✅ |
| POST | `/{id}/close-sale` | 판매 종료 (ON_SALE → CLOSED) | ✅ |
| POST | `/{id}/complete` | 행사 종료 (CLOSED → COMPLETED) | ✅ |
| DELETE | `/{id}` | 상품 취소 (→ CANCELLED) | ✅ |

### Request DTOs

#### ProductCreateRequest (상품 생성)

| 구분 | 필드 | 타입 | 필수 | 설명 |
|------|------|------|:----:|------|
| **기본 정보** | name | String | ✅ | 상품명 (최대 50자) |
| | productType | ProductType | ✅ | CONCERT, MUSICAL, PLAY, SPORTS |
| | runningTime | Integer | ✅ | 상영 시간 (분) |
| **행사 일정** | startAt | LocalDateTime | ✅ | 행사 시작 일시 |
| | endAt | LocalDateTime | ✅ | 행사 종료 일시 |
| **예매 일정** | saleStartAt | LocalDateTime | ✅ | 예매 시작 일시 |
| | saleEndAt | LocalDateTime | ✅ | 예매 종료 일시 |
| **장소** | stageId | Long | ✅ | 스테이지 ID |
| | stageName | String | ✅ | 스테이지명 |
| | artHallId | Long | ✅ | 공연장 ID |
| | artHallName | String | ✅ | 공연장명 |
| | artHallAddress | String | ✅ | 공연장 주소 |
| **콘텐츠** | description | String | | 상세 설명 (최대 5000자) |
| | posterImageUrl | String | | 포스터 이미지 URL |
| | detailImageUrls | String | | 상세 이미지 URL (JSON) |
| | castInfo | String | | 출연진 정보 |
| | notice | String | | 유의사항 |
| | organizer | String | | 주최사 |
| | agency | String | | 주관사 |
| **관람 제한** | ageRating | AgeRating | ✅ | ALL, TWELVE, FIFTEEN, NINETEEN |
| | restrictionNotice | String | | 추가 제한사항 |
| **예매 정책** | maxTicketsPerPerson | Integer | ✅ | 1인당 최대 매수 (1~10) |
| | idVerificationRequired | Boolean | | 본인확인 필요 |
| | transferable | Boolean | | 양도 가능 |
| **입장 정책** | admissionMinutesBefore | Integer | ✅ | 입장 시작 시간 (n분 전) |
| | lateEntryAllowed | Boolean | | 지각 입장 가능 |
| | lateEntryNotice | String | | 지각 입장 안내 |
| | hasIntermission | Boolean | | 인터미션 유무 |
| | intermissionMinutes | Integer | | 인터미션 시간 |
| | photographyAllowed | Boolean | | 촬영 가능 |
| | foodAllowed | Boolean | | 음식물 반입 가능 |
| **환불 정책** | cancellable | Boolean | ✅ | 취소 가능 여부 |
| | cancelDeadlineDays | Integer | | 취소 마감일 (n일 전) |
| | refundPolicyText | String | | 환불 정책 안내 |
| **좌석** | seatGradeInfos | List | ✅ | 좌석 등급 목록 |
| | seatCreateInfos | List | ✅ | 개별 좌석 목록 |

#### ProductUpdateRequest (상품 수정)

- 모든 필드 **선택적** (null이 아닌 필드만 수정)
- **세트 필드**는 모두 함께 제공 필요:
    - 일정: startAt, endAt, saleStartAt, saleEndAt (4개)
    - 장소: stageId, stageName, artHallId, artHallName, artHallAddress (5개)
    - 좌석: seatGradeInfos, seatCreateInfos (2개, 전체 교체)

#### ProductSearchRequest (목록 조회)

| 필드 | 타입 | 설명 |
|------|------|------|
| name | String | 상품명 (부분 일치) |
| productType | ProductType | 상품 타입 |
| status | ProductStatus | 상품 상태 |
| stageId | Long | 스테이지 ID |
| sellerId | String | 판매자 ID |

#### RejectRequest (반려)

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| reason | String | ✅ | 반려 사유 (최대 500자) |

## 이벤트

### 발행 이벤트 (Producer)

상품 취소 시 RabbitMQ를 통해 관련 서비스로 이벤트를 발행합니다.

| 이벤트 | Routing Key | 대상 서비스 | 설명 |
|--------|-------------|-------------|------|
| ProductCancelledToReservationEvent | `product.cancelled.reservation` | Reservation Service | 예매 취소 처리 |
| ProductCancelledToReservationSeatEvent | `product.cancelled.reservation-seat` | ReservationSeat Service | 좌석 해제 처리 |

### 수신 이벤트 (Consumer)

ReservationSeat 서비스에서 발행하는 좌석 이벤트를 수신합니다.

| 이벤트 | Queue | 처리 내용 |
|--------|-------|----------|
| SeatReservedEvent | `seat.reserved.product` | 잔여 좌석 차감, 예매수 증가, 등급별 좌석 차감 |
| SeatReleasedEvent | `seat.released.product` | 잔여 좌석 복구, 예매수 감소, 등급별 좌석 복구 |

## 외부 연동

### Feign Client

| 서비스 | 용도 |
|--------|------|
| ReservationSeatClient | 상품 생성 시 개별 좌석 정보 전달 |

## 실행 방법

### 환경 변수

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tickatch
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  rabbitmq:
    host: localhost
    port: 5672
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
```

### 실행

```bash
./gradlew bootRun
```

### 테스트

```bash
./gradlew test
```

### 코드 품질 검사

```bash
./gradlew spotlessApply spotbugsMain spotbugsTest
```

## 데이터 모델

### ERD

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              p_product                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│ id                    BIGINT PK                                              │
│ seller_id             VARCHAR(50) NOT NULL                                   │
│ name                  VARCHAR(50) NOT NULL                                   │
│ product_type          VARCHAR NOT NULL (CONCERT/MUSICAL/PLAY/SPORTS)        │
│ running_time          INTEGER NOT NULL                                       │
│ product_status        VARCHAR NOT NULL                                       │
│ rejection_reason      VARCHAR(500)                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- Schedule --                                                               │
│ start_at              TIMESTAMP NOT NULL                                     │
│ end_at                TIMESTAMP NOT NULL                                     │
│ sale_start_at         TIMESTAMP NOT NULL                                     │
│ sale_end_at           TIMESTAMP NOT NULL                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- Venue --                                                                  │
│ stage_id              BIGINT NOT NULL                                        │
│ stage_name            VARCHAR NOT NULL                                       │
│ arthall_id            BIGINT NOT NULL                                        │
│ arthall_name          VARCHAR NOT NULL                                       │
│ arthall_address       VARCHAR NOT NULL                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- SeatSummary --                                                            │
│ total_seats           INTEGER                                                │
│ available_seats       INTEGER                                                │
│ seat_updated_at       TIMESTAMP                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- ProductContent --                                                         │
│ description           VARCHAR(5000)                                          │
│ poster_image_url      VARCHAR(500)                                           │
│ detail_image_urls     JSON                                                   │
│ cast_info             VARCHAR(1000)                                          │
│ notice                VARCHAR(2000)                                          │
│ organizer             VARCHAR(100)                                           │
│ agency                VARCHAR(100)                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- AgeRestriction --                                                         │
│ age_rating            VARCHAR (ALL/TWELVE/FIFTEEN/NINETEEN)                  │
│ restriction_notice    VARCHAR(500)                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- BookingPolicy --                                                          │
│ max_tickets_per_person INTEGER (1~10, default 4)                             │
│ id_verification_required BOOLEAN (default false)                             │
│ transferable          BOOLEAN (default true)                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- AdmissionPolicy --                                                        │
│ admission_minutes_before INTEGER (default 30)                                │
│ late_entry_allowed    BOOLEAN (default false)                                │
│ late_entry_notice     VARCHAR(200)                                           │
│ has_intermission      BOOLEAN (default false)                                │
│ intermission_minutes  INTEGER                                                │
│ photography_allowed   BOOLEAN (default false)                                │
│ food_allowed          BOOLEAN (default false)                                │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- RefundPolicy --                                                           │
│ cancellable           BOOLEAN (default true)                                 │
│ cancel_deadline_days  INTEGER (default 1)                                    │
│ refund_policy_text    VARCHAR(1000)                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- ProductStats --                                                           │
│ view_count            BIGINT (default 0)                                     │
│ reservation_count     INTEGER (default 0)                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│ -- Audit --                                                                  │
│ created_at            TIMESTAMP                                              │
│ created_by            VARCHAR                                                │
│ updated_at            TIMESTAMP                                              │
│ updated_by            VARCHAR                                                │
│ deleted_at            TIMESTAMP                                              │
│ deleted_by            VARCHAR                                                │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ 1:N
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          p_product_seat_grade                                │
├─────────────────────────────────────────────────────────────────────────────┤
│ id                    BIGINT PK                                              │
│ product_id            BIGINT FK NOT NULL                                     │
│ grade_name            VARCHAR(20) NOT NULL                                   │
│ price                 BIGINT NOT NULL                                        │
│ total_seats           INTEGER NOT NULL                                       │
│ available_seats       INTEGER NOT NULL                                       │
│ display_order         INTEGER                                                │
│ created_at            TIMESTAMP                                              │
│ updated_at            TIMESTAMP                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 관련 서비스

| 서비스 | 역할 |
|--------|------|
| **Reservation Service** | 예매 관리 |
| **Ticket Service** | 티켓 발권 |
| **ReservationSeat Service** | 개별 좌석 예약 관리 |

---

© 2025 Tickatch Team