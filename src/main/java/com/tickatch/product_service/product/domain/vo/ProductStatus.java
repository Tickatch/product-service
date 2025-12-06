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
 * DRAFT ──→ PENDING ──→ APPROVED ──→ SCHEDULED ──→ ON_SALE ──→ CLOSED ──→ COMPLETED
 *               │                                      │
 *               ↓                                      │
 *            REJECTED                                  │
 *               │                                      │
 *   └───────────┴──────────────────────────────────────┴──→ CANCELLED
 * </pre>
 *
 * <p>매진 여부는 상태가 아닌 {@code SeatSummary.isSoldOut()}으로 판단한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ProductStatus {

  /** 임시저장 - 판매자가 작성 중인 상태 */
  DRAFT("임시저장"),

  /** 심사대기 - 관리자 승인 대기 중 */
  PENDING("심사대기"),

  /** 승인됨 - 심사 통과, 예매 시작 전 */
  APPROVED("승인됨"),

  /** 반려됨 - 심사 불합격, 수정 후 재제출 가능 */
  REJECTED("반려됨"),

  /** 예매예정 - 승인 완료, 예매 시작 시간 대기 */
  SCHEDULED("예매예정"),

  /** 판매중 - 예매 진행 중 */
  ON_SALE("판매중"),

  /** 판매종료 - 예매 기간 종료 */
  CLOSED("판매종료"),

  /** 행사종료 - 공연/전시 완료 */
  COMPLETED("행사종료"),

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
      case PENDING -> target == APPROVED || target == REJECTED || target == CANCELLED;
      case APPROVED -> target == SCHEDULED || target == CANCELLED;
      case REJECTED -> target == DRAFT || target == CANCELLED;
      case SCHEDULED -> target == ON_SALE || target == CANCELLED;
      case ON_SALE -> target == CLOSED || target == CANCELLED;
      case CLOSED -> target == COMPLETED || target == CANCELLED;
      case COMPLETED, CANCELLED -> false;
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
   * APPROVED 상태 여부를 확인한다.
   *
   * @return APPROVED 상태이면 true
   */
  public boolean isApproved() {
    return this == APPROVED;
  }

  /**
   * REJECTED 상태 여부를 확인한다.
   *
   * @return REJECTED 상태이면 true
   */
  public boolean isRejected() {
    return this == REJECTED;
  }

  /**
   * SCHEDULED 상태 여부를 확인한다.
   *
   * @return SCHEDULED 상태이면 true
   */
  public boolean isScheduled() {
    return this == SCHEDULED;
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
   * CLOSED 상태 여부를 확인한다.
   *
   * @return CLOSED 상태이면 true
   */
  public boolean isClosed() {
    return this == CLOSED;
  }

  /**
   * COMPLETED 상태 여부를 확인한다.
   *
   * @return COMPLETED 상태이면 true
   */
  public boolean isCompleted() {
    return this == COMPLETED;
  }

  /**
   * CANCELLED 상태 여부를 확인한다.
   *
   * @return CANCELLED 상태이면 true
   */
  public boolean isCancelled() {
    return this == CANCELLED;
  }

  /**
   * 수정 가능한 상태인지 확인한다.
   *
   * @return DRAFT 또는 REJECTED 상태이면 true
   */
  public boolean isEditable() {
    return this == DRAFT || this == REJECTED;
  }

  /**
   * 최종 상태인지 확인한다.
   *
   * @return COMPLETED 또는 CANCELLED 상태이면 true
   */
  public boolean isTerminal() {
    return this == COMPLETED || this == CANCELLED;
  }

  /**
   * 구매 가능한 상태인지 확인한다.
   *
   * <p>심사 승인 이후 상태(APPROVED, SCHEDULED, ON_SALE, CLOSED)이면 구매 가능하다.
   * 실제 구매 가능 여부는 {@code Product.canPurchase()}에서 판매 기간과 잔여 좌석을 함께 확인한다.
   *
   * @return 구매 가능한 상태이면 true
   */
  public boolean canBePurchased() {
    return this == APPROVED || this == SCHEDULED || this == ON_SALE || this == CLOSED;
  }
}
