package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RefundPolicy 테스트")
class RefundPolicyTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_값으로_생성할_수_있다() {
      RefundPolicy policy = new RefundPolicy(true, 3, "공연 3일 전까지 전액 환불");

      assertThat(policy.getCancellable()).isTrue();
      assertThat(policy.getCancelDeadlineDays()).isEqualTo(3);
      assertThat(policy.getRefundPolicyText()).isEqualTo("공연 3일 전까지 전액 환불");
    }

    @Test
    void null_값은_기본값으로_설정된다() {
      RefundPolicy policy = new RefundPolicy(null, null, null);

      assertThat(policy.getCancellable()).isTrue();
      assertThat(policy.getCancelDeadlineDays()).isEqualTo(1);
      assertThat(policy.getRefundPolicyText()).isNull();
    }

    @Test
    void 취소_마감일이_0이면_생성에_성공한다() {
      RefundPolicy policy = new RefundPolicy(true, 0, null);

      assertThat(policy.getCancelDeadlineDays()).isZero();
    }

    @Test
    void 취소_마감일이_음수이면_예외가_발생한다() {
      assertThatThrownBy(() -> new RefundPolicy(true, -1, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_REFUND_POLICY);
    }

    @Test
    void 환불_정책_안내가_1000자를_초과하면_예외가_발생한다() {
      String longText = "a".repeat(1001);

      assertThatThrownBy(() -> new RefundPolicy(true, 1, longText))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_REFUND_POLICY);
    }

    @Test
    void 환불_정책_안내가_1000자이면_생성에_성공한다() {
      String maxText = "a".repeat(1000);

      RefundPolicy policy = new RefundPolicy(true, 1, maxText);

      assertThat(policy.getRefundPolicyText()).hasSize(1000);
    }
  }

  @Nested
  class defaultPolicy_테스트 {

    @Test
    void 기본_정책을_생성한다() {
      RefundPolicy policy = RefundPolicy.defaultPolicy();

      assertThat(policy.getCancellable()).isTrue();
      assertThat(policy.getCancelDeadlineDays()).isEqualTo(1);
      assertThat(policy.getRefundPolicyText()).isNull();
    }
  }

  @Nested
  class nonRefundable_테스트 {

    @Test
    void 취소_불가_정책을_생성한다() {
      RefundPolicy policy = RefundPolicy.nonRefundable();

      assertThat(policy.getCancellable()).isFalse();
      assertThat(policy.getCancelDeadlineDays()).isZero();
      assertThat(policy.getRefundPolicyText()).isEqualTo("본 상품은 취소/환불이 불가합니다.");
    }
  }

  @Nested
  class isCancellable_테스트 {

    @Test
    void 취소_가능_시_true를_반환한다() {
      RefundPolicy policy = new RefundPolicy(true, 1, null);

      assertThat(policy.isCancellable()).isTrue();
    }

    @Test
    void 취소_불가_시_false를_반환한다() {
      RefundPolicy policy = new RefundPolicy(false, 0, null);

      assertThat(policy.isCancellable()).isFalse();
    }
  }

  @Nested
  class canCancelFor_테스트 {

    @Test
    void 취소_불가_정책이면_항상_false를_반환한다() {
      RefundPolicy policy = RefundPolicy.nonRefundable();
      LocalDate eventDate = LocalDate.now().plusDays(30);

      assertThat(policy.canCancelFor(eventDate)).isFalse();
    }

    @Test
    void 마감일_이전이면_취소_가능하다() {
      RefundPolicy policy = new RefundPolicy(true, 3, null);
      LocalDate eventDate = LocalDate.now().plusDays(5);

      // 오늘 기준으로 5일 후 행사, 3일 전까지 취소 가능 = 2일 후까지 취소 가능
      assertThat(policy.canCancelFor(eventDate)).isTrue();
    }

    @Test
    void 마감일_당일은_취소_가능하다() {
      RefundPolicy policy = new RefundPolicy(true, 3, null);
      LocalDate eventDate = LocalDate.now().plusDays(3);

      // 오늘 기준으로 3일 후 행사, 3일 전까지 취소 가능 = 오늘까지 취소 가능
      assertThat(policy.canCancelFor(eventDate)).isTrue();
    }

    @Test
    void 마감일_이후면_취소_불가능하다() {
      RefundPolicy policy = new RefundPolicy(true, 3, null);
      LocalDate eventDate = LocalDate.now().plusDays(2);

      // 오늘 기준으로 2일 후 행사, 3일 전까지 취소 가능 = 어제까지 취소 가능했음 (이미 마감)
      assertThat(policy.canCancelFor(eventDate)).isFalse();
    }

    @Test
    void 당일_취소_기능_청책일_때_당일도_취소_가능하다() {
      RefundPolicy policy = new RefundPolicy(true, 0, null);
      LocalDate eventDate = LocalDate.now();

      assertThat(policy.canCancelFor(eventDate)).isTrue();
    }
  }

  @Nested
  class getCancelDeadLine_테스트 {

    @Test
    void 행사일_기준_마감일을_계산한다() {
      RefundPolicy policy = new RefundPolicy(true, 3, null);
      LocalDate eventDate = LocalDate.of(2024, 12, 25);

      LocalDate deadline = policy.getCancelDeadline(eventDate);

      assertThat(deadline).isEqualTo(LocalDate.of(2024, 12, 22));
    }

    @Test
    void 마감일이_0일이면_행사일과_같다() {
      RefundPolicy policy = new RefundPolicy(true, 0, null);
      LocalDate eventDate = LocalDate.of(2024, 12, 25);

      LocalDate deadline = policy.getCancelDeadline(eventDate);

      assertThat(deadline).isEqualTo(eventDate);
    }

    @Test
    void 마감일이_7일이면_행사_7일_전이다() {
      RefundPolicy policy = new RefundPolicy(true, 7, null);
      LocalDate eventDate = LocalDate.of(2024, 12, 25);

      LocalDate deadline = policy.getCancelDeadline(eventDate);

      assertThat(deadline).isEqualTo(LocalDate.of(2024, 12, 18));
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값을_가진_객체는_동등하다() {
      RefundPolicy policy1 = new RefundPolicy(true, 3, "안내");
      RefundPolicy policy2 = new RefundPolicy(true, 3, "안내");

      assertThat(policy1).isEqualTo(policy2);
      assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
    }

    @Test
    void 다른_값을_가진_객체는_동등하지_않다() {
      RefundPolicy policy1 = new RefundPolicy(true, 3, null);
      RefundPolicy policy2 = new RefundPolicy(true, 5, null);

      assertThat(policy1).isNotEqualTo(policy2);
    }
  }
}