package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SeatSummary 테스트")
class SeatSummaryTest {

  @Nested
  class 생성_테스트 {

    @Test
    void  좌석_현황을_생성한다() {
      Integer totalSeats = 100;
      Integer availableSeats = 80;

      SeatSummary seatSummary = new SeatSummary(totalSeats, availableSeats);

      assertThat(seatSummary.getTotalSeats()).isEqualTo(100);
      assertThat(seatSummary.getAvailableSeats()).isEqualTo(80);
      assertThat(seatSummary.getUpdatedAt()).isNotNull();
    }

    @Test
    void null_값은_0으로_초기화한다() {
      SeatSummary seatSummary = new SeatSummary(null, null);

      assertThat(seatSummary.getTotalSeats()).isEqualTo(0);
      assertThat(seatSummary.getAvailableSeats()).isEqualTo(0);
    }

    @Test
    void 총_좌석수로_초기화하면_잔여_좌석도_동일하다() {
      SeatSummary seatSummary = SeatSummary.initialize(500);

      assertThat(seatSummary.getTotalSeats()).isEqualTo(500);
      assertThat(seatSummary.getAvailableSeats()).isEqualTo(500);
    }

    @Test
    void 빈_좌석_현황을_생성한다() {
      SeatSummary seatSummary = SeatSummary.empty();

      assertThat(seatSummary.getTotalSeats()).isEqualTo(0);
      assertThat(seatSummary.getAvailableSeats()).isEqualTo(0);
    }
  }

  @Nested
  class 좌석_계산_테스트 {

    @Test
    void 판매된_좌석_수를_계산한다() {
      SeatSummary seatSummary = new SeatSummary(100, 30);

      assertThat(seatSummary.getSoldSeats()).isEqualTo(70);
    }

    @Test
    void 판매율을_계산한다() {
      SeatSummary seatSummary = new SeatSummary(100, 30);

      assertThat(seatSummary.getSoldRate()).isEqualTo(70.0);
    }

    @Test
    void 총_좌석이_0이면_판매율은_0이다() {
      SeatSummary seatSummary = SeatSummary.empty();

      assertThat(seatSummary.getSoldRate()).isEqualTo(0.0);
    }
  }

  @Nested
  class 매진_확인_테스트 {

    @Test
    void 잔여_좌석이_0이면_매진이다() {
      SeatSummary seatSummary = new SeatSummary(100, 0);

      assertThat(seatSummary.isSoldOut()).isTrue();
      assertThat(seatSummary.hasAvailableSeats()).isFalse();
    }

    @Test
    void 잔여_좌석이_있으면_매진이_아니다() {
      SeatSummary seatSummary = new SeatSummary(100, 1);

      assertThat(seatSummary.isSoldOut()).isFalse();
      assertThat(seatSummary.hasAvailableSeats()).isTrue();
    }
  }

  @Nested
  class 좌석_증감_테스트 {

    @Test
    void 잔여_좌석을_차감하면_새_객체를_반환한다() {
      SeatSummary original = new SeatSummary(100, 50);

      SeatSummary updated = original.decreaseAvailable(10);

      assertThat(updated).isNotSameAs(original);
      assertThat(updated.getAvailableSeats()).isEqualTo(40);
      assertThat(original.getAvailableSeats()).isEqualTo(50);
    }

    @Test
    void 잔여_좌석_차감_시_0_미만이_되지_않는다() {
      SeatSummary seatSummary = new SeatSummary(100, 5);

      SeatSummary updated = seatSummary.decreaseAvailable(10);

      assertThat(updated.getAvailableSeats()).isEqualTo(0);
    }

    @Test
    void 잔여_좌석을_증가하면_새_객체를_반환한다() {
      SeatSummary original = new SeatSummary(100, 50);

      SeatSummary updated = original.increaseAvailable(10);

      assertThat(updated).isNotSameAs(original);
      assertThat(updated.getAvailableSeats()).isEqualTo(60);
      assertThat(original.getAvailableSeats()).isEqualTo(50);
    }

    @Test
    void 잔여_좌석을_증가_시_총_좌석수를_초과하지_않는다() {
      SeatSummary seatSummary = new SeatSummary(100, 95);

      SeatSummary updated = seatSummary.increaseAvailable(10);

      assertThat(updated.getAvailableSeats()).isEqualTo(100);
    }

    @Nested
    class 동등성_테스트 {

      @Test
      void 같은_값을_가진_SeatSummary는_동등하다() {
        SeatSummary summary1 = new SeatSummary(100, 50);
        SeatSummary summary2 = new SeatSummary(100, 50);

        assertThat(summary1.getTotalSeats()).isEqualTo(summary2.getTotalSeats());
        assertThat(summary1.getAvailableSeats()).isEqualTo(summary2.getAvailableSeats());
      }
    }
  }
}