package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 일정 Value Object.
 *
 * <p>상품의 시작 일시와 종료 일시를 관리한다. 불변 객체로 설계되어 값의 일관성을 보장한다.
 *
 * <p>유효성 규칙:
 *
 * <ul>
 *   <li>시작 일시와 종료 일시는 필수
 *   <li>종료 일시는 시작 일시 이후여야 함
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Schedule {

  /** 시작 일시 */
  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

  /** 종료 일시 */
  @Column(name = "end_at", nullable = false)
  private LocalDateTime endAt;

  /**
   * 일정을 생성한다.
   *
   * @param startAt 시작 일시 (필수)
   * @param endAt 종료 일시 (필수, 시작 일시 이후)
   * @throws ProductException 유효성 검증 실패 시 ({@link ProductErrorCode#INVALID_SCHEDULE})
   */
  public Schedule(LocalDateTime startAt, LocalDateTime endAt) {
    validate(startAt, endAt);
    this.startAt = startAt;
    this.endAt = endAt;
  }

  /**
   * 일정이 시작되었는지 확인한다.
   *
   * @return 현재 시간이 시작 일시 이후이면 true
   */
  public boolean isStarted() {
    return LocalDateTime.now().isAfter(startAt);
  }

  /**
   * 일정이 종료되었는지 확인한다.
   *
   * @return 현재 시간이 종료 일시 이후이면 true
   */
  public boolean isEnded() {
    return LocalDateTime.now().isAfter(endAt);
  }

  /**
   * 일정이 진행 중인지 확인한다.
   *
   * @return 현재 시간이 시작 일시 이후이고 종료 일시 이전이면 true
   */
  public boolean isOngoing() {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(startAt) && now.isBefore(endAt);
  }

  private static void validate(LocalDateTime startAt, LocalDateTime endAt) {
    requireNonNull(startAt);
    requireNonNull(endAt);
    requireEndAfterStart(startAt, endAt);
  }

  private static void requireNonNull(LocalDateTime value) {
    if (Objects.isNull(value)) {
      throw new ProductException(ProductErrorCode.INVALID_SCHEDULE);
    }
  }

  private static void requireEndAfterStart(LocalDateTime startAt, LocalDateTime endAt) {
    if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
      throw new ProductException(ProductErrorCode.INVALID_SCHEDULE);
    }
  }
}
