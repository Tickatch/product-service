package com.tickatch.product_service.product.infrastructure.messaging.product.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.Schedule;
import io.github.tickatch.common.event.IntegrationEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitProductEventPublisher 테스트")
class RabbitProductEventPublisherTest {

  @Mock private RabbitTemplate rabbitTemplate;

  @Captor private ArgumentCaptor<String> exchangeCaptor;

  @Captor private ArgumentCaptor<String> routingKeyCaptor;

  @Captor private ArgumentCaptor<IntegrationEvent> eventCaptor;

  private RabbitProductEventPublisher publisher;

  private static final String SERVICE_NAME = "product-service";
  private static final String EXCHANGE = "tickatch.product";

  @BeforeEach
  void setUp() {
    publisher = new RabbitProductEventPublisher(rabbitTemplate);
    ReflectionTestUtils.setField(publisher, "serviceName", SERVICE_NAME);
    ReflectionTestUtils.setField(publisher, "productExchange", EXCHANGE);
  }

  @Nested
  class publishCancelled관련_테스트 {

    @Test
    void 상품_취소_시_2개_서비스로_이벤트가_발행된다() {
      Product product = createTestProduct(1L);

      publisher.publishCancelled(product);

      verify(rabbitTemplate, times(2))
          .convertAndSend(
              exchangeCaptor.capture(), routingKeyCaptor.capture(), eventCaptor.capture());
      assertThat(exchangeCaptor.getAllValues()).containsOnly(EXCHANGE);
      assertThat(routingKeyCaptor.getAllValues())
          .containsExactly(
              "product.cancelled.reservation-seat",
              "product.cancelled.reservation");
    }

    @Test
    void 발행된_이벤트에_productId가_포함된다() {
      Long productId = 123L;
      Product product = createTestProduct(productId);

      publisher.publishCancelled(product);

      verify(rabbitTemplate, times(2))
          .convertAndSend(any(String.class), any(String.class), eventCaptor.capture());
      for (IntegrationEvent event : eventCaptor.getAllValues()) {
        assertThat(event.getPayload()).contains("\"productId\":123");
        assertThat(event.getSourceService()).isEqualTo(SERVICE_NAME);
        assertThat(event.getAggregateId()).isEqualTo("123");
        assertThat(event.getAggregateType()).isEqualTo("Product");
      }
    }

    @Test
    void 각_이벤트의_eventType이_올바르게_설정된다() {
      Product product = createTestProduct(1L);

      publisher.publishCancelled(product);

      verify(rabbitTemplate, times(2))
          .convertAndSend(any(String.class), any(String.class), eventCaptor.capture());
      assertThat(eventCaptor.getAllValues())
          .extracting(IntegrationEvent::getEventType)
          .containsExactly(
              "ProductCancelledToReservationSeatEvent",
              "ProductCancelledToReservationEvent");
    }

    @Test
    void RabbitMQ_발행_실패_시_BusinessException이_발생한다() {
      Product product = createTestProduct(1L);
      doThrow(new RuntimeException("RabbitMQ connection failed"))
          .when(rabbitTemplate)
          .convertAndSend(any(String.class), any(String.class), any(IntegrationEvent.class));

      assertThatThrownBy(() -> publisher.publishCancelled(product))
          .isInstanceOf(ProductException.class)
          .satisfies(
              ex -> {
                ProductException pe = (ProductException) ex;
                assertThat(pe.getErrorCode()).isEqualTo(ProductErrorCode.EVENT_PUBLISH_FAILED);
                assertThat(pe.getStatus()).isEqualTo(503);
              });
    }

    @Test
    void 첫_번째_이벤트_발행_실패_시_나머지_이벤트는_발행되지_않는다() {
      Product product = createTestProduct(1L);
      doThrow(new RuntimeException("Connection failed"))
          .when(rabbitTemplate)
          .convertAndSend(
              eq(EXCHANGE), eq("product.cancelled.reservation-seat"), any(IntegrationEvent.class));

      assertThatThrownBy(() -> publisher.publishCancelled(product))
          .isInstanceOf(ProductException.class);

      verify(rabbitTemplate, times(1))
          .convertAndSend(any(String.class), any(String.class), any(IntegrationEvent.class));
    }
  }

  @Nested
  class IntergrationEvent_구조_검증 {

    @Test
    void IntefrationEvent의_모든_필수_필드가_설정된다() {
      Product product = createTestProduct(1L);

      publisher.publishCancelled(product);

      verify(rabbitTemplate, atLeastOnce())
          .convertAndSend(any(String.class), any(String.class), eventCaptor.capture());
      IntegrationEvent event = eventCaptor.getValue();
      assertThat(event.getEventId()).isNotNull();
      assertThat(event.getEventType()).isNotNull();
      assertThat(event.getOccurredAt()).isNotNull();
      assertThat(event.getSourceService()).isEqualTo(SERVICE_NAME);
      assertThat(event.getVersion()).isEqualTo(1);
      assertThat(event.getPayload()).isNotNull();
      assertThat(event.getAggregateId()).isNotNull();
      assertThat(event.getAggregateType()).isEqualTo("Product");
      assertThat(event.getRoutingKey()).isNotNull();
    }
  }

  private Product createTestProduct(Long id) {
    Schedule schedule =
        new Schedule(
            LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(30).plusHours(2));
    Product product = Product.create("테스트 공연", ProductType.CONCERT, 120, schedule, 1L);
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }
}
