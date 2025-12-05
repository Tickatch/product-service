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
 *   <li>Queue: 서비스별 취소 이벤트 큐 3개 (ReservationSeat, Reservation, Ticket)
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

  /** ReservationSeat 서비스용 취소 이벤트 큐 이름 */
  public static final String QUEUE_PRODUCT_CANCELLED_RESERVATION_SEAT =
      "tickatch.product.cancelled.reservation-seat.queue";

  /** Reservation 서비스용 취소 이벤트 큐 이름 */
  public static final String QUEUE_PRODUCT_CANCELLED_RESERVATION =
      "tickatch.product.cancelled.reservation.queue";

  /** ReservationSeat 서비스용 라우팅 키 */
  public static final String ROUTING_KEY_CANCELLED_RESERVATION_SEAT =
      "product.cancelled.reservation-seat";

  /** Reservation 서비스용 라우팅 키 */
  public static final String ROUTING_KEY_CANCELLED_RESERVATION = "product.cancelled.reservation";

  // ========================================
  // Exchange
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
  // Queues
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
  // Bindings
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
  // Dead Letter Exchange & Queues
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
