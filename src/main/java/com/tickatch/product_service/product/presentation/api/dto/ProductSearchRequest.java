package com.tickatch.product_service.product.presentation.api.dto;

import com.tickatch.product_service.product.domain.repository.dto.ProductSearchCondition;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 상품 검색 요청 DTO.
 *
 * <p>상품 목록 조회 시 사용되는 검색 조건을 담는다. 모든 필드는 선택 사항이며, null인 경우 해당 조건을 적용하지 않는다.
 *
 * @param name 상품명 (부분 일치 검색)
 * @param productType 상품 타입 (CONCERT, MUSICAL, SPORTS 등)
 * @param status 상품 상태 (DRAFT, OPEN, CLOSED, CANCELLED)
 * @param stageId 스테이지 ID
 * @author Tickatch
 * @since 1.0.0
 */
@Schema(description = "상품 검색 요청")
public record ProductSearchRequest(
    @Schema(description = "상품명 (부분 일치)", example = "레미제라블") String name,
    @Schema(description = "상품 타입", example = "MUSICAL") ProductType productType,
    @Schema(description = "상품 상태", example = "OPEN") ProductStatus status,
    @Schema(description = "스테이지 ID", example = "1") Long stageId) {

  /**
   * 검색 요청을 검색 조건 객체로 변환한다.
   *
   * @return 변환된 검색 조건
   */
  public ProductSearchCondition toCondition() {
    return ProductSearchCondition.builder()
        .name(name)
        .productType(productType)
        .status(status)
        .stageId(stageId)
        .build();
  }
}
