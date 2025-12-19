package com.tickatch.product_service.product.application.messaging.event;

/**
 * 상품 로그 액션 타입.
 *
 * <p>상품 도메인에서 발생할 수 있는 모든 액션 타입을 정의한다.
 * 로그 서비스에서 이 값을 기준으로 액션을 분류하고 저장한다.
 *
 * <p>카테고리별 액션:
 *
 * <ul>
 *   <li>생성/수정: CREATED, UPDATED, CREATE_FAILED, UPDATE_FAILED
 *   <li>심사: SUBMITTED_FOR_APPROVAL, APPROVED, REJECTED, RESUBMITTED 및 각 FAILED
 *   <li>상태 변경: SCHEDULED, SALE_STARTED, SALE_CLOSED, COMPLETED, CANCELLED 및 각 FAILED
 *   <li>좌석 관리: SEATS_DECREASED, SEATS_INCREASED, SEAT_GRADE_DECREASED, SEAT_GRADE_INCREASED, SEAT_OPERATION_FAILED
 *   <li>통계: VIEW_COUNT_SYNCED, RESERVATION_COUNT_INCREASED, RESERVATION_COUNT_DECREASED 및 각 FAILED
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductLogEvent
 */
public final class ProductActionType {

  private ProductActionType() {
    // 인스턴스화 방지
  }

  // ========================================
  // 생성/수정 관련
  // ========================================

  /** 상품 생성 */
  public static final String CREATED = "CREATED";

  /** 상품 생성 실패 */
  public static final String CREATE_FAILED = "CREATE_FAILED";

  /** 상품 수정 */
  public static final String UPDATED = "UPDATED";

  /** 상품 수정 실패 */
  public static final String UPDATE_FAILED = "UPDATE_FAILED";

  // ========================================
  // 심사 관련
  // ========================================

  /** 심사 요청 */
  public static final String SUBMITTED_FOR_APPROVAL = "SUBMITTED_FOR_APPROVAL";

  /** 심사 요청 실패 */
  public static final String SUBMIT_FOR_APPROVAL_FAILED = "SUBMIT_FOR_APPROVAL_FAILED";

  /** 승인 */
  public static final String APPROVED = "APPROVED";

  /** 승인 실패 */
  public static final String APPROVE_FAILED = "APPROVE_FAILED";

  /** 반려 */
  public static final String REJECTED = "REJECTED";

  /** 반려 실패 */
  public static final String REJECT_FAILED = "REJECT_FAILED";

  /** 재제출 */
  public static final String RESUBMITTED = "RESUBMITTED";

  /** 재제출 실패 */
  public static final String RESUBMIT_FAILED = "RESUBMIT_FAILED";

  // ========================================
  // 상태 변경 관련
  // ========================================

  /** 판매 예정 상태로 변경 */
  public static final String SCHEDULED = "SCHEDULED";

  /** 판매 예정 상태 변경 실패 */
  public static final String SCHEDULE_FAILED = "SCHEDULE_FAILED";

  /** 판매 시작 */
  public static final String SALE_STARTED = "SALE_STARTED";

  /** 판매 시작 실패 */
  public static final String SALE_START_FAILED = "SALE_START_FAILED";

  /** 판매 종료 */
  public static final String SALE_CLOSED = "SALE_CLOSED";

  /** 판매 종료 실패 */
  public static final String SALE_CLOSE_FAILED = "SALE_CLOSE_FAILED";

  /** 완료 */
  public static final String COMPLETED = "COMPLETED";

  /** 완료 처리 실패 */
  public static final String COMPLETE_FAILED = "COMPLETE_FAILED";

  /** 취소 */
  public static final String CANCELLED = "CANCELLED";

  /** 취소 실패 */
  public static final String CANCEL_FAILED = "CANCEL_FAILED";

  // ========================================
  // 좌석 관리 관련
  // ========================================

  /** 잔여 좌석 차감 (총합) */
  public static final String SEATS_DECREASED = "SEATS_DECREASED";

  /** 잔여 좌석 복구 (총합) */
  public static final String SEATS_INCREASED = "SEATS_INCREASED";

  /** 등급별 잔여 좌석 차감 */
  public static final String SEAT_GRADE_DECREASED = "SEAT_GRADE_DECREASED";

  /** 등급별 잔여 좌석 복구 */
  public static final String SEAT_GRADE_INCREASED = "SEAT_GRADE_INCREASED";

  /** 좌석 작업 실패 */
  public static final String SEAT_OPERATION_FAILED = "SEAT_OPERATION_FAILED";

  // ========================================
  // 통계 관련
  // ========================================

  /** 조회수 동기화 */
  public static final String VIEW_COUNT_SYNCED = "VIEW_COUNT_SYNCED";

  /** 조회수 동기화 실패 */
  public static final String VIEW_COUNT_SYNC_FAILED = "VIEW_COUNT_SYNC_FAILED";

  /** 예매 수 증가 */
  public static final String RESERVATION_COUNT_INCREASED = "RESERVATION_COUNT_INCREASED";

  /** 예매 수 감소 */
  public static final String RESERVATION_COUNT_DECREASED = "RESERVATION_COUNT_DECREASED";

  /** 예매 수 변경 실패 */
  public static final String RESERVATION_COUNT_CHANGE_FAILED = "RESERVATION_COUNT_CHANGE_FAILED";
}