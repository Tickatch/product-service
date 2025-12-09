# Product 도메인 2차 확장 설계서

## 1. 개요

### 1.1 문서 목적
본 문서는 Tickatch 프로젝트의 Product 도메인 2차 확장에 대한 설계를 정의한다.

### 1.2 확장 배경
- 현재 Product 도메인은 기본적인 상품 정보(이름, 타입, 일정, 장소)만 관리
- 실제 티켓 예매 서비스 운영에 필요한 상세 정보 부족
- ReservationSeat 서비스와의 좌석 연동 필요
- 프론트엔드 상품 상세 페이지 구현을 위한 데이터 확장 필요

### 1.3 확장 목표
1. 행사 상세 정보 (설명, 포스터, 출연진, 관람등급 등) 관리
2. 예매/입장/환불 정책 관리
3. 등급별 좌석 정보 관리 및 ReservationSeat 서비스 연동
4. 조회 최적화를 위한 데이터 비정규화

---

## 2. 현재 구조 분석

### 2.1 현재 Product 엔티티 구조
```
Product
├── 기본 정보
│   ├── id: Long (PK)
│   ├── sellerId: String (판매자 ID)
│   ├── name: String (상품명, 최대 50자)
│   ├── productType: ProductType (CONCERT, MUSICAL, PLAY, SPORTS)
│   └── runningTime: Integer (상영시간, 분)
│
├── 일정 정보
│   ├── schedule: Schedule (행사 시작/종료 일시)
│   └── saleSchedule: SaleSchedule (예매 시작/종료 일시)
│
├── 장소 정보
│   └── venue: Venue (스테이지ID, 공연장ID, 주소 등)
│
├── 좌석 정보
│   └── seatSummary: SeatSummary (총좌석, 잔여좌석 - 총합만)
│
├── 통계 정보
│   └── stats: ProductStats (조회수, 예매수)
│
└── 상태 정보
    ├── status: ProductStatus (DRAFT → ... → COMPLETED/CANCELLED)
    └── rejectionReason: String (반려 사유)
```

### 2.2 현재 구조의 한계
| 영역 | 한계점 |
|------|--------|
| 상품 상세 | 설명, 포스터, 출연진 정보 없음 |
| 관람 제한 | 나이 제한(관람등급) 정보 없음 |
| 예매 정책 | 1인당 최대 예매 수량 등 정책 없음 |
| 좌석 정보 | 등급별 가격/잔여석 정보 없음 (총합만 존재) |
| 서비스 연동 | ReservationSeat 서비스와 좌석 생성 연동 없음 |

---

## 3. 확장 설계

### 3.1 확장 후 Product 구조
```
Product (확장 후)
├── 기본 정보
│   ├── id, sellerId, name
│   ├── productType, runningTime
│   └── status, rejectionReason
│
├── 일정 정보
│   ├── schedule (행사 일정)
│   └── saleSchedule (예매 일정)
│
├── 장소 정보
│   └── venue (공연장 정보)
│
├── 행사 상세 정보 ⭐ NEW
│   ├── content: ProductContent
│   │   ├── description (상세 설명)
│   │   ├── posterImageUrl (포스터 이미지)
│   │   ├── detailImageUrls (상세 이미지들)
│   │   ├── castInfo (출연진 정보)
│   │   ├── notice (유의사항)
│   │   ├── organizer (주최)
│   │   └── agency (주관)
│   │
│   └── ageRestriction: AgeRestriction
│       ├── ageRating (관람등급: ALL, 12, 15, 19)
│       └── restrictionNotice (추가 제한사항)
│
├── 정책 정보 ⭐ NEW
│   ├── bookingPolicy: BookingPolicy
│   │   ├── maxTicketsPerPerson (1인당 최대 매수)
│   │   ├── idVerificationRequired (본인확인 필요 여부)
│   │   └── transferable (양도 가능 여부)
│   │
│   ├── admissionPolicy: AdmissionPolicy
│   │   ├── admissionMinutesBefore (입장 시작 시간)
│   │   ├── lateEntryAllowed (지각 입장 가능)
│   │   ├── lateEntryNotice (지각 입장 안내)
│   │   ├── hasIntermission (인터미션 유무)
│   │   ├── intermissionMinutes (인터미션 시간)
│   │   ├── photographyAllowed (촬영 가능)
│   │   └── foodAllowed (음식물 반입 가능)
│   │
│   └── refundPolicy: RefundPolicy
│       ├── cancellable (취소 가능 여부)
│       ├── cancelDeadlineDays (취소 마감일)
│       └── refundPolicyText (환불 정책 안내)
│
├── 좌석 정보
│   ├── seatSummary: SeatSummary (총합 - 기존)
│   └── seatGrades: List<SeatGrade> ⭐ NEW
│       ├── gradeName (등급명: VIP, R, S...)
│       ├── price (가격)
│       ├── totalSeats (총 좌석수)
│       ├── availableSeats (잔여 좌석수)
│       └── displayOrder (정렬 순서)
│
├── 부가 정보 ⭐ NEW (선택적)
│   ├── metadata: ProductMetadata
│   │   ├── tags (검색 태그)
│   │   ├── subCategory (서브 카테고리)
│   │   ├── featured (추천 여부)
│   │   └── priority (우선순위)
│   │
│   └── contactInfo: ContactInfo
│       ├── inquiryPhone (문의 전화)
│       ├── inquiryEmail (문의 이메일)
│       └── homepageUrl (홈페이지)
│
└── 통계 정보
    └── stats: ProductStats (기존)
```

---

## 4. 신규 Value Object 상세

### 4.1 ProductContent (행사 상세 정보)

#### 목적
상품 상세 페이지에 표시될 콘텐츠 정보를 관리한다.

#### 필드 정의
| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| description | String | 최대 5000자 | 상세 설명 |
| posterImageUrl | String | 최대 500자 | 메인 포스터 이미지 URL |
| detailImageUrls | String (JSON) | - | 상세 이미지 URL 배열 |
| castInfo | String | 최대 1000자 | 출연진/아티스트 정보 |
| notice | String | 최대 2000자 | 유의사항 |
| organizer | String | 최대 100자 | 주최사 |
| agency | String | 최대 100자 | 주관사/기획사 |

#### 생성 규칙
- 초기 생성 시 모든 필드 nullable (DRAFT 상태에서 점진적 입력)
- 심사 제출(PENDING) 시 description, posterImageUrl 필수 검증

---

### 4.2 AgeRestriction (관람 제한)

#### 목적
관람등급 및 입장 제한 정보를 관리한다.

#### 필드 정의
| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| ageRating | AgeRating (Enum) | 기본값: ALL | 관람등급 |
| restrictionNotice | String | 최대 500자 | 추가 제한사항 안내 |

#### AgeRating Enum
| 값 | 설명 | 최소 나이 |
|----|------|----------|
| ALL | 전체 관람가 | 0 |
| TWELVE | 12세 이상 | 12 |
| FIFTEEN | 15세 이상 | 15 |
| NINETEEN | 19세 이상 | 19 |

#### 비즈니스 규칙
- 19세 관람가의 경우 본인확인 필수 (BookingPolicy.idVerificationRequired = true 권장)

---

### 4.3 BookingPolicy (예매 정책)

#### 목적
예매 시 적용되는 정책을 관리한다.

#### 필드 정의
| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| maxTicketsPerPerson | Integer | 1~10, 기본값: 4 | 1인당 최대 예매 매수 |
| idVerificationRequired | Boolean | 기본값: false | 본인확인 필요 여부 |
| transferable | Boolean | 기본값: true | 양도 가능 여부 |

#### 비즈니스 규칙
- Reservation 서비스에서 예매 시 maxTicketsPerPerson 검증에 활용
- idVerificationRequired=true 시 입장 시 신분증 확인 안내

---

### 4.4 AdmissionPolicy (입장 정책)

#### 목적
공연장 입장 및 관람 관련 정책을 관리한다.

#### 필드 정의
| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| admissionMinutesBefore | Integer | 기본값: 30 | 입장 시작 (공연 n분 전) |
| lateEntryAllowed | Boolean | 기본값: false | 지각 입장 가능 여부 |
| lateEntryNotice | String | 최대 200자 | 지각 입장 안내 |
| hasIntermission | Boolean | 기본값: false | 인터미션 유무 |
| intermissionMinutes | Integer | nullable | 인터미션 시간 (분) |
| photographyAllowed | Boolean | 기본값: false | 촬영 가능 여부 |
| foodAllowed | Boolean | 기본값: false | 음식물 반입 가능 |

---

### 4.5 RefundPolicy (환불 정책)

#### 목적
취소/환불 관련 정책을 관리한다.

#### 필드 정의
| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| cancellable | Boolean | 기본값: true | 취소 가능 여부 |
| cancelDeadlineDays | Integer | 기본값: 1 | 취소 마감 (공연 n일 전) |
| refundPolicyText | String | 최대 1000자 | 환불 정책 상세 안내 |

#### 비즈니스 규칙
- Reservation 서비스에서 취소 요청 시 cancelDeadlineDays 기반 취소 가능 여부 판단
- 공연 당일(cancelDeadlineDays=0)까지 취소 가능한 경우도 허용

---

### 4.6 SeatGrade (등급별 좌석 정보) - Entity

#### 목적
등급별 좌석 가격 및 잔여석 정보를 관리한다. 상품 조회 시 ReservationSeat 서비스 호출 없이 등급별 정보를 제공하기 위한 비정규화 데이터.

#### 필드 정의
| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK | 식별자 |
| productId | Long | FK, NOT NULL | 상품 ID |
| gradeName | String | NOT NULL, 최대 20자 | 등급명 (VIP, R, S 등) |
| price | Long | NOT NULL, >= 0 | 가격 |
| totalSeats | Integer | NOT NULL, >= 0 | 총 좌석수 |
| availableSeats | Integer | NOT NULL, >= 0 | 잔여 좌석수 |
| displayOrder | Integer | nullable | 표시 순서 |

#### 데이터 흐름
```
[상품 생성 시]
1. 프론트 → Product Service: 등급별 좌석 정보 + 좌석번호 리스트
2. Product Service: SeatGrade 리스트 생성 (등급별 요약)
3. Product Service → ReservationSeat Service (Feign): 개별 좌석 생성

[예매 시]
1. ReservationSeat 상태 변경 (AVAILABLE → RESERVED)
2. ReservationSeat → Product (Event): 좌석 예약 이벤트
3. Product: SeatGrade.availableSeats 감소, SeatSummary 갱신

[취소 시]
1. ReservationSeat 상태 변경 (RESERVED → AVAILABLE)
2. ReservationSeat → Product (Event): 좌석 취소 이벤트
3. Product: SeatGrade.availableSeats 증가, SeatSummary 갱신
```

---

---

## 5. 우선순위 및 구현 계획

### 5.1 구현 우선순위

| 우선순위 | VO/Entity | 필요성 | 비고 |
|----------|-----------|--------|------|
| **1순위** | ProductContent | 상품 상세 페이지 필수 | 즉시 구현 |
| **1순위** | AgeRestriction | 법적 필수 (관람등급) | 즉시 구현 |
| **1순위** | SeatGrade | ReservationSeat 연동 핵심 | 즉시 구현 |
| **2순위** | BookingPolicy | 예매 제한 로직 필요 | 예매 기능 구현 시 |
| **2순위** | RefundPolicy | 취소/환불 로직 필요 | 취소 기능 구현 시 |
| **3순위** | AdmissionPolicy | 상세 정보 제공 | 상세 페이지 고도화 시 |
| **4순위** | ProductMetadata | 검색/추천 고도화 | 검색 기능 구현 시 |
| **4순위** | ContactInfo | 부가 정보 | 필요 시 |

### 5.2 1차 구현 범위 (Phase 1)
- ProductContent
- AgeRestriction
- SeatGrade
- ReservationSeat Feign 연동

### 5.3 2차 구현 범위 (Phase 2)
- BookingPolicy
- RefundPolicy
- AdmissionPolicy

### 5.4 3차 구현 범위 (Phase 3)
- ProductMetadata
- ContactInfo

---

## 6. 데이터베이스 스키마 변경

### 6.1 p_product 테이블 컬럼 추가

```sql
-- ProductContent
ALTER TABLE p_product ADD COLUMN description TEXT;
ALTER TABLE p_product ADD COLUMN poster_image_url VARCHAR(500);
ALTER TABLE p_product ADD COLUMN detail_image_urls JSON;
ALTER TABLE p_product ADD COLUMN cast_info VARCHAR(1000);
ALTER TABLE p_product ADD COLUMN notice VARCHAR(2000);
ALTER TABLE p_product ADD COLUMN organizer VARCHAR(100);
ALTER TABLE p_product ADD COLUMN agency VARCHAR(100);

-- AgeRestriction
ALTER TABLE p_product ADD COLUMN age_rating VARCHAR(20) DEFAULT 'ALL';
ALTER TABLE p_product ADD COLUMN restriction_notice VARCHAR(500);

-- BookingPolicy
ALTER TABLE p_product ADD COLUMN max_tickets_per_person INT DEFAULT 4;
ALTER TABLE p_product ADD COLUMN id_verification_required BOOLEAN DEFAULT FALSE;
ALTER TABLE p_product ADD COLUMN transferable BOOLEAN DEFAULT TRUE;

-- AdmissionPolicy
ALTER TABLE p_product ADD COLUMN admission_minutes_before INT DEFAULT 30;
ALTER TABLE p_product ADD COLUMN late_entry_allowed BOOLEAN DEFAULT FALSE;
ALTER TABLE p_product ADD COLUMN late_entry_notice VARCHAR(200);
ALTER TABLE p_product ADD COLUMN has_intermission BOOLEAN DEFAULT FALSE;
ALTER TABLE p_product ADD COLUMN intermission_minutes INT;
ALTER TABLE p_product ADD COLUMN photography_allowed BOOLEAN DEFAULT FALSE;
ALTER TABLE p_product ADD COLUMN food_allowed BOOLEAN DEFAULT FALSE;

-- RefundPolicy
ALTER TABLE p_product ADD COLUMN cancellable BOOLEAN DEFAULT TRUE;
ALTER TABLE p_product ADD COLUMN cancel_deadline_days INT DEFAULT 1;
ALTER TABLE p_product ADD COLUMN refund_policy_text VARCHAR(1000);

-- ProductMetadata
ALTER TABLE p_product ADD COLUMN tags VARCHAR(500);
ALTER TABLE p_product ADD COLUMN sub_category VARCHAR(50);
ALTER TABLE p_product ADD COLUMN featured BOOLEAN DEFAULT FALSE;
ALTER TABLE p_product ADD COLUMN priority INT DEFAULT 0;

-- ContactInfo
ALTER TABLE p_product ADD COLUMN inquiry_phone VARCHAR(20);
ALTER TABLE p_product ADD COLUMN inquiry_email VARCHAR(100);
ALTER TABLE p_product ADD COLUMN homepage_url VARCHAR(300);
```

### 6.2 p_product_seat_grade 테이블 신규 생성

```sql
CREATE TABLE p_product_seat_grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    grade_name VARCHAR(20) NOT NULL,
    price BIGINT NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    display_order INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_seat_grade_product 
        FOREIGN KEY (product_id) REFERENCES p_product(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 7. 서비스 간 연동

### 7.1 상품 생성 시 좌석 생성 흐름

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────────────┐
│  Frontend   │      │  Product Service │      │ ReservationSeat Svc │
└──────┬──────┘      └────────┬─────────┘      └──────────┬──────────┘
       │                      │                           │
       │  1. 상품 생성 요청    │                           │
       │  (등급별 좌석 정보    │                           │
       │   + 좌석번호 리스트)  │                           │
       │─────────────────────>│                           │
       │                      │                           │
       │                      │  2. Product 생성          │
       │                      │     SeatGrade 리스트 생성  │
       │                      │     SeatSummary 계산      │
       │                      │                           │
       │                      │  3. Feign: 좌석 생성 요청  │
       │                      │─────────────────────────>│
       │                      │                           │
       │                      │                           │ 4. 개별 좌석
       │                      │                           │    생성
       │                      │                           │
       │                      │  5. 생성 완료 응답        │
       │                      │<─────────────────────────│
       │                      │                           │
       │  6. 상품 생성 완료    │                           │
       │<─────────────────────│                           │
       │                      │                           │
```

### 7.2 좌석 예약 시 동기화 흐름

```
┌──────────────────┐      ┌─────────────────────┐      ┌─────────────┐
│  Product Service │      │ ReservationSeat Svc │      │  Message Q  │
└────────┬─────────┘      └──────────┬──────────┘      └──────┬──────┘
         │                           │                        │
         │                           │  1. 좌석 예약 처리     │
         │                           │     (상태: RESERVED)   │
         │                           │                        │
         │                           │  2. 이벤트 발행        │
         │                           │─────────────────────>│
         │                           │   SeatReservedEvent   │
         │                           │   {productId, grade,  │
         │                           │    count}             │
         │                           │                        │
         │  3. 이벤트 수신           │                        │
         │<───────────────────────────────────────────────────│
         │                           │                        │
         │  4. SeatGrade.            │                        │
         │     availableSeats 감소   │                        │
         │     SeatSummary 갱신      │                        │
         │                           │                        │
```

### 7.3 상품 취소 시 이벤트 발행

```
┌──────────────────┐      ┌──────────┐      ┌─────────────────────┐
│  Product Service │      │ Message Q │      │ ReservationSeat Svc │
└────────┬─────────┘      └─────┬────┘      └──────────┬──────────┘
         │                      │                      │
         │  1. 상품 취소        │                      │
         │     (CANCELLED)      │                      │
         │                      │                      │
         │  2. 이벤트 발행      │                      │
         │─────────────────────>│                      │
         │  ProductCancelled    │                      │
         │  ToReservationSeat   │                      │
         │  Event               │                      │
         │                      │                      │
         │                      │  3. 이벤트 수신      │
         │                      │─────────────────────>│
         │                      │                      │
         │                      │                      │ 4. 해당 상품
         │                      │                      │    전체 좌석
         │                      │                      │    CANCELLED
         │                      │                      │
```

---

## 8. API 요청/응답 예시

### 8.1 상품 생성 요청

```json
{
  "name": "2025 IU 콘서트 - The Golden Hour",
  "productType": "CONCERT",
  "runningTime": 150,
  "schedule": {
    "startAt": "2025-03-15T19:00:00",
    "endAt": "2025-03-15T21:30:00"
  },
  "saleSchedule": {
    "saleStartAt": "2025-02-01T10:00:00",
    "saleEndAt": "2025-03-14T18:00:00"
  },
  "venue": {
    "stageId": 1,
    "stageName": "올림픽홀",
    "artHallId": 1,
    "artHallName": "올림픽공원",
    "artHallAddress": "서울특별시 송파구 올림픽로 424"
  },
  "content": {
    "description": "IU의 2025년 전국 투어 콘서트 'The Golden Hour' 서울 앙코르 공연입니다...",
    "posterImageUrl": "https://cdn.tickatch.com/posters/iu-2025.jpg",
    "detailImageUrls": [
      "https://cdn.tickatch.com/details/iu-2025-1.jpg",
      "https://cdn.tickatch.com/details/iu-2025-2.jpg"
    ],
    "castInfo": "IU (아이유), 밴드 세션",
    "notice": "※ 공연 중 사진/영상 촬영 금지\n※ 우천 시에도 정상 진행",
    "organizer": "EDAM엔터테인먼트",
    "agency": "현대카드"
  },
  "ageRestriction": {
    "ageRating": "ALL",
    "restrictionNotice": null
  },
  "bookingPolicy": {
    "maxTicketsPerPerson": 4,
    "idVerificationRequired": false,
    "transferable": true
  },
  "admissionPolicy": {
    "admissionMinutesBefore": 60,
    "lateEntryAllowed": true,
    "lateEntryNotice": "1부 종료 후 인터미션 시간에 입장 가능",
    "hasIntermission": true,
    "intermissionMinutes": 20,
    "photographyAllowed": false,
    "foodAllowed": false
  },
  "refundPolicy": {
    "cancellable": true,
    "cancelDeadlineDays": 3,
    "refundPolicyText": "관람일 3일 전까지: 전액 환불\n관람일 2일 전: 90% 환불\n관람일 1일 전~당일: 환불 불가"
  },
  "seatGrades": [
    {
      "gradeName": "VIP",
      "price": 165000,
      "seatNumbers": ["A1", "A2", "A3", "A4", "A5", ... , "A50"]
    },
    {
      "gradeName": "R",
      "price": 143000,
      "seatNumbers": ["B1", "B2", ... , "B100"]
    },
    {
      "gradeName": "S",
      "price": 121000,
      "seatNumbers": ["C1", "C2", ... , "C150"]
    },
    {
      "gradeName": "A",
      "price": 99000,
      "seatNumbers": ["D1", "D2", ... , "D200"]
    }
  ]
}
```

### 8.2 상품 상세 조회 응답

```json
{
  "id": 1,
  "name": "2025 IU 콘서트 - The Golden Hour",
  "productType": "CONCERT",
  "productTypeDescription": "콘서트",
  "runningTime": 150,
  "status": "ON_SALE",
  "statusDescription": "판매중",
  
  "schedule": {
    "startAt": "2025-03-15T19:00:00",
    "endAt": "2025-03-15T21:30:00"
  },
  "saleSchedule": {
    "saleStartAt": "2025-02-01T10:00:00",
    "saleEndAt": "2025-03-14T18:00:00",
    "isInSalePeriod": true
  },
  
  "venue": {
    "stageId": 1,
    "stageName": "올림픽홀",
    "artHallId": 1,
    "artHallName": "올림픽공원",
    "artHallAddress": "서울특별시 송파구 올림픽로 424"
  },
  
  "content": {
    "description": "IU의 2025년 전국 투어 콘서트...",
    "posterImageUrl": "https://cdn.tickatch.com/posters/iu-2025.jpg",
    "detailImageUrls": ["..."],
    "castInfo": "IU (아이유), 밴드 세션",
    "notice": "※ 공연 중 사진/영상 촬영 금지...",
    "organizer": "EDAM엔터테인먼트",
    "agency": "현대카드"
  },
  
  "ageRestriction": {
    "ageRating": "ALL",
    "ageRatingDescription": "전체 관람가",
    "minimumAge": 0,
    "restrictionNotice": null
  },
  
  "bookingPolicy": {
    "maxTicketsPerPerson": 4,
    "idVerificationRequired": false,
    "transferable": true
  },
  
  "admissionPolicy": {
    "admissionMinutesBefore": 60,
    "lateEntryAllowed": true,
    "lateEntryNotice": "1부 종료 후 인터미션 시간에 입장 가능",
    "hasIntermission": true,
    "intermissionMinutes": 20,
    "photographyAllowed": false,
    "foodAllowed": false
  },
  
  "refundPolicy": {
    "cancellable": true,
    "cancelDeadlineDays": 3,
    "refundPolicyText": "관람일 3일 전까지: 전액 환불..."
  },
  
  "seatSummary": {
    "totalSeats": 500,
    "availableSeats": 127,
    "soldSeats": 373,
    "soldRate": 74.6,
    "isSoldOut": false
  },
  
  "seatGrades": [
    {
      "gradeName": "VIP",
      "price": 165000,
      "totalSeats": 50,
      "availableSeats": 0,
      "isSoldOut": true
    },
    {
      "gradeName": "R",
      "price": 143000,
      "totalSeats": 100,
      "availableSeats": 23,
      "isSoldOut": false
    },
    {
      "gradeName": "S",
      "price": 121000,
      "totalSeats": 150,
      "availableSeats": 45,
      "isSoldOut": false
    },
    {
      "gradeName": "A",
      "price": 99000,
      "totalSeats": 200,
      "availableSeats": 59,
      "isSoldOut": false
    }
  ],
  
  "stats": {
    "viewCount": 15234,
    "reservationCount": 373
  },
  
  "canPurchase": true,
  "createdAt": "2025-01-15T09:00:00",
  "updatedAt": "2025-02-20T14:30:00"
}
```

---

## 9. 고려사항 및 결정 사항

### 9.1 설계 결정 사항

| 항목 | 결정 | 이유 |
|------|------|------|
| 좌석 생성 시점 | 상품 등록 시 즉시 생성 | 심사 시 좌석 정보 검증 가능, 판매 시작 시 부하 분산 |
| 등급별 정보 저장 | Product에 비정규화 | 상품 조회 시 Feign 호출 없이 등급/가격/잔여석 제공 |
| 개별 좌석 관리 | ReservationSeat에 위임 | Source of Truth는 ReservationSeat |
| 동기화 방식 | 생성은 Feign, 상태 변경은 Event | 생성은 동기 처리 필요, 상태 변경은 Eventually Consistent |

### 9.2 추후 고려 사항
- 이미지 업로드 처리 (별도 Storage 서비스 연동)
- 등급별 좌석 정보 캐싱 전략 (Redis)
- 좌석 동기화 실패 시 재처리 로직 (Outbox 패턴)
- 대량 좌석 생성 시 성능 최적화 (Batch Insert)

---

## 10. 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|----------|
| 1.0 | 2025-xx-xx | - | 최초 작성 |