package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class SaleSchedule {

  private LocalDateTime saleStartAt;

  private LocalDateTime saleEndAt;

  public SaleSchedule(LocalDateTime saleStartAt, LocalDateTime saleEndAt) {
    validate(saleStartAt, saleEndAt);
    this.saleStartAt = saleStartAt;
    this.saleEndAt = saleEndAt;
  }

  public boolean isSaleStarted() {
    return LocalDateTime.now().isAfter(saleStartAt);
  }

  public boolean isSaleEnded() {
    return LocalDateTime.now().isAfter(saleEndAt);
  }

  public boolean isInSalePeriod() {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(saleStartAt) && now.isBefore(saleEndAt);
  }

  public boolean isBeforeSaleStart() {
    return LocalDateTime.now().isBefore(saleStartAt);
  }

  private static void validate(LocalDateTime saleStartAt, LocalDateTime saleEndAt) {
    requireNonNull(saleStartAt);
    requireNonNull(saleEndAt);
    requireEndAfterStart(saleStartAt, saleEndAt);
  }

  private static void requireNonNull(LocalDateTime value) {
    if (Objects.isNull(value)) {
      throw new ProductException(ProductErrorCode.INVALID_SALE_SCHEDULE);
    }
  }

  private static void requireEndAfterStart(LocalDateTime startAt, LocalDateTime endAt) {
    if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
      throw new ProductException(ProductErrorCode.INVALID_SALE_SCHEDULE);
    }
  }
}
