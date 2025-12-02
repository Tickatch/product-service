package com.tickatch.product_service.product.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductType Enum 테스트")
class ProductTypeTest {

  @Nested
  class Enum_값_테스트 {

    @Test
    void ProductType은_CONCERT_MUSICAL_PLAY_SPORTS_값이_존재한다() {
      assertThat(ProductType.values())
          .containsExactly(
              ProductType.CONCERT,
              ProductType.MUSICAL,
              ProductType.PLAY,
              ProductType.SPORTS
          );
    }
  }

  @Nested
  class Description_테스트 {

    @Test
    void CONCERT의_description은_콘서트이다() {
      assertThat(ProductType.CONCERT.getDescription()).isEqualTo("콘서트");
    }

    @Test
    void MUSICAL의_description은_뮤지컬이다() {
      assertThat(ProductType.MUSICAL.getDescription()).isEqualTo("뮤지컬");
    }

    @Test
    void PLAY의_description은_연극이다() {
      assertThat(ProductType.PLAY.getDescription()).isEqualTo("연극");
    }

    @Test
    void SPORTS의_description은_스포츠이다() {
      assertThat(ProductType.SPORTS.getDescription()).isEqualTo("스포츠");
    }

    @ParameterizedTest
    @EnumSource(ProductType.class)
    void 모든_ProductType은_null이_아닌_description을_가진다(ProductType productType) {
      assertThat(productType.getDescription()).isNotNull().isNotBlank();
    }
  }

  @Nested
  class ValueOf_메서드_테스트 {

    @Test
    void 문자열로_ProductType을_조회할_수_있다() {
      assertThat(ProductType.valueOf("CONCERT")).isEqualTo(ProductType.CONCERT);
      assertThat(ProductType.valueOf("MUSICAL")).isEqualTo(ProductType.MUSICAL);
      assertThat(ProductType.valueOf("PLAY")).isEqualTo(ProductType.PLAY);
      assertThat(ProductType.valueOf("SPORTS")).isEqualTo(ProductType.SPORTS);
    }
  }
}