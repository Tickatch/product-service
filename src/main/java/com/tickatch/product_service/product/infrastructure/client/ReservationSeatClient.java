package com.tickatch.product_service.product.infrastructure.client;

import com.tickatch.product_service.product.application.dto.SeatCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 예약 좌석 서비스 Feign Client.
 *
 * <p>ReservationSeat 서비스와의 통신을 담당한다. 상품 생성/수정 시 개별 좌석 정보를 전달한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@FeignClient(name = "reservation-seat-service", path = "/api/v1/reservation-seats")
public interface ReservationSeatClient {

  /**
   * 예약 좌석을 일괄 생성한다.
   *
   * <p>상품 생성 시 호출되며, 개별 좌석 정보를 ReservationSeat 서비스에 전달한다.
   *
   * @param request 좌석 생성 요청 (productId + 개별 좌석 정보 리스트)
   */
  @PostMapping
  void createSeats(@RequestBody SeatCreateRequest request);
}
