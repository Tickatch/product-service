package com.tickatch.product_service.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {

  DRAFT("임시저장"),
  PENDING("판매대기"),
  ON_SALE("판매중"),
  SOLD_OUT("매진"),
  CANCELLED("취소됨");

  private final String description;

  public boolean canChangeTo(ProductStatus target) {
    if (target == null || this == target) {
      return false;
    }

    return switch (this) {
      case DRAFT -> target == PENDING || target == CANCELLED;
      case PENDING -> target == DRAFT || target == ON_SALE || target == CANCELLED;
      case ON_SALE -> target == SOLD_OUT || target == CANCELLED;
      case SOLD_OUT -> target == ON_SALE || target == CANCELLED;
      case CANCELLED -> false;
    };
  }

  public boolean isDraft() {
    return this == DRAFT;
  }

  public boolean isPending() {
    return this == PENDING;
  }

  public boolean isOnSale() {
    return this == ON_SALE;
  }

  public boolean isSoldOut() {
    return this == SOLD_OUT;
  }

  public boolean isCancelled() {
    return this == CANCELLED;
  }
}
