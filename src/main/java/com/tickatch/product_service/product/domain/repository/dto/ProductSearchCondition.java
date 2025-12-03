package com.tickatch.product_service.product.domain.repository.dto;

import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSearchCondition {

  private String name;
  private ProductType productType;
  private ProductStatus status;
  private Long stageId;
}
