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
                                         id              BIGSERIAL       PRIMARY KEY,
                                         name            VARCHAR(50)     NOT NULL,
    product_type    VARCHAR(20)     NOT NULL,
    running_time    INTEGER         NOT NULL,
    start_at        TIMESTAMP       NOT NULL,
    end_at          TIMESTAMP       NOT NULL,
    stage_id        BIGINT          NOT NULL,
    product_status  VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',

    -- Audit 필드
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(100),
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(100),

    -- 제약 조건
    CONSTRAINT chk_product_type CHECK (product_type IN ('CONCERT', 'MUSICAL', 'PLAY', 'SPORTS')),
    CONSTRAINT chk_product_status CHECK (product_status IN ('DRAFT', 'PENDING', 'ON_SALE', 'SOLD_OUT', 'CANCELLED')),
    CONSTRAINT chk_schedule CHECK (end_at > start_at),
    CONSTRAINT chk_running_time CHECK (running_time > 0)
    );

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_product_status ON p_product(product_status);
CREATE INDEX IF NOT EXISTS idx_product_stage_id ON p_product(stage_id);
CREATE INDEX IF NOT EXISTS idx_product_product_type ON p_product(product_type);
CREATE INDEX IF NOT EXISTS idx_product_deleted_at ON p_product(deleted_at);
CREATE INDEX IF NOT EXISTS idx_product_start_at ON p_product(start_at);

-- 코멘트
COMMENT ON TABLE p_product IS '상품 테이블';
COMMENT ON COLUMN p_product.id IS '상품 ID';
COMMENT ON COLUMN p_product.name IS '상품명';
COMMENT ON COLUMN p_product.product_type IS '상품 타입 (CONCERT, MUSICAL, PLAY, SPORTS)';
COMMENT ON COLUMN p_product.running_time IS '상영 시간 (분)';
COMMENT ON COLUMN p_product.start_at IS '시작 일시';
COMMENT ON COLUMN p_product.end_at IS '종료 일시';
COMMENT ON COLUMN p_product.stage_id IS '스테이지 ID (FK)';
COMMENT ON COLUMN p_product.product_status IS '상품 상태 (DRAFT, PENDING, ON_SALE, SOLD_OUT, CANCELLED)';