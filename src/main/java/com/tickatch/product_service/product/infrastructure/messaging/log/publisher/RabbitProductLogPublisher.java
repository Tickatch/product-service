package com.tickatch.product_service.product.infrastructure.messaging.log.publisher;

import com.tickatch.product_service.product.application.messaging.ProductLogEventPublisher;
import com.tickatch.product_service.product.application.messaging.event.ProductActionType;
import com.tickatch.product_service.product.application.messaging.event.ProductLogEvent;
import com.tickatch.product_service.product.infrastructure.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 기반 상품 로그 이벤트 발행자.
 *
 * <p>상품 도메인에서 발생하는 주요 액션에 대한 로그 이벤트를 RabbitMQ를 통해
 * 로그 서비스로 발행한다. 로그 발행 실패 시에도 비즈니스 로직에 영향을 주지
 * 않도록 예외를 던지지 않고 에러 로그로 기록한다.
 *
 * <p>메시징 설정:
 *
 * <ul>
 *   <li>Exchange: tickatch.log (Topic)
 *   <li>Routing Key: product.log
 *   <li>Queue: tickatch.product.log.queue
 * </ul>
 *
 * <p>발행 흐름:
 *
 * <pre>{@code
 * ProductCommandService
 *     -> ProductLogEventPublisher.publishCreated()
 *     -> RabbitProductLogPublisher.publish()
 *     -> RabbitTemplate.convertAndSend()
 *     -> RabbitMQ Exchange (tickatch.log)
 *     -> Queue (tickatch.product.log.queue)
 *     -> ProductLogConsumer (log-service)
 * }</pre>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductLogEventPublisher
 * @see ProductLogEvent
 * @see RabbitMQConfig
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitProductLogPublisher implements ProductLogEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.exchange.log:tickatch.log}")
  private String logExchange;

  // ========================================
  // 기본 발행 메서드
  // ========================================

  /**
   * {@inheritDoc}
   *
   * <p>RabbitMQ를 통해 로그 Exchange로 이벤트를 발행한다. 발행 실패 시 예외를 던지지 않고
   * 에러 로그로 기록하여 비즈니스 로직에 영향을 주지 않는다.
   */
  @Override
  public void publish(ProductLogEvent event) {
    try {
      rabbitTemplate.convertAndSend(logExchange, RabbitMQConfig.ROUTING_KEY_PRODUCT_LOG, event);
      log.debug(
          "상품 로그 이벤트 발행 완료. eventId: {}, productId: {}, actionType: {}, actorType: {}",
          event.eventId(),
          event.productId(),
          event.actionType(),
          event.actorType());
    } catch (Exception e) {
      log.error(
          "상품 로그 이벤트 발행 실패. eventId: {}, productId: {}, actionType: {}, error: {}",
          event.eventId(),
          event.productId(),
          event.actionType(),
          e.getMessage(),
          e);
    }
  }

  // ========================================
  // 생성/수정 관련
  // ========================================

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishCreated(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.CREATED);
    publish(event);
    log.info("상품 생성 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishCreateFailed() {
    ProductLogEvent event = ProductLogEvent.create(null, ProductActionType.CREATE_FAILED);
    publish(event);
    log.warn("상품 생성 실패 로그 발행.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishUpdated(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.UPDATED);
    publish(event);
    log.info("상품 수정 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishUpdateFailed(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.UPDATE_FAILED);
    publish(event);
    log.warn("상품 수정 실패 로그 발행. productId: {}", productId);
  }

  // ========================================
  // 심사 관련
  // ========================================

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishSubmittedForApproval(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.create(productId, ProductActionType.SUBMITTED_FOR_APPROVAL);
    publish(event);
    log.info("심사 요청 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishSubmitForApprovalFailed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.create(productId, ProductActionType.SUBMIT_FOR_APPROVAL_FAILED);
    publish(event);
    log.warn("심사 요청 실패 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishApproved(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.APPROVED);
    publish(event);
    log.info("상품 승인 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishApproveFailed(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.APPROVE_FAILED);
    publish(event);
    log.warn("상품 승인 실패 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishRejected(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.REJECTED);
    publish(event);
    log.info("상품 반려 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishRejectFailed(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.REJECT_FAILED);
    publish(event);
    log.warn("상품 반려 실패 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishResubmitted(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.RESUBMITTED);
    publish(event);
    log.info("상품 재제출 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishResubmitFailed(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.RESUBMIT_FAILED);
    publish(event);
    log.warn("상품 재제출 실패 로그 발행. productId: {}", productId);
  }

  // ========================================
  // 상태 변경 관련
  // ========================================

  /**
   * {@inheritDoc}
   *
   * <p>스케줄러에 의한 자동 상태 변경이므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishScheduled(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SCHEDULED);
    publish(event);
    log.info("판매 예정 상태 변경 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishScheduleFailed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SCHEDULE_FAILED);
    publish(event);
    log.warn("판매 예정 상태 변경 실패 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   *
   * <p>스케줄러에 의한 자동 상태 변경이므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishSaleStarted(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SALE_STARTED);
    publish(event);
    log.info("판매 시작 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishSaleStartFailed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SALE_START_FAILED);
    publish(event);
    log.warn("판매 시작 실패 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   *
   * <p>스케줄러에 의한 자동 상태 변경이므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishSaleClosed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SALE_CLOSED);
    publish(event);
    log.info("판매 종료 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishSaleCloseFailed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SALE_CLOSE_FAILED);
    publish(event);
    log.warn("판매 종료 실패 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   *
   * <p>스케줄러에 의한 자동 상태 변경이므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishCompleted(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.COMPLETED);
    publish(event);
    log.info("상품 완료 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishCompleteFailed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.COMPLETE_FAILED);
    publish(event);
    log.warn("상품 완료 실패 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   *
   * <p>취소 요청자 정보는 SecurityContext에서 자동으로 추출된다.
   */
  @Override
  public void publishCancelled(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.CANCELLED);
    publish(event);
    log.info("상품 취소 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishCancelFailed(Long productId) {
    ProductLogEvent event = ProductLogEvent.create(productId, ProductActionType.CANCEL_FAILED);
    publish(event);
    log.warn("상품 취소 실패 로그 발행. productId: {}", productId);
  }

  // ========================================
  // 좌석 관리 관련
  // ========================================

  /**
   * {@inheritDoc}
   *
   * <p>좌석 차감은 예매 시스템에 의해 자동으로 수행되므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishSeatsDecreased(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SEATS_DECREASED);
    publish(event);
    log.debug("잔여 좌석 차감 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   *
   * <p>좌석 복구는 예매 취소 시스템에 의해 자동으로 수행되므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishSeatsIncreased(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SEATS_INCREASED);
    publish(event);
    log.debug("잔여 좌석 복구 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishSeatGradeDecreased(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SEAT_GRADE_DECREASED);
    publish(event);
    log.debug("등급별 좌석 차감 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishSeatGradeIncreased(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SEAT_GRADE_INCREASED);
    publish(event);
    log.debug("등급별 좌석 복구 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishSeatOperationFailed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.SEAT_OPERATION_FAILED);
    publish(event);
    log.warn("좌석 작업 실패 로그 발행. productId: {}", productId);
  }

  // ========================================
  // 통계 관련
  // ========================================

  /**
   * {@inheritDoc}
   *
   * <p>조회수 동기화는 배치 시스템에 의해 수행되므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishViewCountSynced(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.VIEW_COUNT_SYNCED);
    publish(event);
    log.debug("조회수 동기화 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishViewCountSyncFailed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(productId, ProductActionType.VIEW_COUNT_SYNC_FAILED);
    publish(event);
    log.warn("조회수 동기화 실패 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   *
   * <p>예매 수 증가는 예매 시스템에 의해 수행되므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishReservationCountIncreased(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(
            productId, ProductActionType.RESERVATION_COUNT_INCREASED);
    publish(event);
    log.debug("예매 수 증가 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   *
   * <p>예매 수 감소는 예매 취소 시스템에 의해 수행되므로 시스템 이벤트로 발행한다.
   */
  @Override
  public void publishReservationCountDecreased(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(
            productId, ProductActionType.RESERVATION_COUNT_DECREASED);
    publish(event);
    log.debug("예매 수 감소 로그 발행. productId: {}", productId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishReservationCountChangeFailed(Long productId) {
    ProductLogEvent event =
        ProductLogEvent.createSystemEvent(
            productId, ProductActionType.RESERVATION_COUNT_CHANGE_FAILED);
    publish(event);
    log.warn("예매 수 변경 실패 로그 발행. productId: {}", productId);
  }
}