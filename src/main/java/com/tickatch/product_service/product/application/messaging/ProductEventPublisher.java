package com.tickatch.product_service.product.application.messaging;

import com.tickatch.product_service.product.domain.Product;

/**
 * 상품 도메인 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다. 이를 통해 Application은 메시징 기술(RabbitMQ, Kafka
 * 등)에 의존하지 않는다.
 *
 * <p>DIP(의존성 역전 원칙)를 적용하여 도메인 로직과 인프라스트럭처를 분리한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see
 *     com.tickatch.product_service.product.infrastructure.messaging.product.publisher.RabbitProductEventPublisher
 */
public interface ProductEventPublisher {

  /**
   * 상품 취소 이벤트를 발행한다.
   *
   * <p>다음 서비스로 각각 취소 이벤트를 발행한다:
   *
   * <ul>
   *   <li>ReservationSeat - 예매 좌석 취소 처리
   *   <li>Reservation - 예매 취소 처리
   *   <li>Ticket - 티켓 취소 처리
   * </ul>
   *
   * <p>모든 이벤트는 동일한 traceId를 공유하여 분산 추적이 가능하다.
   *
   * @param product 취소된 상품 엔티티
   */
  void publishCancelled(Product product);
}
