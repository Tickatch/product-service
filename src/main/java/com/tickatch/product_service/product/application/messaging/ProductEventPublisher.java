package com.tickatch.product_service.product.application.messaging;

import com.tickatch.product_service.product.domain.Product;

/**
 * 상품 도메인 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다. 이를 통해 Application은 메시징 기술(RabbitMQ, Kafka
 * 등)에 의존하지 않는다.
 */
public interface ProductEventPublisher {

  /**
   * 상품 취소 이벤트를 발행한다.
   *
   * <p>Seat, Reservation, Ticket 서비스에 각각 취소 이벤트를 발행한다. 모든 이벤트는 동일한 correlationId를 공유하여 추적 가능하다.
   *
   * @param product 취소된 상품
   */
  void publishCancelled(Product product);
}
