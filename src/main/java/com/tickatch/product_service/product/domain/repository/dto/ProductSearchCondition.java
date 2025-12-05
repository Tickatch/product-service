package com.tickatch.product_service.product.domain.repository.dto;

import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 검색 조건 DTO.
 *
 * <p>상품 목록 조회 시 사용되는 검색 조건을 담는다. 모든 필드는 선택 사항이며, null인 경우 해당 조건을 적용하지 않는다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public class ProductSearchCondition {

  /** 상품명 (부분 일치 검색) */
  private String name;

  /** 상품 타입 */
  private ProductType productType;

  /** 상품 상태 */
  private ProductStatus status;

  /** 스테이지 ID */
  private Long stageId;

  /** 판매자 ID*/
  private String sellerId;
}
