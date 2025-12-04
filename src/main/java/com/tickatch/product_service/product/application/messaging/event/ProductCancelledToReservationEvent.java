package com.tickatch.product_service.product.application.messaging.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

/**
 * Reservation(예매) 서비스로 전달되는 상품 취소 이벤트.
 *
 * <p>상품이 취소되면 해당 상품의 모든 예매를 취소 처리해야 한다.
 *
 * <p>이벤트 정보:
 *
 * <ul>
 *   <li>Aggregate Type: Product
 *   <li>Routing Key: product.cancelled.reservation
 *   <li>대상 서비스: reservation-service
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductCancelledToReservationSeatEvent
 * @see ProductCancelledToTicketEvent
 */
@Getter
public class ProductCancelledToReservationEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "Product";
  private static final String ROUTING_KEY = "product.cancelled.reservation";

  /** 취소된 상품 ID */
  private final Long productId;

  /**
   * 이벤트 발행용 생성자.
   *
   * <p>새 이벤트 생성 시 사용된다. eventId, eventType, occurredAt, version이 자동 생성된다.
   *
   * @param productId 취소된 상품 ID
   */
  public ProductCancelledToReservationEvent(Long productId) {
    super();
    this.productId = productId;
  }

  /**
   * JSON 역직렬화용 생성자.
   *
   * <p>메시지 수신 시 JSON에서 객체로 복원할 때 사용된다.
   *
   * @param eventId 이벤트 ID
   * @param occurredAt 이벤트 발생 시간
   * @param version 이벤트 버전
   * @param productId 취소된 상품 ID
   */
  @JsonCreator
  public ProductCancelledToReservationEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("productId") Long productId) {
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
