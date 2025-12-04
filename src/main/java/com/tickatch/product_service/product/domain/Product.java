package com.tickatch.product_service.product.domain;

import com.tickatch.product_service.global.domain.AbstractAuditEntity;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.Schedule;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
 *   <li>PENDING → DRAFT, ON_SALE, CANCELLED
 *   <li>ON_SALE → SOLD_OUT, CANCELLED
 *   <li>SOLD_OUT → ON_SALE, CANCELLED
 *   <li>CANCELLED → (변경 불가)
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductStatus
 * @see ProductType
 * @see Schedule
 */
@Entity
@Table(name = "p_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends AbstractAuditEntity {

  /** 상품명 최대 길이 */
  private static final int NAME_MAX_LENGTH = 50;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "product_type", nullable = false)
  private ProductType productType;

  @Column(name = "running_time", nullable = false)
  private Integer runningTime;

  @Embedded private Schedule schedule;

  @Column(name = "stage_id", nullable = false)
  private Long stageId;

  @Enumerated(EnumType.STRING)
  @Column(name = "product_status", nullable = false)
  private ProductStatus status;

  private Product(
      String name, ProductType productType, Integer runningTime, Schedule schedule, Long stageId) {
    this.name = name;
    this.productType = productType;
    this.runningTime = runningTime;
    this.schedule = schedule;
    this.stageId = stageId;
    this.status = ProductStatus.DRAFT;
  }

  /**
   * 상품을 생성한다.
   *
   * <p>새 상품은 DRAFT 상태로 생성된다.
   *
   * @param name 상품명 (필수, 최대 50자)
   * @param productType 상품 타입 (필수)
   * @param runningTime 상영 시간 (필수, 양수)
   * @param schedule 일정 (필수)
   * @param stageId 스테이지 ID (필수)
   * @return 생성된 상품 엔티티
   * @throws ProductException 유효성 검증 실패 시
   */
  public static Product create(
      String name, ProductType productType, Integer runningTime, Schedule schedule, Long stageId) {
    validateName(name);
    validateProductType(productType);
    validateRunningTime(runningTime);
    validateSchedule(schedule);
    validateStageId(stageId);

    return new Product(name, productType, runningTime, schedule, stageId);
  }

  /**
   * 상품 정보를 수정한다.
   *
   * @param name 상품명
   * @param productType 상품 타입
   * @param runningTime 상영 시간
   * @param schedule 일정
   * @throws ProductException 취소된 상품인 경우 ({@link ProductErrorCode#PRODUCT_ALREADY_CANCELLED})
   * @throws ProductException 유효성 검증 실패 시
   */
  public void update(String name, ProductType productType, Integer runningTime, Schedule schedule) {
    validateNotCancelled();
    validateName(name);
    validateProductType(productType);
    validateRunningTime(runningTime);
    validateSchedule(schedule);

    this.name = name;
    this.productType = productType;
    this.runningTime = runningTime;
    this.schedule = schedule;
  }

  /**
   * 스테이지를 변경한다.
   *
   * <p>일정이 시작되기 전에만 변경 가능하다.
   *
   * @param stageId 변경할 스테이지 ID
   * @throws ProductException 취소된 상품인 경우 ({@link ProductErrorCode#PRODUCT_ALREADY_CANCELLED})
   * @throws ProductException 일정이 시작된 경우 ({@link ProductErrorCode#STAGE_CHANGE_NOT_ALLOWED})
   */
  public void changeStage(Long stageId) {
    validateNotCancelled();
    validateStageId(stageId);
    validateIsStartedSchedule();

    this.stageId = stageId;
  }

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

  /**
   * 시작 일시를 반환한다.
   *
   * @return 시작 일시
   */
  public LocalDateTime getStartAt() {
    return this.schedule.getStartAt();
  }

  /**
   * 종료 일시를 반환한다.
   *
   * @return 종료 일시
   */
  public LocalDateTime getEndAt() {
    return this.schedule.getEndAt();
  }

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
   * ON_SALE 상태 여부를 확인한다.
   *
   * @return ON_SALE 상태이면 true
   */
  public boolean isOnSale() {
    return this.status.isOnSale();
  }

  /**
   * SOLD_OUT 상태 여부를 확인한다.
   *
   * @return SOLD_OUT 상태이면 true
   */
  public boolean isSoldOut() {
    return this.status.isSoldOut();
  }

  /**
   * CANCELLED 상태 여부를 확인한다.
   *
   * @return CANCELLED 상태이면 true
   */
  public boolean isCancelled() {
    return this.status.isCancelled();
  }

  private void validateNotCancelled() {
    if (this.status.isCancelled()) {
      throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
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

  private static void validateStageId(Long stageId) {
    if (Objects.isNull(stageId)) {
      throw new ProductException(ProductErrorCode.INVALID_STAGE_ID);
    }
  }

  private static void validateProductStatus(ProductStatus status) {
    if (Objects.isNull(status)) {
      throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
    }
  }

  private void validateCanChangeToStatus(ProductStatus newStatus) {
    if (!this.status.canChangeTo(newStatus)) {
      throw new ProductException(
          ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED, this.status.name(), newStatus.name());
    }
  }

  /**
   * 일정이 시작되었는지 검증한다.
   *
   * @throws ProductException 일정이 시작된 경우 ({@link ProductErrorCode#STAGE_CHANGE_NOT_ALLOWED})
   */
  public void validateIsStartedSchedule() {
    if (schedule.isStarted()) {
      throw new ProductException(ProductErrorCode.STAGE_CHANGE_NOT_ALLOWED);
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
