package com.tickatch.product_service.product.infrastructure.messaging.product.publisher;

import com.tickatch.product_service.product.application.messaging.ProductEventPublisher;
import com.tickatch.product_service.product.application.messaging.event.ProductCancelledToReservationEvent;
import com.tickatch.product_service.product.application.messaging.event.ProductCancelledToReservationSeatEvent;
import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 기반 상품 이벤트 발행 구현체.
 *
 * <p>상품 도메인에서 발생하는 이벤트를 RabbitMQ를 통해 다른 서비스로 발행한다. 상품 취소 시 ReservationSeat, Reservation, Ticket
 * 서비스로 각각 이벤트를 발행한다.
 *
 * <p>발행 흐름:
 *
 * <ol>
 *   <li>DomainEvent 생성
 *   <li>IntegrationEvent로 래핑
 *   <li>RabbitTemplate을 통해 발행
 * </ol>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductEventPublisher
 * @see IntegrationEvent
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitProductEventPublisher implements ProductEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Value("${spring.application.name:product-service}")
  private String serviceName;

  @Value("${messaging.exchange.product:tickatch.product}")
  private String productExchange;

  /**
   * 상품 취소 이벤트를 발행한다.
   *
   * <p>3개 서비스(ReservationSeat, Reservation, Ticket)로 각각 이벤트를 발행한다. 하나라도 발행에 실패하면 {@link
   * ProductException}을 발생시킨다.
   *
   * @param product 취소된 상품 엔티티
   * @throws ProductException 이벤트 발행 실패 시 ({@link ProductErrorCode#EVENT_PUBLISH_FAILED})
   */
  @Override
  public void publishCancelled(Product product) {
    Long productId = product.getId();

    log.info("상품 취소 이벤트 발행 시작. productId: {}", productId);

    try {
      publishToReservationSeat(productId);
      publishToReservation(productId);

      log.info("상품 취소 이벤트 발행 완료. productId: {}", productId);
    } catch (Exception e) {
      log.error("상품 취소 이벤트 발행 실패. productId: {}", productId, e);
      throw new ProductException(ProductErrorCode.EVENT_PUBLISH_FAILED, e, productId);
    }
  }

  /**
   * ReservationSeat 서비스로 취소 이벤트를 발행한다.
   *
   * @param productId 취소된 상품 ID
   */
  private void publishToReservationSeat(Long productId) {
    ProductCancelledToReservationSeatEvent domainEvent =
        new ProductCancelledToReservationSeatEvent(productId);
    IntegrationEvent integrationEvent = IntegrationEvent.from(domainEvent, serviceName);

    rabbitTemplate.convertAndSend(productExchange, domainEvent.getRoutingKey(), integrationEvent);

    log.debug(
        "ReservationSeat 취소 이벤트 발행. productId: {}, routingKey: {}",
        productId,
        domainEvent.getRoutingKey());
  }

  /**
   * Reservation 서비스로 취소 이벤트를 발행한다.
   *
   * @param productId 취소된 상품 ID
   */
  private void publishToReservation(Long productId) {
    ProductCancelledToReservationEvent domainEvent =
        new ProductCancelledToReservationEvent(productId);
    IntegrationEvent integrationEvent = IntegrationEvent.from(domainEvent, serviceName);

    rabbitTemplate.convertAndSend(productExchange, domainEvent.getRoutingKey(), integrationEvent);

    log.debug(
        "Reservation 취소 이벤트 발행. productId: {}, routingKey: {}",
        productId,
        domainEvent.getRoutingKey());
  }
}
