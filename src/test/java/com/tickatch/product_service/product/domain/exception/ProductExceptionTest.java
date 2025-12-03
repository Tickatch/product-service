package com.tickatch.product_service.product.domain.exception;

import static org.junit.jupiter.api.Assertions.*;

import io.github.tickatch.common.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductException 테스트")
class ProductExceptionTest {

  @Nested
  class 상속_관계_테스트 {

    @Test
    void ProductExceptio은_BusinessException을_상속한다() {
      ProductException exception = new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);

      assertThat(exception).isInstanceOf(BusinessException.class);
      assertThat(exception).isInstanceOf(RuntimeException.class);
    }
  }

  @Nested
  class 생성자_ErrorCode만_테스트 {

    @Test
    void ErrorCode와_단일_인자로_예외를_생성할_수_있다() {
      ProductException exception = new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);

      assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
      assertThat(exception.getStatus()).isEqualTo(404);
      assertThat(exception.getCode()).isEqualTo("PRODUCT_NOT_FOUND");
      assertThat(exception.getErrorArgs()).isEmpty();
    }
  }

  @Nested
  class 생성자_ErrorCode와_Args_테스트 {

    @Test
    void ErrorCode와_단일_인자로_예외를_생성할_수_있다() {
      Long productId = 123L;

      ProductException exception = new ProductException(
          ProductErrorCode.PRODUCT_NOT_FOUND,
          productId
      );

      assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
      assertThat(exception.getErrorArgs()).containsExactly(productId);
    }

    @Test
    void ErrorCode와_다중_인자로_예외를_생성할_수_있다() {
      String currentStatus = "DRAFT";
      String targetStatus = "SOLD_OUT";

      ProductException exception = new ProductException(
          ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED,
          currentStatus,
          targetStatus
      );

      assertThat(exception.getErrorArgs()).containsExactly(currentStatus, targetStatus);
    }
  }

  @Nested
  class 생성자_ErrorCode와_Cause_그리고_Args_테스트 {

    @Test
    void ErrorCode_원인예외_인자로_예외를_생성할_수_있다() {
      RuntimeException cause = new RuntimeException("원인 예외");
      Long productId = 456L;

      ProductException exception = new ProductException(
          ProductErrorCode.PRODUCT_NOT_FOUND,
          cause,
          productId
      );

      assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
      assertThat(exception.getCause()).isEqualTo(cause);
      assertThat(exception.getErrorArgs()).containsExactly(productId);
    }
  }

  @Nested
  class 메서드_테스트 {

    @Test
    void getStatus메서드는_ErrorCode의_status를_반환한다() {
      ProductException exception = new ProductException(ProductErrorCode.INVALID_PRODUCT_NAME);

      assertThat(exception.getStatus()).isEqualTo(400);
    }

    @Test
    void getCode메서드는_ErrorCode의_code를_반환한다() {
      ProductException exception = new ProductException(ProductErrorCode.STAGE_CHANGE_NOT_ALLOWED);

      assertThat(exception.getCode()).isEqualTo("STAGE_CHANGE_NOT_ALLOWED");
    }

    @Test
    void getMessage메서드는_ErrorCode의_code를_반환한다() {
      ProductException exception = new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);

      assertThat(exception.getMessage()).isEqualTo("PRODUCT_NOT_FOUND");
    }
  }

  @Nested
  class 실제_사용_시나리오_테스트 {

    @Test
    void 상품_조회_실패_시나리오() {
      Long productId = 999L;

      ProductException exception = new ProductException(
          ProductErrorCode.PRODUCT_NOT_FOUND,
          productId
      );

      assertThat(exception.getStatus()).isEqualTo(404);
      assertThat(exception.getCode()).isEqualTo("PRODUCT_NOT_FOUND");
      assertThat(exception.getErrorArgs()).containsExactly(999L);
    }

    @Test
    void 상태_변경_실패_시나리오() {
      ProductException exception = new ProductException(
          ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED,
          "DRAFT",
          "SOLD_OUT"
      );

      assertThat(exception.getStatus()).isEqualTo(422);
      assertThat(exception.getErrorArgs()).hasSize(2);
    }

    @Test
    void try_catch에서_타입으로_구분_가능() {
      boolean caught = false;

      try {
        throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, 1L);
      } catch (ProductException e) {
        caught = true;
        assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
      }

      assertThat(caught).isTrue();
    }
  }
}