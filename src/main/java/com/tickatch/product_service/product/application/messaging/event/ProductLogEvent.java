package com.tickatch.product_service.product.application.messaging.event;

import com.tickatch.product_service.global.config.ActorExtractor;
import com.tickatch.product_service.global.config.ActorExtractor.ActorInfo;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상품 로그 이벤트.
 *
 * <p>상품 도메인에서 발생하는 주요 액션에 대한 로그 정보를 담는 이벤트 객체이다.
 * 로그 서비스로 전송되어 상품 관련 활동 이력을 기록한다.
 *
 * <p>이벤트 정보:
 *
 * <ul>
 *   <li>Exchange: tickatch.log
 *   <li>Routing Key: product.log
 *   <li>대상 서비스: log-service
 * </ul>
 *
 * <p>ActorInfo는 {@link ActorExtractor}를 통해 SecurityContext에서 자동 추출된다.
 *
 * @param eventId 이벤트 고유 ID (발행 서비스에서 생성)
 * @param productId 대상 상품 ID
 * @param actionType 액션 타입 ({@link ProductActionType} 참조)
 * @param actorType 액터 타입 (ADMIN, SELLER, CUSTOMER, SYSTEM 등)
 * @param actorUserId 액터 사용자 ID (SYSTEM인 경우 null)
 * @param occurredAt 이벤트 발생 시간
 * @author Tickatch
 * @since 1.0.0
 * @see ProductLogEventPublisher
 * @see ActorExtractor
 */
public record ProductLogEvent(
    UUID eventId,
    Long productId,
    String actionType,
    String actorType,
    UUID actorUserId,
    LocalDateTime occurredAt) {

  /**
   * 새로운 상품 로그 이벤트를 생성한다.
   *
   * <p>eventId와 occurredAt은 자동으로 생성되고, actorType과 actorUserId는
   * {@link ActorExtractor}를 통해 SecurityContext에서 자동 추출된다.
   *
   * @param productId 대상 상품 ID
   * @param actionType 액션 타입
   * @return 생성된 ProductLogEvent
   */
  public static ProductLogEvent create(Long productId, String actionType) {
    ActorInfo actorInfo = ActorExtractor.extract();
    return new ProductLogEvent(
        UUID.randomUUID(),
        productId,
        actionType,
        actorInfo.actorType(),
        actorInfo.actorUserId(),
        LocalDateTime.now());
  }

  /**
   * 시스템에 의한 상품 로그 이벤트를 생성한다.
   *
   * <p>스케줄러나 내부 시스템 작업 등 사용자 컨텍스트가 없는 경우 사용한다.
   * actorType은 "SYSTEM"으로, actorUserId는 null로 설정된다.
   *
   * @param productId 대상 상품 ID
   * @param actionType 액션 타입
   * @return 생성된 ProductLogEvent
   */
  public static ProductLogEvent createSystemEvent(Long productId, String actionType) {
    return new ProductLogEvent(
        UUID.randomUUID(),
        productId,
        actionType,
        "SYSTEM",
        null,
        LocalDateTime.now());
  }
}