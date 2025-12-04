package com.tickatch.product_service.product.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Product 전용 에러코드 */
@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

  // 조회
  PRODUCT_NOT_FOUND(404, "PRODUCT_NOT_FOUND"),

  // 검증
  INVALID_PRODUCT_NAME(400, "INVALID_PRODUCT_NAME"),
  INVALID_RUNNING_TIME(400, "INVALID_RUNNING_TIME"),
  INVALID_PRODUCT_TYPE(400, "INVALID_PRODUCT_TYPE"),
  INVALID_SCHEDULE(400, "INVALID_SCHEDULE"),
  INVALID_STAGE_ID(400, "INVALID_STAGE_ID"),
  INVALID_PRODUCT_STATUS(400, "INVALID_PRODUCT_STATUS"),

  // 비즈니스 규칙
  STAGE_CHANGE_NOT_ALLOWED(422, "STAGE_CHANGE_NOT_ALLOWED"),
  PRODUCT_ALREADY_CANCELLED(422, "PRODUCT_ALREADY_CANCELLED"),
  PRODUCT_STATUS_CHANGE_NOT_ALLOWED(422, "PRODUCT_STATUS_CHANGE_NOT_ALLOWED"),

  // 이벤트 발행 실패
  EVENT_PUBLISH_FAILED(503, "EVENT_PUBLISH_FAILED");

  private final int status;
  private final String code;
}
