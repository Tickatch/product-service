package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("AgeRestriction 테스트")
class AgeRestrictionTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_값으로_생성할_수_있다() {
      AgeRestriction restriction = new AgeRestriction(AgeRating.FIFTEEN, "보호자 동반 필요");

      assertThat(restriction.getAgeRating()).isEqualTo(AgeRating.FIFTEEN);
      assertThat(restriction.getRestrictionNotice()).isEqualTo("보호자 동반 필요");
    }

    @Test
    void ageRating이_null이면_ALL로_설정된다() {
      AgeRestriction restriction = new AgeRestriction(null, null);

      assertThat(restriction.getAgeRating()).isEqualTo(AgeRating.ALL);
    }

    @Test
    void restrictionNotice가_null이어도_생성할_수_있다() {
      AgeRestriction restriction = new AgeRestriction(AgeRating.TWELVE, null);

      assertThat(restriction.getRestrictionNotice()).isNull();
    }

    @Test
    void restrictionNotice가_500자를_초과하면_예외가_발생한다() {
      String longNotice = "a".repeat(501);

      assertThatThrownBy(() -> new AgeRestriction(AgeRating.ALL, longNotice))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_AGE_RESTRICTION);
    }

    @Test
    void restrictionNotice가_500자이면_생성에_성공한다() {
      String maxNotice = "a".repeat(500);

      AgeRestriction restriction = new AgeRestriction(AgeRating.ALL, maxNotice);

      assertThat(restriction.getRestrictionNotice()).hasSize(500);
    }
  }

  @Nested
  class DefaultRestriction_테스트 {

    @Test
    void 전체_관람가로_기본_제한_정보를_생성한다() {
      AgeRestriction restriction = AgeRestriction.defaultRestriction();

      assertThat(restriction.getAgeRating()).isEqualTo(AgeRating.ALL);
      assertThat(restriction.getRestrictionNotice()).isNull();
    }
  }

  @Nested
  class getMinimumAge_테스트 {

    @Test
    void ALL등급의_최소_나이는_0이다() {
      AgeRestriction restriction = new AgeRestriction(AgeRating.ALL, null);

      assertThat(restriction.getMinimumAge()).isZero();
    }

    @Test
    void TWELVE_등급의_최소_나이는_12이다() {
      AgeRestriction restriction = new AgeRestriction(AgeRating.TWELVE, null);

      assertThat(restriction.getMinimumAge()).isEqualTo(12);
    }

    @Test
    void FIFTEEN_등급의_최소_나이는_15이다() {
      AgeRestriction restriction = new AgeRestriction(AgeRating.FIFTEEN, null);

      assertThat(restriction.getMinimumAge()).isEqualTo(15);
    }

    @Test
    void NINETEEN_등급의_최소_나이는_19이다() {
      AgeRestriction restriction = new AgeRestriction(AgeRating.NINETEEN, null);

      assertThat(restriction.getMinimumAge()).isEqualTo(19);
    }
  }

  @Nested
  class isAdultOnly_테스트 {

    @Test
    void NINETEEN_등급이면_성인_전용이다() {
      AgeRestriction restriction = new AgeRestriction(AgeRating.NINETEEN, null);

      assertThat(restriction.isAdultOnly()).isTrue();
    }

    @Test
    void NINETEEN_외_등급은_성인_전용이_아니다() {
      assertThat(new AgeRestriction(AgeRating.ALL, null).isAdultOnly()).isFalse();
      assertThat(new AgeRestriction(AgeRating.TWELVE, null).isAdultOnly()).isFalse();
      assertThat(new AgeRestriction(AgeRating.FIFTEEN, null).isAdultOnly()).isFalse();
    }
  }

  @Nested
  class CanWatch_테스트 {

    @ParameterizedTest(name = "{0} 등급: {1}세 관람 가능 여부 = {2}")
    @CsvSource({
        "ALL, 5, true",
        "ALL, 0, true",
        "TWELVE, 11, false",
        "TWELVE, 12, true",
        "FIFTEEN, 14, false",
        "FIFTEEN, 15, true",
        "NINETEEN, 18, false",
        "NINETEEN, 19, true"
    })
    void 등급에_따라_관람_가능_여부를_확인한다(AgeRating rating, int age, boolean expected) {
      AgeRestriction restriction = new AgeRestriction(rating, null);

      assertThat(restriction.canWatch(age)).isEqualTo(expected);
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값을_가진_객체는_동등하다() {
      AgeRestriction restriction1 = new AgeRestriction(AgeRating.FIFTEEN, "안내");
      AgeRestriction restriction2 = new AgeRestriction(AgeRating.FIFTEEN, "안내");

      assertThat(restriction1).isEqualTo(restriction2);
      assertThat(restriction1.hashCode()).isEqualTo(restriction2.hashCode());
    }

    @Test
    void 다른_등급을_가진_객체는_동등하지_않다() {
      AgeRestriction restriction1 = new AgeRestriction(AgeRating.FIFTEEN, null);
      AgeRestriction restriction2 = new AgeRestriction(AgeRating.TWELVE, null);

      assertThat(restriction1).isNotEqualTo(restriction2);
    }
  }
}