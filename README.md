# Tickatch Product Service

티켓 예매 플랫폼 **Tickatch**의 상품(공연) 관리 마이크로서비스입니다.

## 프로젝트 소개

Tickatch는 콘서트, 뮤지컬, 연극, 스포츠 등 다양한 공연의 티켓 예매를 지원하는 플랫폼입니다. Product Service는 공연 상품의 생성, 수정, 상태 관리를 담당하며, 이벤트 기반 아키텍처를 통해 다른 서비스와 통신합니다.

> 🚧 **MVP 단계** - 현재 핵심 기능 개발 중입니다.

## 기술 스택

| 분류 | 기술              |
|------|-----------------|
| Framework | Spring Boot 3.x |
| Language | Java 21+        |
| Database | PostgreSQL      |
| Messaging | RabbitMQ        |
| Query | QueryDSL        |
| Communication | OpenFeign       |
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
├── presentation/       # API 컨트롤러, DTO
├── application/        # 서비스 레이어 (CQRS)
│   ├── service/
│   │   ├── ProductCommandService
│   │   └── ProductQueryService
│   └── messaging/      # 이벤트 발행
├── domain/             # 엔티티, VO, 리포지토리 인터페이스
│   ├── Product
│   ├── vo/
│   │   ├── ProductStatus
│   │   ├── ProductType
│   │   └── Schedule
│   └── ProductRepository
├── infrastructure/     # 리포지토리 구현, 외부 연동
└── global/             # 공통 설정, 예외 처리
```

## 주요 기능

### 상품 관리
- 상품 생성 / 수정 / 조회 / 취소
- 스테이지(공연장) 변경
- 상태 전이 관리

### 상품 타입
- `CONCERT` - 콘서트
- `MUSICAL` - 뮤지컬
- `PLAY` - 연극
- `SPORTS` - 스포츠

### 상태 흐름

```
DRAFT ──→ PENDING ──→ ON_SALE ──→ SOLD_OUT
  │          │           │           │
  └──────────┴───────────┴───────────┴──→ CANCELLED
```

| 상태 | 설명 |
|------|------|
| DRAFT | 임시저장 (초기 상태) |
| PENDING | 판매대기 |
| ON_SALE | 판매중 |
| SOLD_OUT | 매진 |
| CANCELLED | 취소됨 (최종 상태) |

## API 명세

### 상품 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/products` | 상품 목록 조회 | X |
| GET | `/api/v1/products/{id}` | 상품 상세 조회 | X |
| POST | `/api/v1/products` | 상품 생성 | O |
| PUT | `/api/v1/products/{id}` | 상품 수정 | O |
| PATCH | `/api/v1/products/{id}/stage` | 스테이지 변경 | O |
| PATCH | `/api/v1/products/{id}/status` | 상태 변경 | O |
| DELETE | `/api/v1/products/{id}` | 상품 취소 | O |

## 이벤트

상품 취소 시 RabbitMQ를 통해 관련 서비스로 이벤트를 발행합니다.

| 이벤트 | Routing Key | 대상 서비스 |
|--------|-------------|-------------|
| ProductCancelledToTicketEvent | `product.cancelled.ticket` | Ticket Service |
| ProductCancelledToReservationEvent | `product.cancelled.reservation` | Reservation Service |
| ProductCancelledToReservationSeatEvent | `product.cancelled.reservation-seat` | ReservationSeat Service |

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

## 관련 서비스

- **Reservation Service** - 예매 관리
- **Ticket Service** - 티켓 발권
- **ReservationSeat Service** - 좌석 예약

---

© 2025 Tickatch Team