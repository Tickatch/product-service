package com.tickatch.product_service.product.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 상태.
 *
 * <p>상품의 라이프사이클을 나타내는 상태 값이다.
 *
 * <p>상태 전이 규칙:
 *
 * <pre>
 * DRAFT ──→ PENDING ──→ ON_SALE ──→ SOLD_OUT
 *   │          │           │           │
 *   └──────────┴───────────┴───────────┴──→ CANCELLED
 * </pre>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ProductStatus {

  /** 임시저장 - 초기 상태 */
  DRAFT("임시저장"),

  /** 판매대기 - 검수 완료, 판매 시작 대기 */
  PENDING("판매대기"),

  /** 판매중 - 예매 가능 상태 */
  ON_SALE("판매중"),

  /** 매진 - 재고 소진 */
  SOLD_OUT("매진"),

  /** 취소됨 - 상품 취소 (최종 상태) */
  CANCELLED("취소됨");

  /** 상태 설명 */
  private final String description;

  /**
   * 대상 상태로 전이 가능한지 확인한다.
   *
   * @param target 대상 상태
   * @return 전이 가능하면 true
   */
  public boolean canChangeTo(ProductStatus target) {
    if (target == null || this == target) {
      return false;
    }

    return switch (this) {
      case DRAFT -> target == PENDING || target == CANCELLED;
      case PENDING -> target == DRAFT || target == ON_SALE || target == CANCELLED;
      case ON_SALE -> target == SOLD_OUT || target == CANCELLED;
      case SOLD_OUT -> target == ON_SALE || target == CANCELLED;
      case CANCELLED -> false;
    };
  }

  /**
   * DRAFT 상태 여부를 확인한다.
   *
   * @return DRAFT 상태이면 true
   */
  public boolean isDraft() {
    return this == DRAFT;
  }

  /**
   * PENDING 상태 여부를 확인한다.
   *
   * @return PENDING 상태이면 true
   */
  public boolean isPending() {
    return this == PENDING;
  }

  /**
   * ON_SALE 상태 여부를 확인한다.
   *
   * @return ON_SALE 상태이면 true
   */
  public boolean isOnSale() {
    return this == ON_SALE;
  }

  /**
   * SOLD_OUT 상태 여부를 확인한다.
   *
   * @return SOLD_OUT 상태이면 true
   */
  public boolean isSoldOut() {
    return this == SOLD_OUT;
  }

  /**
   * CANCELLED 상태 여부를 확인한다.
   *
   * @return CANCELLED 상태이면 true
   */
  public boolean isCancelled() {
    return this == CANCELLED;
  }
}
