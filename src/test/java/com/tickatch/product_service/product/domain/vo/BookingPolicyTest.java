package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("BookingPolicy 테스트")
class BookingPolicyTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_값으로_생성할_수_있다() {}

    @Test
    void null_값은_기본값으로_설정된다() {}

    @ParameterizedTest(name = "최대 매수 {0}은 유효하다")
    @ValueSource(ints = {1, 2, 5, 10})
    void 범위_내에_최대_매수로_생성할_수_있다(int maxTickets) {
      BookingPolicy policy = new BookingPolicy(maxTickets, null, null);

      assertThat(policy.getMaxTicketsPerPerson()).isEqualTo(maxTickets);
    }

    @ParameterizedTest(name = "최대 매수 {0}은 유효하지 않다")
    @ValueSource(ints = {0, -1, 11, 100})
    void 범위를_벗어난_최대_매수는_예외가_발생한다(int maxTickets) {
      assertThatThrownBy(() -> new BookingPolicy(maxTickets, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_BOOKING_POLICY);
    }
  }

  @Nested
  class defaultPolicy_테스트 {

    @Test
    void 기본_정책을_생성한다() {
      BookingPolicy policy = BookingPolicy.defaultPolicy();

      assertThat(policy.getMaxTicketsPerPerson()).isEqualTo(4);
      assertThat(policy.getIdVerificationRequired()).isFalse();
      assertThat(policy.getTransferable()).isTrue();
    }
  }

  @Nested
  class canBook_테스트 {

    @Test
    void 최대_매수_이하의_수량은_예매_가능하다() {
      BookingPolicy policy = new BookingPolicy(4, null, null);

      assertThat(policy.canBook(1)).isTrue();
      assertThat(policy.canBook(2)).isTrue();
      assertThat(policy.canBook(4)).isTrue();
    }

    @Test
    void 최대_매수를_초과한_수량은_예매_불가능하다() {
      BookingPolicy policy = new BookingPolicy(4, null, null);

      assertThat(policy.canBook(5)).isFalse();
      assertThat(policy.canBook(10)).isFalse();
    }

    @Test
    void Zero_이하의_수량은_예매_불가능하다() {
      BookingPolicy policy = new BookingPolicy(4, null, null);

      assertThat(policy.canBook(0)).isFalse();
      assertThat(policy.canBook(-1)).isFalse();
    }
  }

  @Nested
  class RequiresIdVerification_테스트 {

    @Test
    void 본인확인_필요_시_true를_반환한다() {
      BookingPolicy policy = new BookingPolicy(4, true, null);

      assertThat(policy.requiresIdVerification()).isTrue();
    }

    @Test
    void 본인확인_불필요_시_false를_반환한다() {
      BookingPolicy policy = new BookingPolicy(4, false, null);

      assertThat(policy.requiresIdVerification()).isFalse();
    }
  }

  @Nested
  class isTransferable_테스트 {

    @Test
    void 양도_가능_시_true를_반환한다() {
      BookingPolicy policy = new BookingPolicy(4, null, true);

      assertThat(policy.isTransferable()).isTrue();
    }

    @Test
    void 양도_불가_시_false를_반환한다() {
      BookingPolicy policy = new BookingPolicy(4, null, false);

      assertThat(policy.isTransferable()).isFalse();
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값을_가진_객체는_동등하다() {
      BookingPolicy policy1 = new BookingPolicy(4, true, false);
      BookingPolicy policy2 = new BookingPolicy(4, true, false);

      assertThat(policy1).isEqualTo(policy2);
      assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
    }

    @Test
    void 다른_값을_가진_객체는_동등하지_않다() {
      BookingPolicy policy1 = new BookingPolicy(4, true, false);
      BookingPolicy policy2 = new BookingPolicy(6, true, false);

      assertThat(policy1).isNotEqualTo(policy2);
    }
  }
}
