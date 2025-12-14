package com.tickatch.product_service.product.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductStats {

  @Column(name = "view_count")
  private Long viewCount;

  @Column(name = "reservation_count")
  private Integer reservationCount;

  public ProductStats(Long viewCount, Integer reservationCount) {
    this.viewCount = viewCount != null ? viewCount : 0L;
    this.reservationCount = reservationCount != null ? reservationCount : 0;
  }

  public static ProductStats empty() {
    return new ProductStats(0L, 0);
  }

  public ProductStats incrementViewCount() {
    return new ProductStats(this.viewCount + 1, this.reservationCount);
  }

  public ProductStats incrementReservationCount() {
    return new ProductStats(this.viewCount, this.reservationCount + 1);
  }

  public ProductStats decrementReservationCount() {
    int newCount = Math.max(0, this.reservationCount - 1);
    return new ProductStats(this.viewCount, newCount);
  }

  public ProductStats syncViewCount(Long count) {
    return new ProductStats(count, this.reservationCount);
  }
}
