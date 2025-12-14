package com.tickatch.product_service.product.application.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 예약 좌석 생성 요청 DTO.
 *
 * <p>ReservationSeat 서비스에 개별 좌석 생성을 요청할 때 사용한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public class SeatCreateRequest {

  /** 상품 ID */
  private final Long productId;

  /** 개별 좌석 정보 리스트 */
  private final List<SeatInfo> seatCreateInfos;

  /**
   * 개별 좌석 정보.
   *
   * @param seatNumber 좌석번호 (예: "A1", "B2")
   * @param grade 등급명 (예: "VIP", "R", "S")
   * @param price 가격
   */
  @Getter
  @Builder
  public static class SeatInfo {
    private final String seatNumber;
    private final String grade;
    private final Long price;
  }
}
