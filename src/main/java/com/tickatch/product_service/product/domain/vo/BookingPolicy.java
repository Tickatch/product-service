package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예매 정책 Value Object.
 *
 * <p>예매 시 적용되는 정책을 관리한다. 불변 객체로 설계되어 값의 일관성을 보장한다.
 *
 * <p>필드 제약:
 *
 * <ul>
 *   <li>maxTicketsPerPerson: 1~10, 기본값 4
 *   <li>idVerificationRequired: 기본값 false
 *   <li>transferable: 기본값 true
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class BookingPolicy {

  private static final int MIN_TICKETS_PER_PERSON = 1;
  private static final int MAX_TICKETS_PER_PERSON = 10;
  private static final int DEFAULT_MAX_TICKETS = 4;

  /** 1인당 최대 예매 매수 */
  @Column(name = "max_tickets_per_person")
  private Integer maxTicketsPerPerson;

  /** 본인확인 필요 여부 */
  @Column(name = "id_verification_required")
  private Boolean idVerificationRequired;

  /** 양도 가능 여부 */
  @Column(name = "transferable")
  private Boolean transferable;

  /**
   * 예매 정책을 생성한다.
   *
   * @param maxTicketsPerPerson 1인당 최대 예매 매수 (null이면 기본값 4)
   * @param idVerificationRequired 본인확인 필요 여부 (null이면 false)
   * @param transferable 양도 가능 여부 (null이면 true)
   * @throws ProductException 최대 매수가 범위를 벗어난 경우 ({@link ProductErrorCode#INVALID_BOOKING_POLICY})
   */
  public BookingPolicy(
      Integer maxTicketsPerPerson,
      Boolean idVerificationRequired,
      Boolean transferable) {
    int maxTickets = maxTicketsPerPerson != null ? maxTicketsPerPerson : DEFAULT_MAX_TICKETS;
    validate(maxTickets);

    this.maxTicketsPerPerson = maxTickets;
    this.idVerificationRequired = idVerificationRequired != null ? idVerificationRequired : false;
    this.transferable = transferable != null ? transferable : true;
  }

  /**
   * 기본 예매 정책을 생성한다.
   *
   * <p>1인당 4매, 본인확인 불필요, 양도 가능으로 설정된다.
   *
   * @return 기본 BookingPolicy
   */
  public static BookingPolicy defaultPolicy() {
    return new BookingPolicy(DEFAULT_MAX_TICKETS, false, true);
  }

  /**
   * 요청한 수량이 예매 가능한지 확인한다.
   *
   * @param quantity 예매 요청 수량
   * @return 예매 가능하면 true
   */
  public boolean canBook(int quantity) {
    return quantity > 0 && quantity <= maxTicketsPerPerson;
  }

  /**
   * 본인확인이 필요한지 확인한다.
   *
   * @return 본인확인 필요 시 true
   */
  public boolean requiresIdVerification() {
    return idVerificationRequired;
  }

  /**
   * 양도 가능한지 확인한다.
   *
   * @return 양도 가능하면 true
   */
  public boolean isTransferable() {
    return transferable;
  }

  private static void validate(int maxTicketsPerPerson) {
    if (maxTicketsPerPerson < MIN_TICKETS_PER_PERSON
        || maxTicketsPerPerson > MAX_TICKETS_PER_PERSON) {
      throw new ProductException(ProductErrorCode.INVALID_BOOKING_POLICY);
    }
  }
}