package com.tickatch.product_service.product.presentation.api.dto;

import com.tickatch.product_service.product.domain.repository.dto.ProductSearchCondition;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;

public record ProductSearchRequest(
    String name, ProductType productType, ProductStatus status, Long stageId) {
  public ProductSearchCondition toCondition() {
    return ProductSearchCondition.builder()
        .name(name)
        .productType(productType)
        .status(status)
        .stageId(stageId)
        .build();
  }
}
