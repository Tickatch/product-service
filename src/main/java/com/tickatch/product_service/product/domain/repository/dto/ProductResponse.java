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

  /** 판매자 ID */
  private final String sellerId;

  /** 상품명 */
  private final String name;

  /** 상품 타입 */
  private final ProductType productType;

  /** 상영 시간 (분) */
  private final Integer runningTime;

  /** 행사 시작 일시 */
  private final LocalDateTime startAt;

  /** 행사 종료 일시 */
  private final LocalDateTime endAt;

  /** 예매 시작 일시 */
  private final LocalDateTime saleStartAt;

  /** 예매 종료 일시 */
  private final LocalDateTime saleEndAt;

  /** 스테이지 ID */
  private final Long stageId;

  /** 스테이지명 */
  private final String stageName;

  /** 아트홀 ID */
  private final Long artHallId;

  /** 아트홀명 */
  private final String artHallName;

  /** 아트홀 주소 */
  private final String artHallAddress;

  /** 총 좌석 수 */
  private final int totalSeats;

  /** 잔여 좌석 수 */
  private final int availableSeats;

  /** 매진 여부 */
  private final boolean soldOut;

  /** 조회수 */
  private final Long viewCount;

  /** 예매 수 */
  private final int reservationCount;

  /** 구매 가능 여부 */
  private final boolean purchasable;

  /** 상품 상태 */
  private final ProductStatus status;

  /** 반려 사유 */
  private final String rejectionReason;

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
        .sellerId(product.getSellerId())
        .name(product.getName())
        .productType(product.getProductType())
        .runningTime(product.getRunningTime())
        .startAt(product.getStartAt())
        .endAt(product.getEndAt())
        .saleStartAt(product.getSaleStartAt())
        .saleEndAt(product.getSaleEndAt())
        .stageId(product.getStageId())
        .stageName(product.getVenue().getStageName())
        .artHallId(product.getVenue().getArtHallId())
        .artHallName(product.getVenue().getArtHallName())
        .artHallAddress(product.getVenue().getArtHallAddress())
        .totalSeats(product.getSeatSummary().getTotalSeats())
        .availableSeats(product.getSeatSummary().getAvailableSeats())
        .soldOut(product.isSoldOut())
        .viewCount(product.getStats().getViewCount())
        .reservationCount(product.getStats().getReservationCount())
        .purchasable(product.canPurchase())
        .status(product.getStatus())
        .rejectionReason(product.getRejectionReason())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
}