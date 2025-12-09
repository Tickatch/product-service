package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("AgeRating 테스트")
class AgeRatingTest {

  @Nested
  class canWatch_메서드_테스트 {

    @ParameterizedTest(name = "ALL 등급: {0}세는 관람 {1}")
    @CsvSource({"0, true", "5, true", "18, true", "100, true"})
    @DisplayName("ALL 등급은 모든 연령이 관람 가능하다")
    void ALL_등급은_모든_연령이_관람_가능하다(int age, boolean expected) {
      assertThat(AgeRating.ALL.canWatch(age)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "TWELVE 등급: {0}세는 관람 {1}")
    @CsvSource({"0, false", "11, false", "12, true", "13, true", "18, true"})
    @DisplayName("TWELVE 등급은 12세 이상만 관람 가능하다")
    void TWELVE_등급은_12세_이상만_관람_가능하다(int age, boolean expected) {
      assertThat(AgeRating.TWELVE.canWatch(age)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "FIFTEEN 등급: {0}세는 관람 {1}")
    @CsvSource({"0, false", "14, false", "15, true", "16, true", "18, true"})
    void FIFTEEN_등급은_15세_이상만_관람_가능하다(int age, boolean expected) {
      assertThat(AgeRating.FIFTEEN.canWatch(age)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "NINETEEN 등급: {0}세는 관람 {1}")
    @CsvSource({"0, false", "18, false", "19, true", "20, true", "30, true"})
    void NINETEEN_등급은_19세_이상만_관람_가능하다(int age, boolean expected) {
      assertThat(AgeRating.NINETEEN.canWatch(age)).isEqualTo(expected);
    }
  }

  @Nested
  class isAultOnly_메서드_테스트 {

    @Test
    void NINETEEN_등급만_성인_등급이다() {
      assertThat(AgeRating.NINETEEN.isAdultOnly()).isTrue();
    }

    @Test
    void ALL_등급은_성인_등급이_아니다() {
      assertThat(AgeRating.ALL.isAdultOnly()).isFalse();
    }

    @Test
    void TWELVE_등급은_성인_등급이_아니다() {
      assertThat(AgeRating.TWELVE.isAdultOnly()).isFalse();
    }

    @Test
    void FIFTEEN_등급은_성인_등급이_아니다() {
      assertThat(AgeRating.FIFTEEN.isAdultOnly()).isFalse();
    }
  }

  @Nested
  class 속성_테스트 {

    @Test
    void 각_등급의_설명이_올바르게_반환된다() {
      assertThat(AgeRating.ALL.getDescription()).isEqualTo("전체 관람가");
      assertThat(AgeRating.TWELVE.getDescription()).isEqualTo("12세 이상");
      assertThat(AgeRating.FIFTEEN.getDescription()).isEqualTo("15세 이상");
      assertThat(AgeRating.NINETEEN.getDescription()).isEqualTo("19세 이상");
    }

    @Test
    void 각_등급의_최소_나이가_올바르게_반환된다() {
      assertThat(AgeRating.ALL.getMinimumAge()).isEqualTo(0);
      assertThat(AgeRating.TWELVE.getMinimumAge()).isEqualTo(12);
      assertThat(AgeRating.FIFTEEN.getMinimumAge()).isEqualTo(15);
      assertThat(AgeRating.NINETEEN.getMinimumAge()).isEqualTo(19);
    }
  }
}
