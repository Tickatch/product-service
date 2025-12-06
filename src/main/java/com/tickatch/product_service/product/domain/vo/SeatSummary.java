package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class SeatSummary {

  @Column(name = "total_seats")
  private Integer totalSeats;

  @Column(name = "available_seats")
  private Integer availableSeats;

  @Column(name = "seat_updated_at")
  private LocalDateTime updatedAt;

  public SeatSummary(Integer totalSeats, Integer availableSeats) {
    int total = totalSeats != null ? totalSeats : 0;
    int available = availableSeats != null ? availableSeats : 0;
    validate(total, available);
    this.totalSeats = total;
    this.availableSeats = available;
    this.updatedAt = LocalDateTime.now();
  }

  public static SeatSummary initialize(int totalSeats) {
    if (totalSeats < 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_COUNT);
    }
    return new SeatSummary(totalSeats, totalSeats);
  }

  public static SeatSummary empty() {
    return new SeatSummary(0, 0);
  }

  public int getSoldSeats() {
    return totalSeats - availableSeats;
  }

  public double getSoldRate() {
    if (totalSeats == 0) {
      return 0.0;
    }
    return (double) getSoldSeats() / totalSeats * 100;
  }

  public boolean isSoldOut() {
    return availableSeats <= 0;
  }

  public boolean hasAvailableSeats() {
    return availableSeats > 0;
  }

  public SeatSummary decreaseAvailable(int count) {
    if (count <= 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_COUNT);
    }
    if (this.availableSeats < count) {
      throw new ProductException(ProductErrorCode.NOT_ENOUGH_SEATS);
    }
    return new SeatSummary(this.totalSeats, this.availableSeats - count);
  }

  public SeatSummary increaseAvailable(int count) {
    if (count <= 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_COUNT);
    }
    int newAvailable = Math.min(totalSeats, this.availableSeats + count);
    return new SeatSummary(this.totalSeats, newAvailable);
  }

  private static void validate(int totalSeats, int availableSeats) {
    if (totalSeats < 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_COUNT);
    }
    if (availableSeats < 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_COUNT);
    }
    if (availableSeats > totalSeats) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_COUNT);
    }
  }
}