package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 환불 정책 Value Object.
 *
 * <p>취소/환불 관련 정책을 관리한다. 불변 객체로 설계되어 값의 일관성을 보장한다.
 *
 * <p>필드 제약:
 *
 * <ul>
 *   <li>cancellable: 기본값 true
 *   <li>cancelDeadlineDays: 기본값 1 (공연 1일 전까지 취소 가능)
 *   <li>refundPolicyText: 최대 1000자
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class RefundPolicy {

  private static final int DEFAULT_CANCEL_DEADLINE_DAYS = 1;
  private static final int REFUND_POLICY_TEXT_MAX_LENGTH = 1000;

  /** 취소 가능 여부 */
  @Column(name = "cancellable")
  private Boolean cancellable;

  /** 취소 마감일 (공연 n일 전) */
  @Column(name = "cancel_deadline_days")
  private Integer cancelDeadlineDays;

  /** 환불 정책 상세 안내 */
  @Column(name = "refund_policy_text", length = REFUND_POLICY_TEXT_MAX_LENGTH)
  private String refundPolicyText;

  /**
   * 환불 정책을 생성한다.
   *
   * @param cancellable 취소 가능 여부 (null이면 true)
   * @param cancelDeadlineDays 취소 마감일 (null이면 1일)
   * @param refundPolicyText 환불 정책 상세 안내
   * @throws ProductException 유효성 검증 실패 시 ({@link ProductErrorCode#INVALID_REFUND_POLICY})
   */
  public RefundPolicy(
      Boolean cancellable,
      Integer cancelDeadlineDays,
      String refundPolicyText) {
    validate(cancelDeadlineDays, refundPolicyText);

    this.cancellable = cancellable != null ? cancellable : true;
    this.cancelDeadlineDays =
        cancelDeadlineDays != null ? cancelDeadlineDays : DEFAULT_CANCEL_DEADLINE_DAYS;
    this.refundPolicyText = refundPolicyText;
  }

  /**
   * 기본 환불 정책을 생성한다.
   *
   * <p>취소 가능, 공연 1일 전까지로 설정된다.
   *
   * @return 기본 RefundPolicy
   */
  public static RefundPolicy defaultPolicy() {
    return new RefundPolicy(true, DEFAULT_CANCEL_DEADLINE_DAYS, null);
  }

  /**
   * 취소 불가 정책을 생성한다.
   *
   * @return 취소 불가 RefundPolicy
   */
  public static RefundPolicy nonRefundable() {
    return new RefundPolicy(false, 0, "본 상품은 취소/환불이 불가합니다.");
  }

  /**
   * 취소 가능한지 확인한다.
   *
   * @return 취소 가능하면 true
   */
  public boolean isCancellable() {
    return cancellable;
  }

  /**
   * 특정 행사일 기준으로 취소 가능한지 확인한다.
   *
   * @param eventDate 행사일
   * @return 오늘 기준 취소 가능하면 true
   */
  public boolean canCancelFor(LocalDate eventDate) {
    if (!cancellable) {
      return false;
    }
    LocalDate deadline = eventDate.minusDays(cancelDeadlineDays);
    return !LocalDate.now().isAfter(deadline);
  }

  /**
   * 취소 마감일을 계산한다.
   *
   * @param eventDate 행사일
   * @return 취소 마감일
   */
  public LocalDate getCancelDeadline(LocalDate eventDate) {
    return eventDate.minusDays(cancelDeadlineDays);
  }

  private static void validate(Integer cancelDeadlineDays, String refundPolicyText) {
    if (cancelDeadlineDays != null && cancelDeadlineDays < 0) {
      throw new ProductException(ProductErrorCode.INVALID_REFUND_POLICY);
    }

    if (refundPolicyText != null && refundPolicyText.length() > REFUND_POLICY_TEXT_MAX_LENGTH) {
      throw new ProductException(ProductErrorCode.INVALID_REFUND_POLICY);
    }
  }
}