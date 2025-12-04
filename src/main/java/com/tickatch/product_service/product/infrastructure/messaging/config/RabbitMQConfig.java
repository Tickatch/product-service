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
 * <p>Product 서비스에서 발행하는 이벤트를 위한 Exchange, Queue, Binding을 정의한다. MessageConverter는 공통 라이브러리의
 * JsonUtils.getObjectMapper()를 사용하여 전체 프로젝트에서 일관된 JSON 직렬화/역직렬화를 보장한다.
 */
@Configuration
public class RabbitMQConfig {

  @Value("${messaging.exchange.product:tickatch.product}")
  private String productExchange;

  public static final String QUEUE_PRODUCT_CANCELLED_RESERVATION_SEAT =
      "tickatch.product.cancelled.reservation-seat.queue";
  public static final String QUEUE_PRODUCT_CANCELLED_RESERVATION =
      "tickatch.product.cancelled.reservation.queue";
  public static final String QUEUE_PRODUCT_CANCELLED_TICKET =
      "tickatch.product.cancelled.ticket.queue";

  public static final String ROUTING_KEY_CANCELLED_RESERVATION_SEAT =
      "product.cancelled.reservation-seat";
  public static final String ROUTING_KEY_CANCELLED_RESERVATION = "product.cancelled.reservation";
  public static final String ROUTING_KEY_CANCELLED_TICKET = "product.cancelled.ticket";

  @Bean
  public TopicExchange productExchange() {
    return ExchangeBuilder.topicExchange(productExchange).durable(true).build();
  }

  @Bean
  public Queue productCancelledReservationSeatQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION_SEAT)
        .withArgument("x-dead-letter-exchange", productExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_CANCELLED_RESERVATION_SEAT)
        .build();
  }

  @Bean
  public Queue productCancelledReservationQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION)
        .withArgument("x-dead-letter-exchange", productExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_CANCELLED_RESERVATION)
        .build();
  }

  @Bean
  public Queue productCancelledTicketQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_TICKET)
        .withArgument("x-dead-letter-exchange", productExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", "dlq." + ROUTING_KEY_CANCELLED_TICKET)
        .build();
  }

  @Bean
  public Binding productCancelledReservationSeatBinding(
      Queue productCancelledReservationSeatQueue, TopicExchange productExchange) {
    return BindingBuilder.bind(productCancelledReservationSeatQueue)
        .to(productExchange)
        .with(ROUTING_KEY_CANCELLED_RESERVATION_SEAT);
  }

  @Bean
  public Binding productCancelledReservationBinding(
      Queue productCancelledReservationQueue, TopicExchange productExchange) {
    return BindingBuilder.bind(productCancelledReservationQueue)
        .to(productExchange)
        .with(ROUTING_KEY_CANCELLED_RESERVATION);
  }

  @Bean
  public Binding productCancelledTicketBinding(
      Queue productCancelledTicketQueue, TopicExchange productExchange) {
    return BindingBuilder.bind(productCancelledTicketQueue)
        .to(productExchange)
        .with(ROUTING_KEY_CANCELLED_TICKET);
  }

  @Bean
  public TopicExchange deadLetterExchange() {
    return ExchangeBuilder.topicExchange(productExchange + ".dlx").durable(true).build();
  }

  @Bean
  public Queue deadLetterReservationSeatQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION_SEAT + ".dlq").build();
  }

  @Bean
  public Queue deadLetterReservationQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_RESERVATION + ".dlq").build();
  }

  @Bean
  public Queue deadLetterTicketQueue() {
    return QueueBuilder.durable(QUEUE_PRODUCT_CANCELLED_TICKET + ".dlq").build();
  }

  @Bean
  public Binding deadLetterReservationSeatBinding(
      Queue deadLetterReservationSeatQueue, TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterReservationSeatQueue)
        .to(deadLetterExchange)
        .with("dlq." + ROUTING_KEY_CANCELLED_RESERVATION_SEAT);
  }

  @Bean
  public Binding deadLetterReservationBinding(
      Queue deadLetterReservationQueue, TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterReservationQueue)
        .to(deadLetterExchange)
        .with("dlq." + ROUTING_KEY_CANCELLED_RESERVATION);
  }

  @Bean
  public Binding deadLetterTicketBinding(
      Queue deadLetterTicketQueue, TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterTicketQueue)
        .to(deadLetterExchange)
        .with("dlq." + ROUTING_KEY_CANCELLED_TICKET);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter(JsonUtils.getObjectMapper());
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter);
    return rabbitTemplate;
  }
}
