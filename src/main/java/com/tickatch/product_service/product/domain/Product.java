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

@Entity
@Table(name = "p_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends AbstractAuditEntity {

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

  public static Product create(
      String name, ProductType productType, Integer runningTime, Schedule schedule, Long stageId) {
    validateName(name);
    validateProductType(productType);
    validateRunningTime(runningTime);
    validateSchedule(schedule);
    validateStageId(stageId);

    return new Product(name, productType, runningTime, schedule, stageId);
  }

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

  public void changeStage(Long stageId) {
    validateNotCancelled();
    validateStageId(stageId);

    if (schedule.isStarted()) {
      throw new ProductException(ProductErrorCode.STAGE_CHANGE_NOT_ALLOWED);
    }

    this.stageId = stageId;
  }

  public void changeStatus(ProductStatus newStatus) {
    validateNotCancelled();
    validateProductStatus(newStatus);

    if (!this.status.canChangeTo(newStatus)) {
      throw new ProductException(
          ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED, this.status.name(), newStatus.name());
    }

    this.status = newStatus;
  }

  public void cancel(String cancelledBy) {
    if (this.status.isCancelled()) {
      throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }

    this.status = ProductStatus.CANCELLED;
    delete(cancelledBy);
  }

  public LocalDateTime getStartAt() {
    return this.schedule.getStartAt();
  }

  public LocalDateTime getEndAt() {
    return this.schedule.getEndAt();
  }

  public boolean isDraft() {
    return this.status.isDraft();
  }

  public boolean isPending() {
    return this.status.isPending();
  }

  public boolean isOnSale() {
    return this.status.isOnSale();
  }

  public boolean isSoldOut() {
    return this.status.isSoldOut();
  }

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
