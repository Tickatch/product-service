package com.tickatch.product_service.product.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductStatus Enum 테스트")
class ProductStatusTest {

  @Nested
  class Enum_값_테스트 {

    @Test
    void DRAFT_PENDING_ON_SALE_SOLD_OUT_CANCELLED_값이_존재한다() {
      assertThat(ProductStatus.values())
          .containsExactly(
              ProductStatus.DRAFT,
              ProductStatus.PENDING,
              ProductStatus.ON_SALE,
              ProductStatus.SOLD_OUT,
              ProductStatus.CANCELLED
          );
    }
  }

  @Nested
  class Description_테스트 {
    
    @Test
    void DRAFT의_description은_임시저장이다() {
      assertThat(ProductStatus.DRAFT.getDescription()).isEqualTo("임시저장");
    }

    @Test
    void PENDING의_description은_판매대기이다() {
      assertThat(ProductStatus.PENDING.getDescription()).isEqualTo("판매대기");
    }

    @Test
    void ON_SALE의_description은_판매중이다() {
      assertThat(ProductStatus.ON_SALE.getDescription()).isEqualTo("판매중");
    }

    @Test
    void SOLD_OUT의_description은_매진이다() {
      assertThat(ProductStatus.SOLD_OUT.getDescription()).isEqualTo("매진");
    }
    @Test
    void CANCELLED의_description은_취소됨이다() {
      assertThat(ProductStatus.CANCELLED.getDescription()).isEqualTo("취소됨");
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    void 모든_ProductStatus는_null이_아닌_description을_가진다(ProductStatus productStatus) {
      assertThat(productStatus.getDescription()).isNotNull().isNotBlank();
    }
  }

  @Nested
  class 상태_전이_규칙_테스트 {

    @Test
    void null로는_변경할_수_없다() {
      for (ProductStatus status: ProductStatus.values()) {
        assertThat(status.canChangeTo(status)).isFalse();
      }
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    void 동일한_상태로는_변경할_수_없다(ProductStatus productStatus) {
      assertThat(productStatus.canChangeTo(productStatus)).isFalse();
    }

    @Nested
    class DRAFT_상태에서_전이_테스트 {

      @Test
      void FENDING으로_변경할_수_있다() {
        assertThat(ProductStatus.DRAFT.canChangeTo(ProductStatus.PENDING)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.DRAFT.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void ON_SALE로_직접_변경할_수_없다() {
        assertThat(ProductStatus.DRAFT.canChangeTo(ProductStatus.ON_SALE)).isFalse();
      }

      @Test
      void SOLD_OUT으로_직접_변경할_수_없다() {
        assertThat(ProductStatus.DRAFT.canChangeTo(ProductStatus.SOLD_OUT)).isFalse();
      }
    }

    @Nested
    class PENDING_상태에서_전이_테스트 {

      @Test
      void DRAFT로_변경할_수_있다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.DRAFT)).isTrue();
      }

      @Test
      void ON_SALE로_변경할_수_있다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.ON_SALE)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.CANCELLED)).isTrue();

      }

      @Test
      void SOLD_OUT으로_직접_변경할_수_없다() {
        assertThat(ProductStatus.PENDING.canChangeTo(ProductStatus.SOLD_OUT)).isFalse();
      }
    }

    @Nested
    class ON_SALE_상태에서_전이_테스트 {

      @Test
      void SOLD_OUT으로_변경할_수_있다() {
        assertThat(ProductStatus.ON_SALE.canChangeTo(ProductStatus.SOLD_OUT)).isTrue();
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
    class SOLD_OUT_상태에서_전이_테스트 {

      @Test
      void ON_SALE으로_변경할_수_있다() {
        assertThat(ProductStatus.SOLD_OUT.canChangeTo(ProductStatus.ON_SALE)).isTrue();
      }

      @Test
      void CANCELLED로_변경할_수_있다() {
        assertThat(ProductStatus.SOLD_OUT.canChangeTo(ProductStatus.CANCELLED)).isTrue();
      }

      @Test
      void DRAFT로_변경할_수_없다() {
        assertThat(ProductStatus.SOLD_OUT.canChangeTo(ProductStatus.DRAFT)).isFalse();
      }

      @Test
      void PENDING으로_변경할_수_없다() {
        assertThat(ProductStatus.SOLD_OUT.canChangeTo(ProductStatus.PENDING)).isFalse();
      }
    }

    @Nested
    class CANCELLED_상태에서_전이_테스트 {

      @ParameterizedTest
      @EnumSource(ProductStatus.class)
      void 어떤_상태로도_변경할_수_없다(ProductStatus productStatus) {
        assertThat(ProductStatus.CANCELLED.canChangeTo(productStatus)).isFalse();
      }
    }

    @Nested
    class 상태_확인_메서드_테스트 {

      @Test
      void isDraft_메서드는_DRAFT일_때만_true를_반환한다() {
        assertThat(ProductStatus.DRAFT.isDraft()).isTrue();
        assertThat(ProductStatus.PENDING.isDraft()).isFalse();
        assertThat(ProductStatus.ON_SALE.isDraft()).isFalse();
        assertThat(ProductStatus.SOLD_OUT.isDraft()).isFalse();
        assertThat(ProductStatus.CANCELLED.isDraft()).isFalse();
      }

      @Test
      void isPending_메서드는_PENDING일_때만_true를_반환한다() {
        assertThat(ProductStatus.PENDING.isPending()).isTrue();
        assertThat(ProductStatus.DRAFT.isPending()).isFalse();
        assertThat(ProductStatus.ON_SALE.isPending()).isFalse();
        assertThat(ProductStatus.SOLD_OUT.isPending()).isFalse();
        assertThat(ProductStatus.CANCELLED.isPending()).isFalse();
      }

      @Test
      void isOnSale_메서드는_ON_SALE일_때만_true를_반환한다() {
        assertThat(ProductStatus.ON_SALE.isOnSale()).isTrue();
        assertThat(ProductStatus.DRAFT.isOnSale()).isFalse();
        assertThat(ProductStatus.PENDING.isOnSale()).isFalse();
        assertThat(ProductStatus.SOLD_OUT.isOnSale()).isFalse();
        assertThat(ProductStatus.CANCELLED.isOnSale()).isFalse();
      }

      @Test
      void isSoldOut_은_SOLD_OUT일_때만_true를_반환한다() {
        assertThat(ProductStatus.SOLD_OUT.isSoldOut()).isTrue();
        assertThat(ProductStatus.DRAFT.isSoldOut()).isFalse();
        assertThat(ProductStatus.PENDING.isSoldOut()).isFalse();
        assertThat(ProductStatus.ON_SALE.isSoldOut()).isFalse();
        assertThat(ProductStatus.CANCELLED.isSoldOut()).isFalse();
      }

      @Test
      void isCancelled_메서드는_CANCELLED일_때만_true를_반환한다() {
        assertThat(ProductStatus.CANCELLED.isCancelled()).isTrue();
        assertThat(ProductStatus.DRAFT.isCancelled()).isFalse();
        assertThat(ProductStatus.PENDING.isCancelled()).isFalse();
        assertThat(ProductStatus.ON_SALE.isCancelled()).isFalse();
        assertThat(ProductStatus.SOLD_OUT.isCancelled()).isFalse();
      }
    }
  }
}