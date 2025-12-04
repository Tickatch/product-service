package com.tickatch.product_service.product.application.messaging.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import lombok.Getter;

import java.time.Instant;

/**
 * Reservation(예매) 서비스로 전달되는 상품 취소 이벤트.
 *
 * <p>상품이 취소되면 해당 상품의 모든 예매를 취소 처리해야 한다.
 */
@Getter
public class ProductCancelledToReservationEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "Product";
  private static final String ROUTING_KEY = "product.cancelled.reservation";

  private final Long productId;

  public ProductCancelledToReservationEvent(Long productId) {
    super();
    this.productId = productId;
  }

  @JsonCreator
  public ProductCancelledToReservationEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("productId") Long productId
  ) {
    super(eventId, occurredAt, version);
    this.productId = productId;
  }

  @Override
  public String getAggregateId() {
    return String.valueOf(productId);
  }

  @Override
  public String getAggregateType() {
    return AGGREGATE_TYPE;
  }

  @Override
  public String getRoutingKey() {
    return ROUTING_KEY;
  }
}