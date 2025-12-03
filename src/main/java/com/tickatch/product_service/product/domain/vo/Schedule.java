package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 상품 일정을 나타내는 Value Object
 *
 * @author 김준형
 * @since 1.0.0
 */
@Embeddable
public record Schedule(
    @Column(name = "start_at", nullable = false) LocalDateTime startAt,
    @Column(name = "end_at", nullable = false) LocalDateTime endAt) {
  public Schedule {
    validate(startAt, endAt);
  }

  public boolean isStarted() {
    return LocalDateTime.now().isAfter(startAt);
  }

  public boolean isEnded() {
    return LocalDateTime.now().isAfter(endAt);
  }

  public boolean isOngoing() {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(startAt) && now.isBefore(endAt);
  }

  private static void validate(LocalDateTime startAt, LocalDateTime endAt) {
    if (Objects.isNull(startAt)) {
      throw new ProductException(ProductErrorCode.INVALID_SCHEDULE);
    }
    if (Objects.isNull(endAt)) {
      throw new ProductException(ProductErrorCode.INVALID_SCHEDULE);
    }
    if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
      throw new ProductException(ProductErrorCode.INVALID_SCHEDULE);
    }
  }
}
