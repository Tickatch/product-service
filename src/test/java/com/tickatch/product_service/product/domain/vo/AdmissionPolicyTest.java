package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AdmissionPolicy 테스트")
class AdmissionPolicyTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_값으로_생성할_수_있다() {
      AdmissionPolicy policy = new AdmissionPolicy(60, true, "인터미션 중 입장 가능", true, 15, true, true);

      assertThat(policy.getAdmissionMinutesBefore()).isEqualTo(60);
      assertThat(policy.getLateEntryAllowed()).isTrue();
      assertThat(policy.getLateEntryNotice()).isEqualTo("인터미션 중 입장 가능");
      assertThat(policy.getHasIntermission()).isTrue();
      assertThat(policy.getIntermissionMinutes()).isEqualTo(15);
      assertThat(policy.getPhotographyAllowed()).isTrue();
      assertThat(policy.getFoodAllowed()).isTrue();
    }

    @Test
    void null_값은_기본값으로_설정된다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, null, null, null, null, null, null);

      assertThat(policy.getAdmissionMinutesBefore()).isEqualTo(30);
      assertThat(policy.getLateEntryAllowed()).isFalse();
      assertThat(policy.getLateEntryNotice()).isNull();
      assertThat(policy.getHasIntermission()).isFalse();
      assertThat(policy.getIntermissionMinutes()).isNull();
      assertThat(policy.getPhotographyAllowed()).isFalse();
      assertThat(policy.getFoodAllowed()).isFalse();
    }

    @Test
    void 인터미션이_있을_때_인터미션_시간이_없으면_예외가_발생한다() {
      assertThatThrownBy(() -> new AdmissionPolicy(null, null, null, true, null, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_ADMISSION_POLICY);
    }

    @Test
    void 인터미션이_있을_때_인터미션_시간이_0_이하면_예외가_발생한다() {
      assertThatThrownBy(() -> new AdmissionPolicy(null, null, null, true, 0, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_ADMISSION_POLICY);

      assertThatThrownBy(() -> new AdmissionPolicy(null, null, null, true, -5, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_ADMISSION_POLICY);
    }

    @Test
    void 인터미션이_없으면_인터미션_시간이_없어도_생성된다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, null, null, false, null, null, null);

      assertThat(policy.getHasIntermission()).isFalse();
      assertThat(policy.getIntermissionMinutes()).isNull();
    }

    @Test
    void 입장_시작_시간이_음수이면_예외가_발생한다() {
      assertThatThrownBy(() -> new AdmissionPolicy(-1, null, null, null, null, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_ADMISSION_POLICY);
    }

    @Test
    void 입장_시작_시간이_0이면_생성에_성공한다() {
      AdmissionPolicy policy = new AdmissionPolicy(0, null, null, null, null, null, null);

      assertThat(policy.getAdmissionMinutesBefore()).isZero();
    }

    @Test
    void 지각_입장_안내가_200자를_초과하면_예외가_발생한다() {
      String longNotice = "a".repeat(201);

      assertThatThrownBy(() -> new AdmissionPolicy(null, null, longNotice, null, null, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_ADMISSION_POLICY);
    }

    @Test
    void 지각_입장_안내가_200자이면_생성에_성공한다() {
      String maxNotice = "a".repeat(200);

      AdmissionPolicy policy = new AdmissionPolicy(null, null, maxNotice, null, null, null, null);

      assertThat(policy.getLateEntryNotice()).hasSize(200);
    }
  }

  @Nested
  class defaultPolicy_테스트 {

    @Test
    void 기본_정책을_생성한다() {
      AdmissionPolicy policy = AdmissionPolicy.defaultPolicy();

      assertThat(policy.getAdmissionMinutesBefore()).isEqualTo(30);
      assertThat(policy.getLateEntryAllowed()).isFalse();
      assertThat(policy.getLateEntryNotice()).isNull();
      assertThat(policy.getHasIntermission()).isFalse();
      assertThat(policy.getIntermissionMinutes()).isNull();
      assertThat(policy.getPhotographyAllowed()).isFalse();
      assertThat(policy.getFoodAllowed()).isFalse();
    }
  }

  @Nested
  class allowLateEntry_테스트 {

    @Test
    void 지각_입장_가능_시_true를_반환한다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, true, null, null, null, null, null);

      assertThat(policy.allowsLateEntry()).isTrue();
    }

    @Test
    void 지각_입장_불가_시_false를_반환한다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, false, null, null, null, null, null);

      assertThat(policy.allowsLateEntry()).isFalse();
    }
  }

  @Nested
  class hasIntermission_테스트 {

    @Test
    void 인터미션이_있으면_true를_반환한다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, null, null, true, 15, null, null);

      assertThat(policy.hasIntermission()).isTrue();
    }

    @Test
    void 인터미션이_없으면_false를_반환한다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, null, null, false, null, null, null);

      assertThat(policy.hasIntermission()).isFalse();
    }
  }

  @Nested
  class allowPhotography_테스트 {

    @Test
    void 촬영_가능_시_true를_반환한다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, null, null, null, null, true, null);

      assertThat(policy.allowsPhotography()).isTrue();
    }

    @Test
    void 활영_불가_시_false를_반환한다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, null, null, null, null, false, null);

      assertThat(policy.allowsPhotography()).isFalse();
    }
  }

  @Nested
  class allowsFood_테스트 {

    @Test
    void 음식물_반입_가능_시_true를_반환한다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, null, null, null, null, null, true);

      assertThat(policy.allowsFood()).isTrue();
    }

    @Test
    void 음식물_반입_불가_시_false를_반환한다() {
      AdmissionPolicy policy = new AdmissionPolicy(null, null, null, null, null, null, false);

      assertThat(policy.allowsFood()).isFalse();
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값을_가진_객체는_동등하다() {
      AdmissionPolicy policy1 = new AdmissionPolicy(30, true, "안내", true, 15, true, false);
      AdmissionPolicy policy2 = new AdmissionPolicy(30, true, "안내", true, 15, true, false);

      assertThat(policy1).isEqualTo(policy2);
      assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
    }

    @Test
    void 다른_값을_가진_객체는_동등하지_않다() {
      AdmissionPolicy policy1 = new AdmissionPolicy(30, true, null, false, null, null, null);
      AdmissionPolicy policy2 = new AdmissionPolicy(60, true, null, false, null, null, null);

      assertThat(policy1).isNotEqualTo(policy2);
    }
  }
}
