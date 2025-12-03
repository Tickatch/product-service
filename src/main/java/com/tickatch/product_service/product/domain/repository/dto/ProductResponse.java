package com.tickatch.product_service.product.domain.repository.dto;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class ProductResponse {

  private final Long id;
  private final String name;
  private final ProductType productType;
  private final Integer runningTime;
  private final LocalDateTime startAt;
  private final LocalDateTime endAt;
  private final Long stageId;
  private final ProductStatus status;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  public static ProductResponse from(Product product) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .productType(product.getProductType())
        .runningTime(product.getRunningTime())
        .startAt(product.getStartAt())
        .endAt(product.getEndAt())
        .stageId(product.getStageId())
        .status(product.getStatus())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
}
