package com.tickatch.product_service.product.infrastructure.messaging.config;

import io.github.tickatch.common.util.JsonUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정.
 *
 * <p>Product 서비스에서 발행하는 이벤트를 위한 Exchange, Queue, Binding을 정의한다. MessageConverter는 공통 라이브러리의 {@link
 * JsonUtils#getObjectMapper()}를 사용하여 전체 프로젝트에서 일관된 JSON 직렬화/역직렬화를 보장한다.
 *
 * <p>구성 요소:
 *
 * <ul>
 *   <li>Exchange: tickatch.product (Topic)
 *   <li>Queue: 서비스별 취소 이벤트 큐 2개 (ReservationSeat, Reservation)
 *   <li>DLQ: 각 큐별 Dead Letter Queue
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see JsonUtils
 */
@Configuration
public class RabbitMQConfig {

  @Value("${messaging.exchange.product:tickatch.product}")
  private String productExchange;

  @Value("${messaging.exchange.arthall:tickatch.arthall}")
  private String arthallExchange;

  @Value("${messaging.exchange.reservation-seat:tickatch.reservation-seat}")
  private String reservationSeatExchange;

  // ========================================
  // Queue Names - Product 발행용
  // ========================================

  /** ReservationSeat 서비스용 취소 이벤트 큐 이름 */
  public static final String QUEUE_PRODUCT_CANCELLED_RESERVATION_SEAT =
      "tickatch.product.cancelled.reservation-seat.queue";

  /** Reservation 서비스용 취소 이벤트 큐 이름 */
  public static final String QUEUE_PRODUCT_CANCELLED_RESERVATION =
      "tickatch.product.cancelled.reservation.queue";

  // ========================================
  // Routing Keys - Product 발행용
  // ========================================

  /** ReservationSeat 서비스용 라우팅 키 */
  public static final String ROUTING_KEY_CANCELLED_RESERVATION_SEAT =
      "product.cancelled.reservation-seat";

  /** Reservation 서비스용 라우팅 키 */
  public static final String ROUTING_KEY_CANCELLED_RESERVATION = "product.cancelled.reservation";

  // ========================================
  // Queue Names - Product 수신용
  // ========================================

  /** 좌석 예약 이벤트 수신 큐 (from ReservationSeat) */
  public static final String QUEUE_SEAT_RESERVED_PRODUCT =
      "tickatch.seat.reserved.product.queue";

  /** 좌석 해제 이벤트 수신 큐 (from ReservationSeat) */
  public static final String QUEUE_SEAT_RELEASED_PRODUCT =
      "tickatch.seat.released.product.queue";

  // ========================================
  // Routing Keys - Product 수신용
  // ========================================

  /** 좌석 예약 이벤트 라우팅 키 */
  public static final String ROUTING_KEY_SEAT_RESERVED = "seat.reserved";

  /** 좌석 해제 이벤트 라우팅 키 */
  public static final String ROUTING_KEY_SEAT_RELEASED = "seat.released";

  // ========================================
  // Exchange - Product 발행용
  // ========================================

  /**
   * Product 도메인 이벤트용 Topic Exchange를 생성한다.
   *
   * @return durable Topic Exchange
   */
  @Bean
  public TopicExchange productExchange() {
    return ExchangeBuilder.topicExchange(productExchange).durable(true).build();
  }

  // ========================================
  // Exchange - Product 수신용
  // ========================================

  /**
   * ReservationSeat 도메인 이벤트용 Topic Exchange를 생성한다.
   *
   * <p>ReservationSeat 서비스에서 생성하지만, Product 서비스에서도 바인딩을 위해 선언한다.
   *
   * @return durable Topic Exchange
   */
  @Bean
  public TopicExchange reservationSeatExchange() {
    return ExchangeBuilder.topicExchange(reservationSeatExchange).durable(true).build();
  }

  // ========================================
  // Queues - Product 발행용
  // ========================================

  /**
   * ReservationSeat 서비스용 취소 이벤트 큐를 생성한다.
   *
   * <p>메시지 처리 실패 시 DLQ로 이동한다.
   *
   * @return DLQ 설정이 포함된 durable Queue
   */
  @Bean
  public Queue productCancelledReservationSeatQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION_SEAT)
        .withArgument("x-dead-letter-exchange", productExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_CANCELLED_RESERVATION_SEAT)
        .build();
  }

  /**
   * Reservation 서비스용 취소 이벤트 큐를 생성한다.
   *
   * <p>메시지 처리 실패 시 DLQ로 이동한다.
   *
   * @return DLQ 설정이 포함된 durable Queue
   */
  @Bean
  public Queue productCancelledReservationQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION)
        .withArgument("x-dead-letter-exchange", productExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_CANCELLED_RESERVATION)
        .build();
  }

  // ========================================
  // Queues - Product 수신용
  // ========================================

  /**
   * 좌석 예약 이벤트 수신 큐를 생성한다.
   *
   * <p>ReservationSeat 서비스에서 좌석이 예약되면 이 큐로 메시지가 전달된다.
   * 메시지 처리 실패 시 DLQ로 이동한다.
   *
   * @return DLQ 설정이 포함된 durable Queue
   */
  @Bean
  public Queue seatReservedProductQueue() {
    return QueueBuilder.durable(QUEUE_SEAT_RESERVED_PRODUCT)
        .withArgument("x-dead-letter-exchange", reservationSeatExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_SEAT_RESERVED + ".product")
        .build();
  }

  /**
   * 좌석 해제 이벤트 수신 큐를 생성한다.
   *
   * <p>ReservationSeat 서비스에서 좌석이 해제되면 이 큐로 메시지가 전달된다.
   * 메시지 처리 실패 시 DLQ로 이동한다.
   *
   * @return DLQ 설정이 포함된 durable Queue
   */
  @Bean
  public Queue seatReleasedProductQueue() {
    return QueueBuilder.durable(QUEUE_SEAT_RELEASED_PRODUCT)
        .withArgument("x-dead-letter-exchange", reservationSeatExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_SEAT_RELEASED + ".product")
        .build();
  }

  // ========================================
  // Bindings - Product 발행용
  // ========================================

  /**
   * ReservationSeat 큐와 Exchange를 바인딩한다.
   *
   * @param productCancelledReservationSeatQueue 바인딩할 큐
   * @param productExchange 바인딩할 Exchange
   * @return 라우팅 키로 연결된 Binding
   */
  @Bean
  public Binding productCancelledReservationSeatBinding(
      Queue productCancelledReservationSeatQueue, TopicExchange productExchange) {
    return BindingBuilder.bind(productCancelledReservationSeatQueue)
        .to(productExchange)
        .with(ROUTING_KEY_CANCELLED_RESERVATION_SEAT);
  }

  /**
   * Reservation 큐와 Exchange를 바인딩한다.
   *
   * @param productCancelledReservationQueue 바인딩할 큐
   * @param productExchange 바인딩할 Exchange
   * @return 라우팅 키로 연결된 Binding
   */
  @Bean
  public Binding productCancelledReservationBinding(
      Queue productCancelledReservationQueue, TopicExchange productExchange) {
    return BindingBuilder.bind(productCancelledReservationQueue)
        .to(productExchange)
        .with(ROUTING_KEY_CANCELLED_RESERVATION);
  }

  // ========================================
  // Bindings - Product 수신용
  // ========================================

  /**
   * 좌석 예약 이벤트 바인딩.
   *
   * <p>ReservationSeat Exchange의 seat.reserved 라우팅 키를 Product 큐에 바인딩한다.
   *
   * @param seatReservedProductQueue 바인딩할 큐
   * @param reservationSeatExchange 바인딩할 Exchange
   * @return 라우팅 키로 연결된 Binding
   */
  @Bean
  public Binding seatReservedProductBinding(
      Queue seatReservedProductQueue, TopicExchange reservationSeatExchange) {
    return BindingBuilder.bind(seatReservedProductQueue)
        .to(reservationSeatExchange)
        .with(ROUTING_KEY_SEAT_RESERVED);
  }

  /**
   * 좌석 해제 이벤트 바인딩.
   *
   * <p>ReservationSeat Exchange의 seat.released 라우팅 키를 Product 큐에 바인딩한다.
   *
   * @param seatReleasedProductQueue 바인딩할 큐
   * @param reservationSeatExchange 바인딩할 Exchange
   * @return 라우팅 키로 연결된 Binding
   */
  @Bean
  public Binding seatReleasedProductBinding(
      Queue seatReleasedProductQueue, TopicExchange reservationSeatExchange) {
    return BindingBuilder.bind(seatReleasedProductQueue)
        .to(reservationSeatExchange)
        .with(ROUTING_KEY_SEAT_RELEASED);
  }

  // ========================================
  // Dead Letter Exchange & Queues - Product 발행용
  // ========================================

  /**
   * Dead Letter Exchange를 생성한다.
   *
   * <p>메시지 처리 실패 시 해당 Exchange로 라우팅된다.
   *
   * @return DLX용 Topic Exchange
   */
  @Bean
  public TopicExchange deadLetterExchange() {
    return ExchangeBuilder.topicExchange(productExchange + ".dlx").durable(true).build();
  }

  /**
   * ReservationSeat 서비스용 Dead Letter Queue를 생성한다.
   *
   * @return durable DLQ
   */
  @Bean
  public Queue deadLetterReservationSeatQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION_SEAT + ".dlq").build();
  }

  /**
   * Reservation 서비스용 Dead Letter Queue를 생성한다.
   *
   * @return durable DLQ
   */
  @Bean
  public Queue deadLetterReservationQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION + ".dlq").build();
  }

  /**
   * ReservationSeat DLQ와 DLX를 바인딩한다.
   *
   * @param deadLetterReservationSeatQueue 바인딩할 DLQ
   * @param deadLetterExchange 바인딩할 DLX
   * @return DLQ Binding
   */
  @Bean
  public Binding deadLetterReservationSeatBinding(
      Queue deadLetterReservationSeatQueue, TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterReservationSeatQueue)
        .to(deadLetterExchange)
        .with("dlq." + ROUTING_KEY_CANCELLED_RESERVATION_SEAT);
  }

  /**
   * Reservation DLQ와 DLX를 바인딩한다.
   *
   * @param deadLetterReservationQueue 바인딩할 DLQ
   * @param deadLetterExchange 바인딩할 DLX
   * @return DLQ Binding
   */
  @Bean
  public Binding deadLetterReservationBinding(
      Queue deadLetterReservationQueue, TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterReservationQueue)
        .to(deadLetterExchange)
        .with("dlq." + ROUTING_KEY_CANCELLED_RESERVATION);
  }

  // ========================================
  // Dead Letter Exchange & Queues - Product 수신용
  // ========================================

  /**
   * ReservationSeat Dead Letter Exchange를 생성한다.
   *
   * <p>ReservationSeat 서비스에서 생성하지만, Product 서비스에서도 DLQ 바인딩을 위해 선언한다.
   *
   * @return DLX용 Topic Exchange
   */
  @Bean
  public TopicExchange reservationSeatDeadLetterExchange() {
    return ExchangeBuilder.topicExchange(reservationSeatExchange + ".dlx").durable(true).build();
  }

  /**
   * 좌석 예약 이벤트 수신용 Dead Letter Queue를 생성한다.
   *
   * @return durable DLQ
   */
  @Bean
  public Queue seatReservedProductDlq() {
    return QueueBuilder.durable(QUEUE_SEAT_RESERVED_PRODUCT + ".dlq").build();
  }

  /**
   * 좌석 해제 이벤트 수신용 Dead Letter Queue를 생성한다.
   *
   * @return durable DLQ
   */
  @Bean
  public Queue seatReleasedProductDlq() {
    return QueueBuilder.durable(QUEUE_SEAT_RELEASED_PRODUCT + ".dlq").build();
  }

  /**
   * 좌석 예약 이벤트 DLQ와 ReservationSeat DLX를 바인딩한다.
   *
   * @param seatReservedProductDlq 바인딩할 DLQ
   * @param reservationSeatDeadLetterExchange 바인딩할 DLX
   * @return DLQ Binding
   */
  @Bean
  public Binding seatReservedProductDlqBinding(
      Queue seatReservedProductDlq, TopicExchange reservationSeatDeadLetterExchange) {
    return BindingBuilder.bind(seatReservedProductDlq)
        .to(reservationSeatDeadLetterExchange)
        .with("dlq." + ROUTING_KEY_SEAT_RESERVED + ".product");
  }

  /**
   * 좌석 해제 이벤트 DLQ와 ReservationSeat DLX를 바인딩한다.
   *
   * @param seatReleasedProductDlq 바인딩할 DLQ
   * @param reservationSeatDeadLetterExchange 바인딩할 DLX
   * @return DLQ Binding
   */
  @Bean
  public Binding seatReleasedProductDlqBinding(
      Queue seatReleasedProductDlq, TopicExchange reservationSeatDeadLetterExchange) {
    return BindingBuilder.bind(seatReleasedProductDlq)
        .to(reservationSeatDeadLetterExchange)
        .with("dlq." + ROUTING_KEY_SEAT_RELEASED + ".product");
  }

  // ========================================
  // Message Converter & Template
  // ========================================

  /**
   * JSON 메시지 변환기를 생성한다.
   *
   * <p>공통 라이브러리의 ObjectMapper를 사용하여 일관된 직렬화/역직렬화를 보장한다.
   *
   * @return Jackson 기반 MessageConverter
   * @see JsonUtils#getObjectMapper()
   */
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter(JsonUtils.getObjectMapper());
  }

  /**
   * RabbitTemplate을 생성한다.
   *
   * <p>JSON MessageConverter가 적용된 템플릿을 반환한다.
   *
   * @param connectionFactory RabbitMQ 연결 팩토리
   * @param jsonMessageConverter JSON 메시지 변환기
   * @return 설정된 RabbitTemplate
   */
  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter);
    return rabbitTemplate;
  }
}