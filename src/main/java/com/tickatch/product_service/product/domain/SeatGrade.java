package com.tickatch.product_service.product.domain;

import com.tickatch.product_service.global.domain.AbstractTimeEntity;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좌석 등급 엔티티.
 *
 * <p>상품의 등급별 좌석 정보를 관리한다. Product Aggregate에 종속되며, Product를 통해서만 접근한다.
 *
 * <p>이 엔티티는 ReservationSeat 서비스의 개별 좌석 정보를 비정규화하여 상품 조회 시 등급별 가격 및 잔여석 정보를 빠르게 제공하기 위해 사용된다.
 *
 * <p>데이터 흐름:
 *
 * <ul>
 *   <li>상품 생성 시: SeatGrade 생성 → ReservationSeat 서비스로 개별 좌석 생성 요청
 *   <li>예매 시: ReservationSeat에서 이벤트 발행 → SeatGrade.availableSeats 감소
 *   <li>취소 시: ReservationSeat에서 이벤트 발행 → SeatGrade.availableSeats 증가
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see Product
 */
@Entity
@Table(name = "p_product_seat_grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatGrade extends AbstractTimeEntity {

  private static final int GRADE_NAME_MAX_LENGTH = 20;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  /** 소속 상품 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  /** 등급명 (VIP, R, S 등) */
  @Column(name = "grade_name", nullable = false, length = GRADE_NAME_MAX_LENGTH)
  private String gradeName;

  /** 가격 */
  @Column(name = "price", nullable = false)
  private Long price;

  /** 총 좌석수 */
  @Column(name = "total_seats", nullable = false)
  private Integer totalSeats;

  /** 잔여 좌석수 */
  @Column(name = "available_seats", nullable = false)
  private Integer availableSeats;

  /** 표시 순서 */
  @Column(name = "display_order")
  private Integer displayOrder;

  private SeatGrade(
      Product product, String gradeName, Long price, Integer totalSeats, Integer displayOrder) {
    this.product = product;
    this.gradeName = gradeName;
    this.price = price;
    this.totalSeats = totalSeats;
    this.availableSeats = totalSeats;
    this.displayOrder = displayOrder;
  }

  /**
   * 좌석 등급을 생성한다.
   *
   * <p>초기 잔여 좌석수는 총 좌석수와 동일하게 설정된다.
   *
   * @param product 소속 상품 (필수)
   * @param gradeName 등급명 (필수, 최대 20자)
   * @param price 가격 (필수, 0 이상)
   * @param totalSeats 총 좌석수 (필수, 1 이상)
   * @param displayOrder 표시 순서
   * @return 생성된 SeatGrade
   * @throws ProductException 유효성 검증 실패 시
   */
  public static SeatGrade create(
      Product product, String gradeName, Long price, Integer totalSeats, Integer displayOrder) {
    validateProduct(product);
    validateGradeName(gradeName);
    validatePrice(price);
    validateTotalSeats(totalSeats);

    return new SeatGrade(product, gradeName, price, totalSeats, displayOrder);
  }

  /**
   * 잔여 좌석을 차감한다.
   *
   * <p>예매 시 호출된다.
   *
   * @param count 차감할 좌석 수
   * @throws ProductException 차감 수가 0 이하인 경우 ({@link ProductErrorCode#INVALID_SEAT_COUNT})
   * @throws ProductException 잔여 좌석이 부족한 경우 ({@link ProductErrorCode#NOT_ENOUGH_SEATS})
   */
  public void decreaseAvailableSeats(int count) {
    if (count <= 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_COUNT);
    }
    if (this.availableSeats < count) {
      throw new ProductException(ProductErrorCode.NOT_ENOUGH_SEATS);
    }
    this.availableSeats -= count;
  }

  /**
   * 잔여 좌석을 복구한다.
   *
   * <p>예매 취소 시 호출된다.
   *
   * @param count 복구할 좌석 수
   * @throws ProductException 복구 수가 0 이하인 경우 ({@link ProductErrorCode#INVALID_SEAT_COUNT})
   */
  public void increaseAvailableSeats(int count) {
    if (count <= 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_COUNT);
    }
    this.availableSeats = Math.min(this.totalSeats, this.availableSeats + count);
  }

  /**
   * 매진 여부를 확인한다.
   *
   * @return 잔여 좌석이 0이면 true
   */
  public boolean isSoldOut() {
    return this.availableSeats <= 0;
  }

  /**
   * 잔여 좌석이 있는지 확인한다.
   *
   * @return 잔여 좌석이 1 이상이면 true
   */
  public boolean hasAvailableSeats() {
    return this.availableSeats > 0;
  }

  /**
   * 판매된 좌석 수를 반환한다.
   *
   * @return 판매 좌석 수
   */
  public int getSoldSeats() {
    return this.totalSeats - this.availableSeats;
  }

  /**
   * 판매율을 반환한다.
   *
   * @return 판매율 (퍼센트)
   */
  public double getSoldRate() {
    if (this.totalSeats == 0) {
      return 0.0;
    }
    return (double) getSoldSeats() / this.totalSeats * 100;
  }

  // ========== 검증 메서드 ==========

  private static void validateProduct(Product product) {
    if (product == null) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_GRADE);
    }
  }

  private static void validateGradeName(String gradeName) {
    if (gradeName == null || gradeName.isBlank()) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_GRADE);
    }
    if (gradeName.length() > GRADE_NAME_MAX_LENGTH) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_GRADE);
    }
  }

  private static void validatePrice(Long price) {
    if (price == null || price < 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_GRADE);
    }
  }

  private static void validateTotalSeats(Integer totalSeats) {
    if (totalSeats == null || totalSeats <= 0) {
      throw new ProductException(ProductErrorCode.INVALID_SEAT_GRADE);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SeatGrade seatGrade)) return false;
    return Objects.equals(id, seatGrade.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
