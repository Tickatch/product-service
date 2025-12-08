package com.tickatch.product_service.product.infrastructure.messaging.reservationseat.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import lombok.Getter;

/**
 * 좌석 예약 이벤트.
 *
 * <p>ReservationSeat 서비스에서 좌석이 예약되면 발행된다. Product 서비스는 이를 수신하여 좌석 현황과 통계를 갱신한다.
 *
 * <p>이벤트 정보:
 * <ul>
 *   <li>Aggregate Type: ReservationSeat
 *   <li>Routing Key: seat.reserved
 *   <li>발행 서비스: reservation-seat-service
 *   <li>수신 서비스: product-service
 * </ul>
 *
 * <p>처리 내용:
 * <ul>
 *   <li>SeatSummary.availableSeats 감소
 *   <li>ProductStats.reservationCount 증가
 *   <li>(2차 확장) SeatGrade별 availableSeats 감소
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see DomainEvent
 */
@Getter
public class SeatReservedEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "ReservationSeat";
  private static final String ROUTING_KEY = "seat.reserved";

  /** 상품 ID */
  private final Long productId;

  /** 좌석 등급 (VIP, R, S 등) - 2차 확장 대비 */
  private final String grade;

  /** 예약된 좌석 수 */
  private final Integer count;

  /**
   * 이벤트 발행용 생성자.
   *
   * <p>새 이벤트 생성 시 사용한다. 부모 클래스에서 eventId, eventType, occurredAt, version이 자동 생성된다.
   *
   * @param productId 상품 ID
   * @param grade 좌석 등급
   * @param count 예약된 좌석 수
   */
  public SeatReservedEvent(Long productId, String grade, Integer count) {
    super();
    this.productId = productId;
    this.grade = grade;
    this.count = count != null ? count : 1;
  }

  /**
   * JSON 역직렬화용 생성자.
   *
   * <p>메시지 수신 시 JSON에서 객체로 복원할 때 사용한다. 부모 클래스의 메타데이터도 함께 복원된다.
   *
   * @param eventId 이벤트 고유 ID
   * @param occurredAt 이벤트 발생 시간
   * @param version 이벤트 스키마 버전
   * @param productId 상품 ID
   * @param grade 좌석 등급
   * @param count 예약된 좌석 수
   */
  @JsonCreator
  public SeatReservedEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("productId") Long productId,
      @JsonProperty("grade") String grade,
      @JsonProperty("count") Integer count) {
    super(eventId, occurredAt, version);
    this.productId = productId;
    this.grade = grade;
    this.count = count != null ? count : 1;
  }

  /**
   * Aggregate ID를 반환한다.
   *
   * @return Product ID 문자열
   */
  @Override
  public String getAggregateId() {
    return String.valueOf(productId);
  }

  /**
   * Aggregate 타입을 반환한다.
   *
   * @return "ReservationSeat"
   */
  @Override
  public String getAggregateType() {
    return AGGREGATE_TYPE;
  }

  /**
   * RabbitMQ 라우팅 키를 반환한다.
   *
   * @return "seat.reserved"
   */
  @Override
  public String getRoutingKey() {
    return ROUTING_KEY;
  }
}