package com.tickatch.product_service.product.infrastructure.messaging.product.publisher;

import com.tickatch.product_service.product.application.messaging.ProductEventPublisher;
import com.tickatch.product_service.product.application.messaging.event.ProductCancelledToReservationEvent;
import com.tickatch.product_service.product.application.messaging.event.ProductCancelledToReservationSeatEvent;
import com.tickatch.product_service.product.application.messaging.event.ProductCancelledToTicketEvent;
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
 * <p>상품 도메인에서 발생하는 이벤트를 RabbitMQ를 통해 다른 서비스로 발행한다.
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

  @Override
  public void publishCancelled(Product product) {
    Long productId = product.getId();

    log.info("상품 취소 이벤트 발행 시작. productId: {}", productId);

    try {
      publishToReservationSeat(productId);
      publishToReservation(productId);
      publishToTicket(productId);

      log.info("상품 취소 이벤트 발행 완료. productId: {}", productId);
    } catch (Exception e) {
      log.error("상품 취소 이벤트 발행 실패. productId: {}", productId, e);
      throw new ProductException(ProductErrorCode.EVENT_PUBLISH_FAILED, e, productId);
    }
  }

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

  private void publishToTicket(Long productId) {
    ProductCancelledToTicketEvent domainEvent = new ProductCancelledToTicketEvent(productId);
    IntegrationEvent integrationEvent = IntegrationEvent.from(domainEvent, serviceName);

    rabbitTemplate.convertAndSend(productExchange, domainEvent.getRoutingKey(), integrationEvent);

    log.debug(
        "Ticket 취소 이벤트 발행. productId: {}, routingKey: {}", productId, domainEvent.getRoutingKey());
  }
}
