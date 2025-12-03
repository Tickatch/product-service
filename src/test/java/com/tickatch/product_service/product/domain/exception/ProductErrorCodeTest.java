package com.tickatch.product_service.product.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductErrorCode 테스트")
class ProductErrorCodeTest {

  @Nested
  class ErrorCode_인터페이스_구현_테스트 {

    @ParameterizedTest
    @EnumSource(ProductErrorCode.class)
    void 모든_에러코드는_ErrorCode_인터페이스를_구현한다(ProductErrorCode errorCode) {
      assertThat(errorCode).isInstanceOf(ErrorCode.class);
    }

    @ParameterizedTest
    @EnumSource(ProductErrorCode.class)
    void 모든_에러코드는_유효한_HTTP_상태코드를_가진다(ProductErrorCode errorCode) {
      assertThat(errorCode.getStatus())
          .isGreaterThanOrEqualTo(400)
          .isLessThan(600);
    }

    @ParameterizedTest
    @EnumSource(ProductErrorCode.class)
    void 모든_에러코드는_null이_아닌_코드_문자열을_가진다(ProductErrorCode errorCode) {
      assertThat(errorCode.getCode())
          .isNotNull()
          .isNotBlank();
    }
  }

  @Nested
  class 조회_에러_404_테스트 {

    @Test
    void PRODUCT_NOT_FOUND는_404_상태코드를_가진다() {
      assertThat(ProductErrorCode.PRODUCT_NOT_FOUND.getStatus()).isEqualTo(404);
      assertThat(ProductErrorCode.PRODUCT_NOT_FOUND.getCode()).isEqualTo("PRODUCT_NOT_FOUND");
    }
  }

  @Nested
  class 검증_에러_400_테스트 {

    @Test
    void INVALID_PRODUCT_NAME은_400_상태코드를_가진다() {
      assertThat(ProductErrorCode.INVALID_PRODUCT_NAME.getStatus()).isEqualTo(400);
    }

    @Test
    void INVALID_RUNNING_TIME은_400_상태코드를_가진다() {
      assertThat(ProductErrorCode.INVALID_RUNNING_TIME.getStatus()).isEqualTo(400);
    }

    @Test
    void INVALID_SCHEDULE은_400_상태코드를_가진다() {
      assertThat(ProductErrorCode.INVALID_SCHEDULE.getStatus()).isEqualTo(400);
    }
  }

  @Nested
  class 비즈니스_규칙_에러_422_테스트 {

    @Test
    void STAGE_CHANGE_NOT_ALLOWED는_422_상태코드를_가진다() {
      assertThat(ProductErrorCode.STAGE_CHANGE_NOT_ALLOWED.getStatus()).isEqualTo(422);
    }

    @Test
    void PRODUCT_ALREADY_CANCELLED는_422_상태코드를_가진다() {
      assertThat(ProductErrorCode.PRODUCT_ALREADY_CANCELLED.getStatus()).isEqualTo(422);
    }

    @Test
    void PRODUCT_STATUS_CHANGE_NOT_ALLOWED는_422_상태코드를_가진다() {
      assertThat(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED.getStatus()).isEqualTo(422);
    }

  }
}