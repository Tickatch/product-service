package com.tickatch.product_service.product.application.messaging;

import com.tickatch.product_service.product.application.messaging.event.ProductLogEvent;

/**
 * 상품 로그 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다. 이를 통해 Application은 메시징 기술(RabbitMQ, Kafka
 * 등)에 의존하지 않는다.
 *
 * <p>DIP(의존성 역전 원칙)를 적용하여 도메인 로직과 인프라스트럭처를 분리한다.
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * // 성공 로그 발행 (ActorInfo는 SecurityContext에서 자동 추출)
 * productLogEventPublisher.publishCreated(productId);
 *
 * // 실패 로그 발행
 * productLogEventPublisher.publishCreateFailed();
 *
 * // 시스템 이벤트 발행 (스케줄러 등)
 * productLogEventPublisher.publishScheduled(productId);
 * }</pre>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductLogEvent
 * @see
 *     com.tickatch.product_service.product.infrastructure.messaging.log.publisher.RabbitProductLogPublisher
 */
public interface ProductLogEventPublisher {

  /**
   * 상품 로그 이벤트를 발행한다.
   *
   * <p>발행된 이벤트는 로그 서비스로 전송되어 상품 활동 이력으로 기록된다. 발행 실패 시에도 비즈니스 로직에 영향을 주지 않도록 예외를 던지지 않고 로그로 기록한다.
   *
   * @param event 발행할 상품 로그 이벤트
   */
  void publish(ProductLogEvent event);

  // ========================================
  // 생성/수정 관련
  // ========================================

  /**
   * 상품 생성 성공 로그를 발행한다.
   *
   * @param productId 생성된 상품 ID
   */
  void publishCreated(Long productId);

  /**
   * 상품 생성 실패 로그를 발행한다.
   *
   * <p>productId가 아직 생성되지 않았으므로 null로 발행된다.
   */
  void publishCreateFailed();

  /**
   * 상품 수정 성공 로그를 발행한다.
   *
   * @param productId 수정된 상품 ID
   */
  void publishUpdated(Long productId);

  /**
   * 상품 수정 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishUpdateFailed(Long productId);

  // ========================================
  // 심사 관련
  // ========================================

  /**
   * 심사 요청 성공 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishSubmittedForApproval(Long productId);

  /**
   * 심사 요청 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishSubmitForApprovalFailed(Long productId);

  /**
   * 상품 승인 성공 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishApproved(Long productId);

  /**
   * 상품 승인 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishApproveFailed(Long productId);

  /**
   * 상품 반려 성공 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishRejected(Long productId);

  /**
   * 상품 반려 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishRejectFailed(Long productId);

  /**
   * 상품 재제출 성공 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishResubmitted(Long productId);

  /**
   * 상품 재제출 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishResubmitFailed(Long productId);

  // ========================================
  // 상태 변경 관련
  // ========================================

  /**
   * 판매 예정 상태 변경 성공 로그를 발행한다.
   *
   * <p>스케줄러에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishScheduled(Long productId);

  /**
   * 판매 예정 상태 변경 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishScheduleFailed(Long productId);

  /**
   * 판매 시작 성공 로그를 발행한다.
   *
   * <p>스케줄러에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishSaleStarted(Long productId);

  /**
   * 판매 시작 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishSaleStartFailed(Long productId);

  /**
   * 판매 종료 성공 로그를 발행한다.
   *
   * <p>스케줄러에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishSaleClosed(Long productId);

  /**
   * 판매 종료 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishSaleCloseFailed(Long productId);

  /**
   * 상품 완료 성공 로그를 발행한다.
   *
   * <p>스케줄러에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishCompleted(Long productId);

  /**
   * 상품 완료 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishCompleteFailed(Long productId);

  /**
   * 상품 취소 성공 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishCancelled(Long productId);

  /**
   * 상품 취소 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishCancelFailed(Long productId);

  // ========================================
  // 좌석 관리 관련
  // ========================================

  /**
   * 잔여 좌석 차감 로그를 발행한다 (총합).
   *
   * <p>시스템에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishSeatsDecreased(Long productId);

  /**
   * 잔여 좌석 복구 로그를 발행한다 (총합).
   *
   * <p>시스템에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishSeatsIncreased(Long productId);

  /**
   * 등급별 잔여 좌석 차감 로그를 발행한다.
   *
   * <p>시스템에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishSeatGradeDecreased(Long productId);

  /**
   * 등급별 잔여 좌석 복구 로그를 발행한다.
   *
   * <p>시스템에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishSeatGradeIncreased(Long productId);

  /**
   * 좌석 작업 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishSeatOperationFailed(Long productId);

  // ========================================
  // 통계 관련
  // ========================================

  /**
   * 조회수 동기화 성공 로그를 발행한다.
   *
   * <p>시스템에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishViewCountSynced(Long productId);

  /**
   * 조회수 동기화 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishViewCountSyncFailed(Long productId);

  /**
   * 예매 수 증가 로그를 발행한다.
   *
   * <p>시스템에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishReservationCountIncreased(Long productId);

  /**
   * 예매 수 감소 로그를 발행한다.
   *
   * <p>시스템에 의해 호출되므로 시스템 이벤트로 발행된다.
   *
   * @param productId 상품 ID
   */
  void publishReservationCountDecreased(Long productId);

  /**
   * 예매 수 변경 실패 로그를 발행한다.
   *
   * @param productId 상품 ID
   */
  void publishReservationCountChangeFailed(Long productId);
}
