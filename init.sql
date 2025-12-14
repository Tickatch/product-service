-- =============================================================================
-- Product Service Database Initialization Script
-- =============================================================================

-- 스키마 생성
CREATE SCHEMA IF NOT EXISTS product_service;

-- 스키마 설정
SET search_path TO product_service;

-- -----------------------------------------------------------------------------
-- Product 테이블
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS p_product (
                                         id                      BIGSERIAL       PRIMARY KEY,

    -- 기본 정보
                                         seller_id               VARCHAR(50)     NOT NULL,
    name                    VARCHAR(50)     NOT NULL,
    product_type            VARCHAR(20)     NOT NULL,
    running_time            INTEGER         NOT NULL,
    product_status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    rejection_reason        VARCHAR(500),

    -- Schedule (행사 일정)
    start_at                TIMESTAMP       NOT NULL,
    end_at                  TIMESTAMP       NOT NULL,

    -- SaleSchedule (예매 일정)
    sale_start_at           TIMESTAMP,
    sale_end_at             TIMESTAMP,

    -- Venue (장소 정보)
    stage_id                BIGINT          NOT NULL,
    stage_name              VARCHAR(255)    NOT NULL,
    arthall_id             BIGINT          NOT NULL,
    arthall_name           VARCHAR(255)    NOT NULL,
    arthall_address        VARCHAR(500)    NOT NULL,

    -- SeatSummary (좌석 현황)
    total_seats             INTEGER         DEFAULT 0,
    available_seats         INTEGER         DEFAULT 0,
    seat_updated_at         TIMESTAMP,

    -- ProductStats (통계)
    view_count              BIGINT          DEFAULT 0,
    reservation_count       INTEGER         DEFAULT 0,

    -- ProductContent (콘텐츠)
    description             VARCHAR(5000),
    poster_image_url        VARCHAR(500),
    detail_image_urls       JSON,
    cast_info               VARCHAR(1000),
    notice                  VARCHAR(2000),
    organizer               VARCHAR(100),
    agency                  VARCHAR(100),

    -- AgeRestriction (관람 제한)
    age_rating              VARCHAR(20)     DEFAULT 'ALL',
    restriction_notice      VARCHAR(500),

    -- BookingPolicy (예매 정책)
    max_tickets_per_person  INTEGER         DEFAULT 1,
    id_verification_required BOOLEAN        DEFAULT FALSE,
    transferable            BOOLEAN         DEFAULT FALSE,

    -- AdmissionPolicy (입장 정책)
    admission_minutes_before INTEGER        DEFAULT 30,
    late_entry_allowed      BOOLEAN         DEFAULT FALSE,
    late_entry_notice       VARCHAR(200),
    has_intermission        BOOLEAN         DEFAULT FALSE,
    intermission_minutes    INTEGER,
    photography_allowed     BOOLEAN         DEFAULT FALSE,
    food_allowed            BOOLEAN         DEFAULT FALSE,

    -- RefundPolicy (환불 정책)
    cancellable             BOOLEAN         DEFAULT TRUE,
    cancel_deadline_days    INTEGER         DEFAULT 1,
    refund_policy_text      VARCHAR(1000),

    -- Audit 필드
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(100),
    updated_at              TIMESTAMP,
    updated_by              VARCHAR(100),
    deleted_at              TIMESTAMP,
    deleted_by              VARCHAR(100),

    -- 제약 조건
    CONSTRAINT chk_product_type CHECK (product_type IN ('CONCERT', 'MUSICAL', 'PLAY', 'SPORTS')),
    CONSTRAINT chk_product_status CHECK (product_status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'SCHEDULED', 'ON_SALE', 'CLOSED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_age_rating CHECK (age_rating IN ('ALL', 'TWELVE', 'FIFTEEN', 'NINETEEN')),
    CONSTRAINT chk_schedule CHECK (end_at > start_at),
    CONSTRAINT chk_running_time CHECK (running_time > 0)
    );

-- Product 인덱스
CREATE INDEX IF NOT EXISTS idx_product_seller_id ON p_product(seller_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON p_product(product_status);
CREATE INDEX IF NOT EXISTS idx_product_stage_id ON p_product(stage_id);
CREATE INDEX IF NOT EXISTS idx_product_product_type ON p_product(product_type);
CREATE INDEX IF NOT EXISTS idx_product_deleted_at ON p_product(deleted_at);
CREATE INDEX IF NOT EXISTS idx_product_start_at ON p_product(start_at);
CREATE INDEX IF NOT EXISTS idx_product_sale_start_at ON p_product(sale_start_at);

-- -----------------------------------------------------------------------------
-- SeatGrade 테이블 (좌석 등급)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS p_product_seat_grade (
                                                    id                      BIGSERIAL       PRIMARY KEY,

    -- 상품 FK
                                                    product_id              BIGINT          NOT NULL,

    -- 등급 정보
                                                    grade_name              VARCHAR(20)     NOT NULL,
    price                   BIGINT          NOT NULL,
    total_seats             INTEGER         NOT NULL,
    available_seats         INTEGER         NOT NULL,
    display_order           INTEGER,

    -- Time 필드 (AbstractTimeEntity)
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP,

    -- 제약 조건
    CONSTRAINT fk_seat_grade_product FOREIGN KEY (product_id) REFERENCES p_product(id) ON DELETE CASCADE,
    CONSTRAINT chk_seat_grade_price CHECK (price >= 0),
    CONSTRAINT chk_seat_grade_total_seats CHECK (total_seats > 0),
    CONSTRAINT chk_seat_grade_available_seats CHECK (available_seats >= 0 AND available_seats <= total_seats)
    );

-- SeatGrade 인덱스
CREATE INDEX IF NOT EXISTS idx_seat_grade_product_id ON p_product_seat_grade(product_id);
CREATE INDEX IF NOT EXISTS idx_seat_grade_grade_name ON p_product_seat_grade(grade_name);

-- -----------------------------------------------------------------------------
-- 코멘트
-- -----------------------------------------------------------------------------
-- Product
COMMENT ON TABLE p_product IS '상품 테이블';
COMMENT ON COLUMN p_product.id IS '상품 ID';
COMMENT ON COLUMN p_product.seller_id IS '판매자 ID';
COMMENT ON COLUMN p_product.name IS '상품명';
COMMENT ON COLUMN p_product.product_type IS '상품 타입 (CONCERT, MUSICAL, PLAY, SPORTS)';
COMMENT ON COLUMN p_product.running_time IS '상영 시간 (분)';
COMMENT ON COLUMN p_product.product_status IS '상품 상태';
COMMENT ON COLUMN p_product.rejection_reason IS '반려 사유';
COMMENT ON COLUMN p_product.start_at IS '행사 시작 일시';
COMMENT ON COLUMN p_product.end_at IS '행사 종료 일시';
COMMENT ON COLUMN p_product.sale_start_at IS '예매 시작 일시';
COMMENT ON COLUMN p_product.sale_end_at IS '예매 종료 일시';
COMMENT ON COLUMN p_product.admission_minutes_before IS '입장 시작 시간 (공연 n분 전)';

-- SeatGrade
COMMENT ON TABLE p_product_seat_grade IS '좌석 등급 테이블';
COMMENT ON COLUMN p_product_seat_grade.id IS '좌석 등급 ID';
COMMENT ON COLUMN p_product_seat_grade.product_id IS '상품 ID (FK)';
COMMENT ON COLUMN p_product_seat_grade.grade_name IS '등급명 (VIP, R, S 등)';
COMMENT ON COLUMN p_product_seat_grade.price IS '가격';
COMMENT ON COLUMN p_product_seat_grade.total_seats IS '총 좌석수';
COMMENT ON COLUMN p_product_seat_grade.available_seats IS '잔여 좌석수';
COMMENT ON COLUMN p_product_seat_grade.display_order IS '표시 순서';