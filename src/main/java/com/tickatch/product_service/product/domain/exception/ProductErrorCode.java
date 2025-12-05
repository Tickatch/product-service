package com.tickatch.product_service.product.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** Product 전용 에러코드 */
@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

  // 조회
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "PRODUCT_NOT_FOUND"),

  // 검증
  INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST.value(), "INVALID_PRODUCT_NAME"),
  INVALID_RUNNING_TIME(HttpStatus.BAD_REQUEST.value(), "INVALID_RUNNING_TIME"),
  INVALID_PRODUCT_TYPE(HttpStatus.BAD_REQUEST.value(), "INVALID_PRODUCT_TYPE"),
  INVALID_SCHEDULE(HttpStatus.BAD_REQUEST.value(), "INVALID_SCHEDULE"),
  INVALID_STAGE_ID(HttpStatus.BAD_REQUEST.value(), "INVALID_STAGE_ID"),
  INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST.value(), "INVALID_PRODUCT_STATUS"),
  INVALID_SALE_SCHEDULE(HttpStatus.BAD_REQUEST.value(), "INVALID_SALE_SCHEDULE"),
  INVALID_VENUE(HttpStatus.BAD_REQUEST.value(), "INVALID_VENUE"),
  INVALID_SELLER_ID(HttpStatus.BAD_REQUEST.value(), "INVALID_SELLER_ID"),
  INVALID_REJECTION_REASON(HttpStatus.BAD_REQUEST.value(), "INVALID_REJECTION_REASON"),

  // 비즈니스 규칙
  STAGE_CHANGE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY.value(), "STAGE_CHANGE_NOT_ALLOWED"),
  VENUE_CHANGE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY.value(), "VENUE_CHANGE_NOT_ALLOWED"),
  PRODUCT_ALREADY_CANCELLED(HttpStatus.UNPROCESSABLE_ENTITY.value(), "PRODUCT_ALREADY_CANCELLED"),
  PRODUCT_STATUS_CHANGE_NOT_ALLOWED(
      HttpStatus.UNPROCESSABLE_ENTITY.value(), "PRODUCT_STATUS_CHANGE_NOT_ALLOWED"),
  PRODUCT_NOT_PENDING(HttpStatus.UNPROCESSABLE_ENTITY.value(), "PRODUCT_NOT_PENDING"),
  PRODUCT_NOT_REJECTED(HttpStatus.UNPROCESSABLE_ENTITY.value(), "PRODUCT_NOT_REJECTED"),
  PRODUCT_NOT_OWNED(HttpStatus.FORBIDDEN.value(), "PRODUCT_NOT_OWNED"),

  // 이벤트 발행
  EVENT_PUBLISH_FAILED(HttpStatus.SERVICE_UNAVAILABLE.value(), "EVENT_PUBLISH_FAILED");

  private final int status;
  private final String code;
}