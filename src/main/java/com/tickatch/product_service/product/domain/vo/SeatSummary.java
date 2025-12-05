package com.tickatch.product_service.product.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatSummary {

  @Column(name = "total_seats")
  private Integer totalSeats;

  @Column(name = "available_seats")
  private Integer availableSeats;

  @Column(name = "seat_updated_at")
  private LocalDateTime updatedAt;

  public SeatSummary(Integer totalSeats, Integer availableSeats) {
    this.totalSeats = totalSeats != null ? totalSeats : 0;
    this.availableSeats = availableSeats != null ? availableSeats : 0;
    this.updatedAt = LocalDateTime.now();
  }

  public static SeatSummary initialize(int totalSeats) {
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
    int newAvailable = Math.max(0, this.availableSeats - count);
    return new SeatSummary(this.totalSeats, newAvailable);
  }

  public SeatSummary increaseAvailable(int count) {
    int newAvailable = Math.min(totalSeats, this.availableSeats + count);
    return new SeatSummary(this.totalSeats, newAvailable);
  }
}
