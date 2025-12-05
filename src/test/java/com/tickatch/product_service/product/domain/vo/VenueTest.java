package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Venue 테스트")
class VenueTest {

  @Nested
  class 생성_테스트 {
    @Test
    void 유효한_장소_정보를_생성한다() {
      Long stageId = 1L;
      String stageName = "올림픽홀";
      Long artHallId = 100L;
      String artHallName = "올림픽공원";
      String artHallAddress = "서울시 송파구";

      Venue venue = new Venue(stageId, stageName, artHallId, artHallName, artHallAddress);

      assertThat(venue.getStageId()).isEqualTo(stageId);
      assertThat(venue.getStageName()).isEqualTo(stageName);
      assertThat(venue.getArtHallId()).isEqualTo(artHallId);
      assertThat(venue.getArtHallName()).isEqualTo(artHallName);
      assertThat(venue.getArtHallAddress()).isEqualTo(artHallAddress);
    }

    @Test
    void stageId가_null이면_예외를_던진다() {
      assertThatThrownBy(() -> new Venue(null, "올림픽홀", 100L, "올림픽공원", "서울"))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void stageName이_null이면_예외를_던진다() {
      assertThatThrownBy(() -> new Venue(1L, null, 100L, "올림픽공원", "서울"))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void stageName이_빈_문자열이면_예외를_던진다() {
      assertThatThrownBy(() -> new Venue(1L, "  ", 100L, "올림픽공원", "서울"))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void artHallId가_null이면_예외를_던진다() {
      assertThatThrownBy(() -> new Venue(1L, "올림픽홀", null, "올림픽공원", "서울"))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void artHallName이_null이면_예외를_던진다() {
      assertThatThrownBy(() -> new Venue(1L, "올림픽홀", 100L, null, "서울"))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void artHallName이_빈_문자열이면_예외를_던진다() {
      assertThatThrownBy(() -> new Venue(1L, "올림픽홀", 100L, "  ", "서울"))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void artHallAddress가_null이면_예외를_던진다() {
      assertThatThrownBy(() -> new Venue(1L, "올림픽홀", 100L, "올림픽공원", null))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void artHallAddress가_빈_문자열이면_예외를_던진다() {
        assertThatThrownBy(() -> new Venue(1L, "올림픽홀", 100L, "올림픽공원", "  "))
            .isInstanceOf(ProductException.class);
    }
  }

  @Nested
  class 정보_업데이트_테스트 {

    @Test
    void 장소_정보를_업데이트하면_새_객체를_반환한다() {
      Venue original = new Venue(1L, "올림픽홀", 100L, "올림픽공원", "서울시 송파구");

      Venue updated = original.updateInfo("대공연장", "세종문화회관", "서울시 종로구");

      assertThat(updated).isNotSameAs(original);
      assertThat(updated.getStageId()).isEqualTo(original.getStageId());
      assertThat(updated.getArtHallId()).isEqualTo(original.getArtHallId());
      assertThat(updated.getStageName()).isEqualTo("대공연장");
      assertThat(updated.getArtHallName()).isEqualTo("세종문화회관");
      assertThat(updated.getArtHallAddress()).isEqualTo("서울시 종로구");
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값을_가진_Venue는_동등하다() {
      Venue venue1 = new Venue(1L, "올림픽홀", 100L, "올림픽공원", "서울");
      Venue venue2 = new Venue(1L, "올림픽홀", 100L, "올림픽공원", "서울");

      assertThat(venue1).isEqualTo(venue2);
      assertThat(venue1.hashCode()).isEqualTo(venue2.hashCode());
    }
  }
}