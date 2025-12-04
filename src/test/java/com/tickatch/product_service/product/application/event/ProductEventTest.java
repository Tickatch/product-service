package com.tickatch.product_service.product.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.product_service.product.application.messaging.event.ProductCancelledToReservationEvent;
import com.tickatch.product_service.product.application.messaging.event.ProductCancelledToReservationSeatEvent;
import com.tickatch.product_service.product.application.messaging.event.ProductCancelledToTicketEvent;
import io.github.tickatch.common.event.IntegrationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Product 이벤트 테스트")
public class ProductEventTest {

  private static final String SOURCE_SERVICE = "product-service";

  @Nested
  class 예약좌석_서비스_관련_이벤트_테스트 {

    @Test
    void 이벤트_생성_시_필드가_올바르게_설정된다() {
      Long productId = 1L;

      ProductCancelledToReservationSeatEvent event =
          new ProductCancelledToReservationSeatEvent(productId);

      assertThat(event.getProductId()).isEqualTo(productId);
      assertThat(event.getEventId()).isNotNull();
      assertThat(event.getEventType()).isEqualTo("ProductCancelledToReservationSeatEvent");
      assertThat(event.getOccurredAt()).isNotNull();
      assertThat(event.getVersion()).isEqualTo(1);
      assertThat(event.getAggregateId()).isEqualTo("1");
      assertThat(event.getAggregateType()).isEqualTo("Product");
      assertThat(event.getRoutingKey()).isEqualTo("product.cancelled.reservation-seat");
    }

    @Test
    void IntegrationEvent로_변환_시_필드가_올바르게_설정된다() {
      Long productId = 1L;
      ProductCancelledToReservationSeatEvent domainEvent =
          new ProductCancelledToReservationSeatEvent(productId);

      IntegrationEvent integrationEvent = IntegrationEvent.from(domainEvent, SOURCE_SERVICE);

      assertThat(integrationEvent.getEventId()).isNotNull();
      assertThat(integrationEvent.getEventType())
          .isEqualTo("ProductCancelledToReservationSeatEvent");
      assertThat(integrationEvent.getSourceService()).isEqualTo(SOURCE_SERVICE);
      assertThat(integrationEvent.getOccurredAt()).isNotNull();
      assertThat(integrationEvent.getVersion()).isEqualTo(1);
      assertThat(integrationEvent.getAggregateId()).isEqualTo("1");
      assertThat(integrationEvent.getAggregateType()).isEqualTo("Product");
      assertThat(integrationEvent.getRoutingKey()).isEqualTo("product.cancelled.reservation-seat");
      assertThat(integrationEvent.getPayload()).isNotNull();
      assertThat(integrationEvent.getPayload()).contains("\"productId\":1");
    }

    @Test
    void IntegrationEvent에서_Payload를_파싱할_수_있다() {
      Long productId = 1L;
      ProductCancelledToReservationSeatEvent domainEvent =
          new ProductCancelledToReservationSeatEvent(productId);
      IntegrationEvent integrationEvent = IntegrationEvent.from(domainEvent, SOURCE_SERVICE);

      ProductCancelledToReservationSeatEvent parsedEvent =
          integrationEvent.getPayloadAs(ProductCancelledToReservationSeatEvent.class);

      assertThat(parsedEvent).isNotNull();
      assertThat(parsedEvent.getProductId()).isEqualTo(productId);
      assertThat(parsedEvent.getEventType()).isEqualTo("ProductCancelledToReservationSeatEvent");
    }
  }

  @Nested
  class 예약_서비스_관련_이벤트_테스트 {

    @Test
    void 이벤트_생성_시_필드가_올바르게_설정된다() {
      Long productId = 2L;

      ProductCancelledToReservationEvent event = new ProductCancelledToReservationEvent(productId);

      assertThat(event.getProductId()).isEqualTo(productId);
      assertThat(event.getEventId()).isNotNull();
      assertThat(event.getEventType()).isEqualTo("ProductCancelledToReservationEvent");
      assertThat(event.getAggregateId()).isEqualTo("2");
      assertThat(event.getAggregateType()).isEqualTo("Product");
      assertThat(event.getRoutingKey()).isEqualTo("product.cancelled.reservation");
    }

    @Test
    void IntegrationEvent에서_Payload를_파싱할_수_있다() {
      Long productId = 2L;
      ProductCancelledToReservationEvent domainEvent =
          new ProductCancelledToReservationEvent(productId);
      IntegrationEvent integrationEvent = IntegrationEvent.from(domainEvent, SOURCE_SERVICE);

      ProductCancelledToReservationEvent parsedEvent =
          integrationEvent.getPayloadAs(ProductCancelledToReservationEvent.class);

      assertThat(parsedEvent).isNotNull();
      assertThat(parsedEvent.getProductId()).isEqualTo(productId);
    }
  }

  @Nested
  class 티켓_서비스_관련_이벤트_테스트 {

    @Test
    void 이벤트_생성_시_필드가_올바르게_설정된다() {
      Long productId = 3L;

      ProductCancelledToTicketEvent event = new ProductCancelledToTicketEvent(productId);

      assertThat(event.getProductId()).isEqualTo(productId);
      assertThat(event.getEventId()).isNotNull();
      assertThat(event.getEventType()).isEqualTo("ProductCancelledToTicketEvent");
      assertThat(event.getAggregateId()).isEqualTo("3");
      assertThat(event.getAggregateType()).isEqualTo("Product");
      assertThat(event.getRoutingKey()).isEqualTo("product.cancelled.ticket");
    }

    @Test
    void IntegrationEvent에서_Payload를_파싱할_수_있다() {
      Long productId = 3L;
      ProductCancelledToTicketEvent domainEvent = new ProductCancelledToTicketEvent(productId);
      IntegrationEvent integrationEvent = IntegrationEvent.from(domainEvent, SOURCE_SERVICE);

      ProductCancelledToTicketEvent parsedEvent =
          integrationEvent.getPayloadAs(ProductCancelledToTicketEvent.class);

      assertThat(parsedEvent).isNotNull();
      assertThat(parsedEvent.getProductId()).isEqualTo(productId);
    }
  }

  @Nested
  class 모든_이벤트_공통_테스트 {

    @Test
    void 전체_이벤트_모두_동일한_productId를_생성하여_각각_다른_routingKey를_가진다() {
      Long productId = 100L;

      ProductCancelledToReservationSeatEvent seatEvent =
          new ProductCancelledToReservationSeatEvent(productId);
      ProductCancelledToReservationEvent reservationEvent =
          new ProductCancelledToReservationEvent(productId);
      ProductCancelledToTicketEvent ticketEvent = new ProductCancelledToTicketEvent(productId);

      assertThat(seatEvent.getRoutingKey()).isEqualTo("product.cancelled.reservation-seat");
      assertThat(reservationEvent.getRoutingKey()).isEqualTo("product.cancelled.reservation");
      assertThat(ticketEvent.getRoutingKey()).isEqualTo("product.cancelled.ticket");
      assertThat(seatEvent.getProductId()).isEqualTo(productId);
      assertThat(reservationEvent.getProductId()).isEqualTo(productId);
      assertThat(ticketEvent.getProductId()).isEqualTo(productId);
    }

    @Test
    void 각_이벤트는_고유한_eventId를_가진다() {
      Long productId = 100L;

      ProductCancelledToReservationSeatEvent event1 =
          new ProductCancelledToReservationSeatEvent(productId);
      ProductCancelledToReservationSeatEvent event2 =
          new ProductCancelledToReservationSeatEvent(productId);

      assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }
  }
}
