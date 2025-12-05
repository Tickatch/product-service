package com.tickatch.product_service.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.product_service.product.domain.vo.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("ProductStatus Enum 테스트")
class ProductStatusTest {

  @Nested
  class Enum_값_테스트 {

    @Test
    void 모든_상태값이_존재한다() {
      assertThat(ProductStatus.values())
          .containsExactly(
              ProductStatus.DRAFT,
              ProductStatus.PENDING,
              ProductStatus.APPROVED,
              ProductStatus.REJECTED,
              ProductStatus.SCHEDULED,
              ProductStatus.ON_SALE,
              ProductStatus.CLOSED,
              ProductStatus.COMPLETED,
              ProductStatus.CANCELLED);
    }
  }

  @Nested
  class Description_테스트 {

    @Test
    void DRAFT의_description은_임시저장이다() {
      assertThat(ProductStatus.DRAFT.getDescription()).isEqualTo("임시저장");
    }

    @Test
    void PENDING의_description은_심사대기이다() {
      assertThat(ProductStatus.PENDING.getDescription()).isEqualTo("심사대기");
    }

    @Test
    void APPROVED의_description은_승인됨이다() {
      assertThat(ProductStatus.APPROVED.getDescription()).isEqualTo("승인됨");
    }

    @Test
    void REJECTED의_description은_반려됨이다() {
      assertThat(ProductStatus.REJECTED.getDescription()).isEqualTo("반려됨");
    }

    @Test
    void SCHEDULED의_description은_예매예정이다() {
      assertThat(ProductStatus.SCHEDULED.getDescription()).isEqualTo("예매예정");
    }

    @Test
    void ON_SALE의_description은_판매중이다() {
      assertThat(ProductStatus.ON_SALE.getDescription()).isEqualTo("판매중");
    }

    @Test
    void CLOSED의_description은_판매종료이다() {
      assertThat(ProductStatus.CLOSED.getDescription()).isEqualTo("판매종료");
    }

    @Test
    void COMPLETED의_description은_행사종료이다() {
      assertThat(ProductStatus.COMPLETED.getDescription()).isEqualTo("행사종료");
    }

    @Test
    void CANCELLED의_description은_취소됨이다() {
      assertThat(ProductStatus.CANCELLED.getDescription()).isEqualTo("취소됨");
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    void 모든_ProductStatus는_null이_아닌_description을_가진다(ProductStatus status) {
      assertThat(status.getDescription()).isNotNull().isNotBlank();
    }
  }

  @Nested
  class 상태_전이_규칙_테스트 {

    @Test
    void null로는_변경할_수_없다() {
      for (ProductStatus status : ProductStatus.values()) {
        assertThat(status.canChangeTo(null)).isFalse();
      }
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    void 동일한_상태로는_변경할_수_없다(ProductStatus status) {
      assertThat(status.canChangeTo(status)).isFalse();
    }

    @Nested
    class DRAFT_상태에서_전이_테스트 {

      @Test
      void PENDING으로_변경할_수_있다() {
        assertThat(ProductStatus.DRAFT.canChangeTo(ProductStatus.PENDING)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.DRAFT.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void APPROVED로_직접_변경할_수_없다() {
        assertThat(ProductStatus.DRAFT.canChangeTo(ProductStatus.APPROVED)).isFalse();
      }

      @Test
      void ON_SALE로_직접_변경할_수_없다() {
        assertThat(ProductStatus.DRAFT.canChangeTo(ProductStatus.ON_SALE)).isFalse();
      }
    }

    @Nested
    class PENDING_상태에서_전이_테스트 {

      @Test
      void APPROVED로_변경할_수_있다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.APPROVED)).isTrue();
      }

      @Test
      void REJECTED로_변경할_수_있다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.REJECTED)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void DRAFT로_변경할_수_없다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.DRAFT)).isFalse();
      }

      @Test
      void ON_SALE로_직접_변경할_수_없다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.ON_SALE)).isFalse();
      }
    }

    @Nested
    class APPROVED_상태에서_전이_테스트 {

      @Test
      void SCHEDULED로_변경할_수_있다() {
        assertThat(ProductStatus.APPROVED.canChangeTo(ProductStatus.SCHEDULED)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.APPROVED.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void DRAFT로_변경할_수_없다() {
        assertThat(ProductStatus.APPROVED.canChangeTo(ProductStatus.DRAFT)).isFalse();
      }

      @Test
      void ON_SALE로_직접_변경할_수_없다() {
        assertThat(ProductStatus.APPROVED.canChangeTo(ProductStatus.ON_SALE)).isFalse();
      }
    }

    @Nested
    class REJECTED_상태에서_전이_테스트 {

      @Test
      void DRAFT로_변경할_수_있다() {
        assertThat(ProductStatus.REJECTED.canChangeTo(ProductStatus.DRAFT)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.REJECTED.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void PENDING으로_직접_변경할_수_없다() {
        assertThat(ProductStatus.REJECTED.canChangeTo(ProductStatus.PENDING)).isFalse();
      }

      @Test
      void APPROVED로_직접_변경할_수_없다() {
        assertThat(ProductStatus.REJECTED.canChangeTo(ProductStatus.APPROVED)).isFalse();
      }
    }

    @Nested
    class SCHEDULED_상태에서_전이_테스트 {

      @Test
      void ON_SALE로_변경할_수_있다() {
        assertThat(ProductStatus.SCHEDULED.canChangeTo(ProductStatus.ON_SALE)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.SCHEDULED.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void DRAFT로_변경할_수_없다() {
        assertThat(ProductStatus.SCHEDULED.canChangeTo(ProductStatus.DRAFT)).isFalse();
      }

      @Test
      void APPROVED로_변경할_수_없다() {
        assertThat(ProductStatus.SCHEDULED.canChangeTo(ProductStatus.APPROVED)).isFalse();
      }
    }

    @Nested
    class ON_SALE_상태에서_전이_테스트 {

      @Test
      void CLOSED로_변경할_수_있다() {
        assertThat(ProductStatus.ON_SALE.canChangeTo(ProductStatus.CLOSED)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.ON_SALE.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void DRAFT로_변경할_수_없다() {
        assertThat(ProductStatus.ON_SALE.canChangeTo(ProductStatus.DRAFT)).isFalse();
      }

      @Test
      void PENDING으로_변경할_수_없다() {
        assertThat(ProductStatus.ON_SALE.canChangeTo(ProductStatus.PENDING)).isFalse();
      }
    }

    @Nested
    class CLOSED_상태에서_전이_테스트 {

      @Test
      void COMPLETED로_변경할_수_있다() {
        assertThat(ProductStatus.CLOSED.canChangeTo(ProductStatus.COMPLETED)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.CLOSED.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void ON_SALE로_변경할_수_없다() {
        assertThat(ProductStatus.CLOSED.canChangeTo(ProductStatus.ON_SALE)).isFalse();
      }

      @Test
      void DRAFT로_변경할_수_없다() {
        assertThat(ProductStatus.CLOSED.canChangeTo(ProductStatus.DRAFT)).isFalse();
      }
    }

    @Nested
    class COMPLETED_상태에서_전이_테스트 {

      @ParameterizedTest
      @EnumSource(ProductStatus.class)
      void 어떤_상태로도_변경할_수_없다(ProductStatus status) {
        assertThat(ProductStatus.COMPLETED.canChangeTo(status)).isFalse();
      }
    }

    @Nested
    class CANCELLED_상태에서_전이_테스트 {

      @ParameterizedTest
      @EnumSource(ProductStatus.class)
      void 어떤_상태로도_변경할_수_없다(ProductStatus status) {
        assertThat(ProductStatus.CANCELLED.canChangeTo(status)).isFalse();
      }
    }
  }

  @Nested
  class 상태_확인_메서드_테스트 {

    @Test
    void isDraft_메서드는_DRAFT일_때만_true를_반환한다() {
      assertThat(ProductStatus.DRAFT.isDraft()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.DRAFT) {
          assertThat(status.isDraft()).isFalse();
        }
      }
    }

    @Test
    void isPending_메서드는_PENDING일_때만_true를_반환한다() {
      assertThat(ProductStatus.PENDING.isPending()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.PENDING) {
          assertThat(status.isPending()).isFalse();
        }
      }
    }

    @Test
    void isApproved_메서드는_APPROVED일_때만_true를_반환한다() {
      assertThat(ProductStatus.APPROVED.isApproved()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.APPROVED) {
          assertThat(status.isApproved()).isFalse();
        }
      }
    }

    @Test
    void isRejected_메서드는_REJECTED일_때만_true를_반환한다() {
      assertThat(ProductStatus.REJECTED.isRejected()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.REJECTED) {
          assertThat(status.isRejected()).isFalse();
        }
      }
    }

    @Test
    void isScheduled_메서드는_SCHEDULED일_때만_true를_반환한다() {
      assertThat(ProductStatus.SCHEDULED.isScheduled()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.SCHEDULED) {
          assertThat(status.isScheduled()).isFalse();
        }
      }
    }

    @Test
    void isOnSale_메서드는_ON_SALE일_때만_true를_반환한다() {
      assertThat(ProductStatus.ON_SALE.isOnSale()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.ON_SALE) {
          assertThat(status.isOnSale()).isFalse();
        }
      }
    }

    @Test
    void isClosed_메서드는_CLOSED일_때만_true를_반환한다() {
      assertThat(ProductStatus.CLOSED.isClosed()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.CLOSED) {
          assertThat(status.isClosed()).isFalse();
        }
      }
    }

    @Test
    void isCompleted_메서드는_COMPLETED일_때만_true를_반환한다() {
      assertThat(ProductStatus.COMPLETED.isCompleted()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.COMPLETED) {
          assertThat(status.isCompleted()).isFalse();
        }
      }
    }

    @Test
    void isCancelled_메서드는_CANCELLED일_때만_true를_반환한다() {
      assertThat(ProductStatus.CANCELLED.isCancelled()).isTrue();
      for (ProductStatus status : ProductStatus.values()) {
        if (status != ProductStatus.CANCELLED) {
          assertThat(status.isCancelled()).isFalse();
        }
      }
    }
  }

  @Nested
  class 유틸리티_메서드_테스트 {

    @Test
    void isEditable_메서드는_DRAFT와_REJECTED일_때_true를_반환한다() {
      assertThat(ProductStatus.DRAFT.isEditable()).isTrue();
      assertThat(ProductStatus.REJECTED.isEditable()).isTrue();

      assertThat(ProductStatus.PENDING.isEditable()).isFalse();
      assertThat(ProductStatus.APPROVED.isEditable()).isFalse();
      assertThat(ProductStatus.SCHEDULED.isEditable()).isFalse();
      assertThat(ProductStatus.ON_SALE.isEditable()).isFalse();
      assertThat(ProductStatus.CLOSED.isEditable()).isFalse();
      assertThat(ProductStatus.COMPLETED.isEditable()).isFalse();
      assertThat(ProductStatus.CANCELLED.isEditable()).isFalse();
    }

    @Test
    void isTerminal_메서드는_COMPLETED와_CANCELLED일_때_true를_반환한다() {
      assertThat(ProductStatus.COMPLETED.isTerminal()).isTrue();
      assertThat(ProductStatus.CANCELLED.isTerminal()).isTrue();

      assertThat(ProductStatus.DRAFT.isTerminal()).isFalse();
      assertThat(ProductStatus.PENDING.isTerminal()).isFalse();
      assertThat(ProductStatus.APPROVED.isTerminal()).isFalse();
      assertThat(ProductStatus.REJECTED.isTerminal()).isFalse();
      assertThat(ProductStatus.SCHEDULED.isTerminal()).isFalse();
      assertThat(ProductStatus.ON_SALE.isTerminal()).isFalse();
      assertThat(ProductStatus.CLOSED.isTerminal()).isFalse();
    }
  }
}