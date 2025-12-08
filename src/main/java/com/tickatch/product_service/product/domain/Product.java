package com.tickatch.product_service.product.domain;

import com.tickatch.product_service.global.domain.AbstractAuditEntity;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.AdmissionPolicy;
import com.tickatch.product_service.product.domain.vo.AgeRestriction;
import com.tickatch.product_service.product.domain.vo.BookingPolicy;
import com.tickatch.product_service.product.domain.vo.ProductContent;
import com.tickatch.product_service.product.domain.vo.ProductStats;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.RefundPolicy;
import com.tickatch.product_service.product.domain.vo.SaleSchedule;
import com.tickatch.product_service.product.domain.vo.Schedule;
import com.tickatch.product_service.product.domain.vo.SeatSummary;
import com.tickatch.product_service.product.domain.vo.Venue;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 엔티티.
 *
 * <p>공연, 뮤지컬, 스포츠 등의 상품 정보를 관리하는 애그리거트 루트이다. 상품은 DRAFT 상태로 생성되며, 상태 전이 규칙에 따라 상태가 변경된다.
 *
 * <p>상태 전이 규칙:
 *
 * <ul>
 *   <li>DRAFT → PENDING, CANCELLED
 *   <li>PENDING → APPROVED, REJECTED, CANCELLED
 *   <li>APPROVED → SCHEDULED, CANCELLED
 *   <li>REJECTED → DRAFT, CANCELLED
 *   <li>SCHEDULED → ON_SALE, CANCELLED
 *   <li>ON_SALE → CLOSED, CANCELLED
 *   <li>CLOSED → COMPLETED, CANCELLED
 *   <li>COMPLETED, CANCELLED → (변경 불가)
 * </ul>
 *
 * <p>매진 여부는 상태가 아닌 {@code SeatSummary.isSoldOut()}으로 판단한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductStatus
 * @see ProductType
 * @see Schedule
 * @see SaleSchedule
 * @see Venue
 * @see SeatSummary
 * @see ProductStats
 * @see ProductContent
 * @see AgeRestriction
 * @see BookingPolicy
 * @see AdmissionPolicy
 * @see RefundPolicy
 * @see SeatGrade
 */
@Entity
@Table(name = "p_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends AbstractAuditEntity {

  /** 상품명 최대 길이 */
  private static final int NAME_MAX_LENGTH = 50;

  /** 반려 사유 최대 길이 */
  private static final int REJECTION_REASON_MAX_LENGTH = 500;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  /** 판매자 ID */
  @Column(name = "seller_id", nullable = false, length = 50)
  private String sellerId;

  /** 상품명 */
  @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
  private String name;

  /** 상품 타입 */
  @Enumerated(EnumType.STRING)
  @Column(name = "product_type", nullable = false)
  private ProductType productType;

  /** 상영 시간 (분) */
  @Column(name = "running_time", nullable = false)
  private Integer runningTime;

  /** 행사 일정 */
  @Embedded
  private Schedule schedule;

  /** 예매 일정 */
  @Embedded
  private SaleSchedule saleSchedule;

  /** 장소 정보 */
  @Embedded
  private Venue venue;

  /** 좌석 현황 (총합) */
  @Embedded
  private SeatSummary seatSummary;

  /** 통계 정보 */
  @Embedded
  private ProductStats stats;

  // ========== 2차 확장: 콘텐츠/정책 ==========

  /** 행사 상세 정보 */
  @Embedded
  private ProductContent content;

  /** 관람 제한 */
  @Embedded
  private AgeRestriction ageRestriction;

  /** 예매 정책 */
  @Embedded
  private BookingPolicy bookingPolicy;

  /** 입장 정책 */
  @Embedded
  private AdmissionPolicy admissionPolicy;

  /** 환불 정책 */
  @Embedded
  private RefundPolicy refundPolicy;

  /** 등급별 좌석 정보 */
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SeatGrade> seatGrades = new ArrayList<>();

  /** 반려 사유 */
  @Column(name = "rejection_reason", length = REJECTION_REASON_MAX_LENGTH)
  private String rejectionReason;

  /** 상품 상태 */
  @Enumerated(EnumType.STRING)
  @Column(name = "product_status", nullable = false)
  private ProductStatus status;

  private Product(
      String sellerId,
      String name,
      ProductType productType,
      Integer runningTime,
      Schedule schedule,
      SaleSchedule saleSchedule,
      Venue venue,
      ProductContent content,
      AgeRestriction ageRestriction,
      BookingPolicy bookingPolicy,
      AdmissionPolicy admissionPolicy,
      RefundPolicy refundPolicy) {
    this.sellerId = sellerId;
    this.name = name;
    this.productType = productType;
    this.runningTime = runningTime;
    this.schedule = schedule;
    this.saleSchedule = saleSchedule;
    this.venue = venue;
    this.content = content;
    this.ageRestriction = ageRestriction;
    this.bookingPolicy = bookingPolicy;
    this.admissionPolicy = admissionPolicy;
    this.refundPolicy = refundPolicy;
    // 도메인 내부 계산/초기화
    this.seatSummary = SeatSummary.empty();
    this.stats = ProductStats.empty();
    this.status = ProductStatus.DRAFT;
  }

  /**
   * 상품을 생성한다.
   *
   * <p>새 상품은 DRAFT 상태로 생성된다. 모든 콘텐츠와 정책은 서비스 레이어에서 조립하여 주입해야 한다.
   * seatSummary는 SeatGrade 추가 시 자동 계산되며, stats는 0으로 초기화된다.
   *
   * @param sellerId 판매자 ID (필수)
   * @param name 상품명 (필수, 최대 50자)
   * @param productType 상품 타입 (필수)
   * @param runningTime 상영 시간 (필수, 양수)
   * @param schedule 행사 일정 (필수)
   * @param saleSchedule 예매 일정 (필수)
   * @param venue 장소 정보 (필수)
   * @param content 상품 콘텐츠 (필수)
   * @param ageRestriction 관람 제한 (필수)
   * @param bookingPolicy 예매 정책 (필수)
   * @param admissionPolicy 입장 정책 (필수)
   * @param refundPolicy 환불 정책 (필수)
   * @return 생성된 상품 엔티티
   * @throws ProductException 유효성 검증 실패 시
   */
  public static Product create(
      String sellerId,
      String name,
      ProductType productType,
      Integer runningTime,
      Schedule schedule,
      SaleSchedule saleSchedule,
      Venue venue,
      ProductContent content,
      AgeRestriction ageRestriction,
      BookingPolicy bookingPolicy,
      AdmissionPolicy admissionPolicy,
      RefundPolicy refundPolicy) {
    // 기본 정보 검증
    validateSellerId(sellerId);
    validateName(name);
    validateProductType(productType);
    validateRunningTime(runningTime);
    validateSchedule(schedule);
    validateSaleSchedule(saleSchedule);
    validateVenue(venue);
    validateScheduleConsistency(schedule, saleSchedule);

    // 콘텐츠/정책 null 검증
    validateContent(content);
    validateAgeRestriction(ageRestriction);
    validateBookingPolicy(bookingPolicy);
    validateAdmissionPolicy(admissionPolicy);
    validateRefundPolicy(refundPolicy);

    return new Product(
        sellerId, name, productType, runningTime,
        schedule, saleSchedule, venue,
        content, ageRestriction, bookingPolicy, admissionPolicy, refundPolicy);
  }

  /**
   * 상품 정보를 수정한다.
   *
   * <p>수정 가능한 상태(DRAFT, REJECTED)에서만 수정 가능하다.
   *
   * @param name 상품명
   * @param productType 상품 타입
   * @param runningTime 상영 시간
   * @param schedule 행사 일정
   * @param saleSchedule 예매 일정
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   * @throws ProductException 유효성 검증 실패 시
   */
  public void update(
      String name,
      ProductType productType,
      Integer runningTime,
      Schedule schedule,
      SaleSchedule saleSchedule) {
    validateEditable();
    validateName(name);
    validateProductType(productType);
    validateRunningTime(runningTime);
    validateSchedule(schedule);
    validateSaleSchedule(saleSchedule);
    validateScheduleConsistency(schedule, saleSchedule);

    this.name = name;
    this.productType = productType;
    this.runningTime = runningTime;
    this.schedule = schedule;
    this.saleSchedule = saleSchedule;
  }

  // ========== 콘텐츠/정책 수정 메서드 ==========

  /**
   * 상품 콘텐츠를 수정한다.
   *
   * <p>수정 가능한 상태(DRAFT, REJECTED)에서만 수정 가능하다.
   *
   * @param content 상품 콘텐츠
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   */
  public void updateContent(ProductContent content) {
    validateEditable();
    this.content = content != null ? content : ProductContent.empty();
  }

  /**
   * 관람 제한을 수정한다.
   *
   * <p>수정 가능한 상태(DRAFT, REJECTED)에서만 수정 가능하다.
   *
   * @param ageRestriction 관람 제한
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   */
  public void updateAgeRestriction(AgeRestriction ageRestriction) {
    validateEditable();
    this.ageRestriction = ageRestriction != null ? ageRestriction : AgeRestriction.defaultRestriction();
  }

  /**
   * 예매 정책을 수정한다.
   *
   * <p>수정 가능한 상태(DRAFT, REJECTED)에서만 수정 가능하다.
   *
   * @param bookingPolicy 예매 정책
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   */
  public void updateBookingPolicy(BookingPolicy bookingPolicy) {
    validateEditable();
    this.bookingPolicy = bookingPolicy != null ? bookingPolicy : BookingPolicy.defaultPolicy();
  }

  /**
   * 입장 정책을 수정한다.
   *
   * <p>수정 가능한 상태(DRAFT, REJECTED)에서만 수정 가능하다.
   *
   * @param admissionPolicy 입장 정책
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   */
  public void updateAdmissionPolicy(AdmissionPolicy admissionPolicy) {
    validateEditable();
    this.admissionPolicy = admissionPolicy != null ? admissionPolicy : AdmissionPolicy.defaultPolicy();
  }

  /**
   * 환불 정책을 수정한다.
   *
   * <p>수정 가능한 상태(DRAFT, REJECTED)에서만 수정 가능하다.
   *
   * @param refundPolicy 환불 정책
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   */
  public void updateRefundPolicy(RefundPolicy refundPolicy) {
    validateEditable();
    this.refundPolicy = refundPolicy != null ? refundPolicy : RefundPolicy.defaultPolicy();
  }

  // ========== 심사 관련 메서드 ==========

  /**
   * 상품을 승인한다.
   *
   * <p>PENDING 상태에서만 승인 가능하다. 승인 후 APPROVED 상태가 된다.
   *
   * @throws ProductException PENDING 상태가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_PENDING})
   */
  public void approve() {
    if (!this.status.isPending()) {
      throw new ProductException(ProductErrorCode.PRODUCT_NOT_PENDING);
    }
    this.status = ProductStatus.APPROVED;
  }

  /**
   * 상품을 반려한다.
   *
   * <p>PENDING 상태에서만 반려 가능하다. 반려 후 REJECTED 상태가 된다.
   *
   * @param reason 반려 사유 (필수)
   * @throws ProductException PENDING 상태가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_PENDING})
   * @throws ProductException 반려 사유가 없는 경우 ({@link ProductErrorCode#INVALID_REJECTION_REASON})
   */
  public void reject(String reason) {
    if (!this.status.isPending()) {
      throw new ProductException(ProductErrorCode.PRODUCT_NOT_PENDING);
    }
    validateRejectionReason(reason);

    this.rejectionReason = reason;
    this.status = ProductStatus.REJECTED;
  }

  /**
   * 반려된 상품을 재제출한다.
   *
   * <p>REJECTED 상태에서만 재제출 가능하다. 재제출 후 DRAFT 상태가 되며 반려 사유가 초기화된다.
   *
   * @throws ProductException REJECTED 상태가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_REJECTED})
   */
  public void resubmit() {
    if (!this.status.isRejected()) {
      throw new ProductException(ProductErrorCode.PRODUCT_NOT_REJECTED);
    }
    this.rejectionReason = null;
    this.status = ProductStatus.DRAFT;
  }

  // ========== 장소 관련 메서드 ==========

  /**
   * 장소를 변경한다.
   *
   * <p>행사 일정이 시작되기 전에만 변경 가능하다.
   *
   * @param venue 변경할 장소 정보
   * @throws ProductException 취소된 상품인 경우 ({@link ProductErrorCode#PRODUCT_ALREADY_CANCELLED})
   * @throws ProductException 일정이 시작된 경우 ({@link ProductErrorCode#VENUE_CHANGE_NOT_ALLOWED})
   */
  public void changeVenue(Venue venue) {
    validateNotCancelled();
    validateVenue(venue);
    validateBeforeScheduleStart();

    this.venue = venue;
  }

  // ========== 좌석 관련 메서드 ==========

  /**
   * 좌석 현황을 초기화한다.
   *
   * @param totalSeats 총 좌석 수
   */
  public void initializeSeatSummary(int totalSeats) {
    this.seatSummary = SeatSummary.initialize(totalSeats);
  }

  /**
   * 잔여 좌석을 차감한다.
   *
   * <p>예매 시 호출된다.
   *
   * @param count 차감할 좌석 수
   */
  public void decreaseAvailableSeats(int count) {
    this.seatSummary = this.seatSummary.decreaseAvailable(count);
  }

  /**
   * 잔여 좌석을 복구한다.
   *
   * <p>예매 취소 시 호출된다.
   *
   * @param count 복구할 좌석 수
   */
  public void increaseAvailableSeats(int count) {
    this.seatSummary = this.seatSummary.increaseAvailable(count);
  }

  // ========== SeatGrade 관련 메서드 ==========

  /**
   * 좌석 등급을 추가한다.
   *
   * <p>수정 가능한 상태(DRAFT, REJECTED)에서만 추가 가능하다.
   *
   * @param gradeName 등급명
   * @param price 가격
   * @param totalSeats 총 좌석수
   * @param displayOrder 표시 순서
   * @return 생성된 SeatGrade
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   */
  public SeatGrade addSeatGrade(String gradeName, Long price, Integer totalSeats, Integer displayOrder) {
    validateEditable();
    SeatGrade seatGrade = SeatGrade.create(this, gradeName, price, totalSeats, displayOrder);
    this.seatGrades.add(seatGrade);
    recalculateSeatSummary();
    return seatGrade;
  }

  /**
   * 좌석 등급을 제거한다.
   *
   * <p>수정 가능한 상태(DRAFT, REJECTED)에서만 제거 가능하다.
   *
   * @param seatGradeId 제거할 SeatGrade ID
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   * @throws ProductException 해당 등급이 없는 경우 ({@link ProductErrorCode#SEAT_GRADE_NOT_FOUND})
   */
  public void removeSeatGrade(Long seatGradeId) {
    validateEditable();
    SeatGrade target = findSeatGradeById(seatGradeId);
    this.seatGrades.remove(target);
    recalculateSeatSummary();
  }

  /**
   * 등급별 잔여 좌석을 차감한다.
   *
   * <p>예매 시 호출된다. SeatSummary도 함께 갱신된다.
   *
   * @param gradeName 등급명
   * @param count 차감할 좌석 수
   * @throws ProductException 해당 등급이 없는 경우 ({@link ProductErrorCode#SEAT_GRADE_NOT_FOUND})
   */
  public void decreaseSeatGradeAvailable(String gradeName, int count) {
    SeatGrade seatGrade = findSeatGradeByName(gradeName);
    seatGrade.decreaseAvailableSeats(count);
    this.seatSummary = this.seatSummary.decreaseAvailable(count);
  }

  /**
   * 등급별 잔여 좌석을 복구한다.
   *
   * <p>예매 취소 시 호출된다. SeatSummary도 함께 갱신된다.
   *
   * @param gradeName 등급명
   * @param count 복구할 좌석 수
   * @throws ProductException 해당 등급이 없는 경우 ({@link ProductErrorCode#SEAT_GRADE_NOT_FOUND})
   */
  public void increaseSeatGradeAvailable(String gradeName, int count) {
    SeatGrade seatGrade = findSeatGradeByName(gradeName);
    seatGrade.increaseAvailableSeats(count);
    this.seatSummary = this.seatSummary.increaseAvailable(count);
  }

  /**
   * 등급별 좌석 정보를 읽기 전용 리스트로 반환한다.
   *
   * @return 읽기 전용 SeatGrade 리스트
   */
  public List<SeatGrade> getSeatGrades() {
    return Collections.unmodifiableList(seatGrades);
  }

  /**
   * SeatSummary를 SeatGrade 총합으로 재계산한다.
   */
  private void recalculateSeatSummary() {
    int totalSeats = seatGrades.stream()
        .mapToInt(SeatGrade::getTotalSeats)
        .sum();
    int availableSeats = seatGrades.stream()
        .mapToInt(SeatGrade::getAvailableSeats)
        .sum();
    this.seatSummary = new SeatSummary(totalSeats, availableSeats);
  }

  private SeatGrade findSeatGradeById(Long seatGradeId) {
    return seatGrades.stream()
        .filter(sg -> sg.getId().equals(seatGradeId))
        .findFirst()
        .orElseThrow(() -> new ProductException(ProductErrorCode.SEAT_GRADE_NOT_FOUND));
  }

  private SeatGrade findSeatGradeByName(String gradeName) {
    return seatGrades.stream()
        .filter(sg -> sg.getGradeName().equals(gradeName))
        .findFirst()
        .orElseThrow(() -> new ProductException(ProductErrorCode.SEAT_GRADE_NOT_FOUND));
  }

  // ========== 통계 관련 메서드 ==========

  /** 조회수를 증가한다. */
  public void incrementViewCount() {
    this.stats = this.stats.incrementViewCount();
  }

  /**
   * 조회수를 동기화한다.
   *
   * <p>Redis에서 배치로 동기화할 때 사용한다.
   *
   * @param viewCount 동기화할 조회수
   */
  public void syncViewCount(Long viewCount) {
    this.stats = this.stats.syncViewCount(viewCount);
  }

  /** 예매 수를 증가한다. */
  public void incrementReservationCount() {
    this.stats = this.stats.incrementReservationCount();
  }

  /**
   * 예매 수를 감소한다.
   *
   * <p>예매 취소 시 호출된다.
   */
  public void decrementReservationCount() {
    this.stats = this.stats.decrementReservationCount();
  }

  // ========== 상태 관련 메서드 ==========

  /**
   * 상태를 변경한다.
   *
   * @param newStatus 변경할 상태
   * @throws ProductException 취소된 상품인 경우 ({@link ProductErrorCode#PRODUCT_ALREADY_CANCELLED})
   * @throws ProductException 상태 전이 규칙 위반 시 ({@link
   *     ProductErrorCode#PRODUCT_STATUS_CHANGE_NOT_ALLOWED})
   */
  public void changeStatus(ProductStatus newStatus) {
    validateNotCancelled();
    validateProductStatus(newStatus);
    validateCanChangeToStatus(newStatus);

    this.status = newStatus;
  }

  /**
   * 상품을 취소한다.
   *
   * <p>상품 취소 시 soft delete가 수행된다.
   *
   * @param cancelledBy 취소 요청자 ID
   * @throws ProductException 이미 취소된 상품인 경우 ({@link ProductErrorCode#PRODUCT_ALREADY_CANCELLED})
   */
  public void cancel(String cancelledBy) {
    if (this.status.isCancelled()) {
      throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }

    this.status = ProductStatus.CANCELLED;
    delete(cancelledBy);
  }

  // ========== 조회 메서드 ==========

  /**
   * 판매자 소유 여부를 확인한다.
   *
   * @param sellerId 확인할 판매자 ID
   * @return 소유자이면 true
   */
  public boolean isOwnedBy(String sellerId) {
    return this.sellerId.equals(sellerId);
  }

  /**
   * 구매 가능 여부를 확인한다.
   *
   * <p>심사 승인된 상태(APPROVED 이후)이고, 예매 기간 중이며, 잔여 좌석이 있으면 구매 가능하다. 상태 전이(ON_SALE)가 늦어지더라도 실제 판매 기간이면
   * 구매 가능하다.
   *
   * @return 구매 가능하면 true
   */
  public boolean canPurchase() {
    return this.status.canBePurchased()
        && this.saleSchedule.isInSalePeriod()
        && this.seatSummary.hasAvailableSeats();
  }

  /**
   * 매진 여부를 확인한다.
   *
   * @return 매진이면 true
   */
  public boolean isSoldOut() {
    return this.seatSummary.isSoldOut();
  }

  /**
   * 심사 제출 가능 여부를 확인한다.
   *
   * <p>필수 콘텐츠(description, posterImageUrl)가 입력되어 있어야 한다.
   *
   * @return 심사 제출 가능하면 true
   */
  public boolean canSubmitForApproval() {
    return this.status.isDraft() && this.content.hasRequiredFields();
  }

  /**
   * 행사 시작 일시를 반환한다.
   *
   * @return 시작 일시
   */
  public LocalDateTime getStartAt() {
    return this.schedule.getStartAt();
  }

  /**
   * 행사 종료 일시를 반환한다.
   *
   * @return 종료 일시
   */
  public LocalDateTime getEndAt() {
    return this.schedule.getEndAt();
  }

  /**
   * 예매 시작 일시를 반환한다.
   *
   * @return 예매 시작 일시
   */
  public LocalDateTime getSaleStartAt() {
    return this.saleSchedule.getSaleStartAt();
  }

  /**
   * 예매 종료 일시를 반환한다.
   *
   * @return 예매 종료 일시
   */
  public LocalDateTime getSaleEndAt() {
    return this.saleSchedule.getSaleEndAt();
  }

  /**
   * 스테이지 ID를 반환한다.
   *
   * @return 스테이지 ID
   */
  public Long getStageId() {
    return this.venue.getStageId();
  }

  // ========== 상태 확인 메서드 ==========

  /**
   * DRAFT 상태 여부를 확인한다.
   *
   * @return DRAFT 상태이면 true
   */
  public boolean isDraft() {
    return this.status.isDraft();
  }

  /**
   * PENDING 상태 여부를 확인한다.
   *
   * @return PENDING 상태이면 true
   */
  public boolean isPending() {
    return this.status.isPending();
  }

  /**
   * APPROVED 상태 여부를 확인한다.
   *
   * @return APPROVED 상태이면 true
   */
  public boolean isApproved() {
    return this.status.isApproved();
  }

  /**
   * REJECTED 상태 여부를 확인한다.
   *
   * @return REJECTED 상태이면 true
   */
  public boolean isRejected() {
    return this.status.isRejected();
  }

  /**
   * SCHEDULED 상태 여부를 확인한다.
   *
   * @return SCHEDULED 상태이면 true
   */
  public boolean isScheduled() {
    return this.status.isScheduled();
  }

  /**
   * ON_SALE 상태 여부를 확인한다.
   *
   * @return ON_SALE 상태이면 true
   */
  public boolean isOnSale() {
    return this.status.isOnSale();
  }

  /**
   * CLOSED 상태 여부를 확인한다.
   *
   * @return CLOSED 상태이면 true
   */
  public boolean isClosed() {
    return this.status.isClosed();
  }

  /**
   * COMPLETED 상태 여부를 확인한다.
   *
   * @return COMPLETED 상태이면 true
   */
  public boolean isCompleted() {
    return this.status.isCompleted();
  }

  /**
   * CANCELLED 상태 여부를 확인한다.
   *
   * @return CANCELLED 상태이면 true
   */
  public boolean isCancelled() {
    return this.status.isCancelled();
  }

  /**
   * 수정 가능한 상태인지 확인한다.
   *
   * @return DRAFT 또는 REJECTED 상태이면 true
   */
  public boolean isEditable() {
    return this.status.isEditable();
  }

  /**
   * 최종 상태인지 확인한다.
   *
   * @return COMPLETED 또는 CANCELLED 상태이면 true
   */
  public boolean isTerminal() {
    return this.status.isTerminal();
  }

  // ========== 검증 메서드 ==========

  private void validateNotCancelled() {
    if (this.status.isCancelled()) {
      throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }
  }

  private void validateEditable() {
    if (!this.status.isEditable()) {
      throw new ProductException(ProductErrorCode.PRODUCT_NOT_EDITABLE);
    }
  }

  private void validateBeforeScheduleStart() {
    if (schedule.isStarted()) {
      throw new ProductException(ProductErrorCode.VENUE_CHANGE_NOT_ALLOWED);
    }
  }

  private static void validateSellerId(String sellerId) {
    if (Objects.isNull(sellerId) || sellerId.isBlank()) {
      throw new ProductException(ProductErrorCode.INVALID_SELLER_ID);
    }
  }

  private static void validateName(String name) {
    if (Objects.isNull(name) || name.isBlank()) {
      throw new ProductException(ProductErrorCode.INVALID_PRODUCT_NAME);
    }
    if (name.length() > NAME_MAX_LENGTH) {
      throw new ProductException(ProductErrorCode.INVALID_PRODUCT_NAME);
    }
  }

  private static void validateProductType(ProductType productType) {
    if (Objects.isNull(productType)) {
      throw new ProductException(ProductErrorCode.INVALID_PRODUCT_TYPE);
    }
  }

  private static void validateRunningTime(Integer runningTime) {
    if (Objects.isNull(runningTime) || runningTime <= 0) {
      throw new ProductException(ProductErrorCode.INVALID_RUNNING_TIME);
    }
  }

  private static void validateSchedule(Schedule schedule) {
    if (Objects.isNull(schedule)) {
      throw new ProductException(ProductErrorCode.INVALID_SCHEDULE);
    }
  }

  private static void validateSaleSchedule(SaleSchedule saleSchedule) {
    if (Objects.isNull(saleSchedule)) {
      throw new ProductException(ProductErrorCode.INVALID_SALE_SCHEDULE);
    }
  }

  private static void validateVenue(Venue venue) {
    if (Objects.isNull(venue)) {
      throw new ProductException(ProductErrorCode.INVALID_VENUE);
    }
  }

  private static void validateRejectionReason(String reason) {
    if (Objects.isNull(reason) || reason.isBlank()) {
      throw new ProductException(ProductErrorCode.INVALID_REJECTION_REASON);
    }
    if (reason.length() > REJECTION_REASON_MAX_LENGTH) {
      throw new ProductException(ProductErrorCode.INVALID_REJECTION_REASON);
    }
  }

  private static void validateProductStatus(ProductStatus status) {
    if (Objects.isNull(status)) {
      throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
    }
  }

  private static void validateScheduleConsistency(Schedule schedule, SaleSchedule saleSchedule) {
    // 예매 시작일은 행사 시작일보다 이전이어야 함
    if (!saleSchedule.getSaleStartAt().isBefore(schedule.getStartAt())) {
      throw new ProductException(ProductErrorCode.SALE_MUST_START_BEFORE_EVENT);
    }
    // 예매 종료일은 행사 시작일보다 이전이어야 함
    if (!saleSchedule.getSaleEndAt().isBefore(schedule.getStartAt())) {
      throw new ProductException(ProductErrorCode.SALE_MUST_END_BEFORE_EVENT);
    }
  }

  private static void validateContent(ProductContent content) {
    if (Objects.isNull(content)) {
      throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CONTENT);
    }
  }

  private static void validateAgeRestriction(AgeRestriction ageRestriction) {
    if (Objects.isNull(ageRestriction)) {
      throw new ProductException(ProductErrorCode.INVALID_AGE_RESTRICTION);
    }
  }

  private static void validateBookingPolicy(BookingPolicy bookingPolicy) {
    if (Objects.isNull(bookingPolicy)) {
      throw new ProductException(ProductErrorCode.INVALID_BOOKING_POLICY);
    }
  }

  private static void validateAdmissionPolicy(AdmissionPolicy admissionPolicy) {
    if (Objects.isNull(admissionPolicy)) {
      throw new ProductException(ProductErrorCode.INVALID_ADMISSION_POLICY);
    }
  }

  private static void validateRefundPolicy(RefundPolicy refundPolicy) {
    if (Objects.isNull(refundPolicy)) {
      throw new ProductException(ProductErrorCode.INVALID_REFUND_POLICY);
    }
  }

  private void validateCanChangeToStatus(ProductStatus newStatus) {
    if (!this.status.canChangeTo(newStatus)) {
      throw new ProductException(
          ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED, this.status.name(), newStatus.name());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Product product)) return false;
    return Objects.equals(id, product.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}