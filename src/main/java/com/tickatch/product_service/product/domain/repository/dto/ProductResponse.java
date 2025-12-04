package com.tickatch.product_service.product.domain.repository.dto;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 응답 DTO.
 *
 * <p>상품 조회 시 반환되는 데이터를 담는다. 엔티티를 외부에 노출하지 않고 필요한 필드만 전달한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public final class ProductResponse {

  /** 상품 ID */
  private final Long id;

  /** 상품명 */
  private final String name;

  /** 상품 타입 */
  private final ProductType productType;

  /** 상영 시간 (분) */
  private final Integer runningTime;

  /** 시작 일시 */
  private final LocalDateTime startAt;

  /** 종료 일시 */
  private final LocalDateTime endAt;

  /** 스테이지 ID */
  private final Long stageId;

  /** 상품 상태 */
  private final ProductStatus status;

  /** 생성 일시 */
  private final LocalDateTime createdAt;

  /** 수정 일시 */
  private final LocalDateTime updatedAt;

  /**
   * 상품 엔티티를 응답 DTO로 변환한다.
   *
   * @param product 상품 엔티티
   * @return 상품 응답 DTO
   */
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
