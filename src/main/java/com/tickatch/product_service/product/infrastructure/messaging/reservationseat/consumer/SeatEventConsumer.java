package com.tickatch.product_service.product.infrastructure.messaging.reservationseat.consumer;

import com.tickatch.product_service.product.application.service.ProductCommandService;
import com.tickatch.product_service.product.infrastructure.messaging.config.RabbitMQConfig;
import com.tickatch.product_service.product.infrastructure.messaging.reservationseat.event.SeatReleasedEvent;
import com.tickatch.product_service.product.infrastructure.messaging.reservationseat.event.SeatReservedEvent;
import io.github.tickatch.common.event.EventContext;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 좌석 이벤트 Consumer.
 *
 * <p>ReservationSeat 서비스에서 발행하는 좌석 관련 이벤트를 수신하여 처리한다.
 *
 * <p>처리하는 이벤트:
 * <ul>
 *   <li>SeatReservedEvent: 좌석 예약 시 잔여 좌석 차감 및 예매 수 증가
 *   <li>SeatReleasedEvent: 좌석 해제 시 잔여 좌석 복구 및 예매 수 감소
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatEventConsumer {

  private final ProductCommandService productCommandService;

  /**
   * 좌석 예약 이벤트를 수신하여 처리한다.
   *
   * <p>처리 내용:
   * <ul>
   *   <li>SeatSummary.availableSeats 감소
   *   <li>ProductStats.reservationCount 증가
   * </ul>
   *
   * @param integrationEvent IntegrationEvent
   */
  @RabbitListener(queues = RabbitMQConfig.QUEUE_SEAT_RESERVED_PRODUCT)
  public void handleSeatReserved(IntegrationEvent integrationEvent) {
    log.info("좌석 예약 이벤트 수신. eventId: {}, traceId: {}",
        integrationEvent.getEventId(), integrationEvent.getTraceId());

    EventContext.run(integrationEvent, event -> {
      SeatReservedEvent payload = event.getPayloadAs(SeatReservedEvent.class);

      log.info("좌석 예약 처리 시작. productId: {}, grade: {}, count: {}",
          payload.getProductId(), payload.getGrade(), payload.getCount());

      // 잔여 좌석 차감
      productCommandService.decreaseAvailableSeats(
          payload.getProductId(),
          payload.getCount());

      // 예매 수 증가
      productCommandService.incrementReservationCount(payload.getProductId());

      // TODO: 2차 확장 - 등급별 좌석 차감
      // productCommandService.decreaseSeatGradeAvailable(
      //     payload.getProductId(),
      //     payload.getGrade(),
      //     payload.getCount());

      log.info("좌석 예약 처리 완료. productId: {}", payload.getProductId());
    });
  }

  /**
   * 좌석 해제 이벤트를 수신하여 처리한다.
   *
   * <p>처리 내용:
   * <ul>
   *   <li>SeatSummary.availableSeats 증가
   *   <li>ProductStats.reservationCount 감소
   * </ul>
   *
   * @param integrationEvent IntegrationEvent
   */
  @RabbitListener(queues = RabbitMQConfig.QUEUE_SEAT_RELEASED_PRODUCT)
  public void handleSeatReleased(IntegrationEvent integrationEvent) {
    log.info("좌석 해제 이벤트 수신. eventId: {}, traceId: {}",
        integrationEvent.getEventId(), integrationEvent.getTraceId());

    EventContext.run(integrationEvent, event -> {
      SeatReleasedEvent payload = event.getPayloadAs(SeatReleasedEvent.class);

      log.info("좌석 해제 처리 시작. productId: {}, grade: {}, count: {}",
          payload.getProductId(), payload.getGrade(), payload.getCount());

      // 잔여 좌석 복구
      productCommandService.increaseAvailableSeats(
          payload.getProductId(),
          payload.getCount());

      // 예매 수 감소
      productCommandService.decrementReservationCount(payload.getProductId());

      // TODO: 2차 확장 - 등급별 좌석 복구
      // productCommandService.increaseSeatGradeAvailable(
      //     payload.getProductId(),
      //     payload.getGrade(),
      //     payload.getCount());

      log.info("좌석 해제 처리 완료. productId: {}", payload.getProductId());
    });
  }
}