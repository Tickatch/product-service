package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProductStats 테스트")
class ProductStatsTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 통계_정보를_생성한다() {
      Long viewCount = 1000L;
      Integer reservationCount = 50;

      ProductStats stats = new ProductStats(viewCount, reservationCount);

      assertThat(stats.getViewCount()).isEqualTo(1000L);
      assertThat(stats.getReservationCount()).isEqualTo(50);
    }

    @Test
    void null_값은_0으로_초기화된다() {
      ProductStats stats = new ProductStats(null, null);

      assertThat(stats.getViewCount()).isEqualTo(0L);
      assertThat(stats.getReservationCount()).isEqualTo(0);
    }

    @Test
    void 빈_통계_정보를_생성한다() {
      ProductStats stats = ProductStats.empty();

      assertThat(stats.getViewCount()).isEqualTo(0L);
      assertThat(stats.getReservationCount()).isEqualTo(0);
    }
  }

  @Nested
  class 조회수_테스트 {

    @Test
    void 조회수를_증가하면_새_객체를_반환한다() {
      ProductStats original = new ProductStats(100L, 10);

      ProductStats updated = original.incrementViewCount();

      assertThat(updated).isNotSameAs(original);
      assertThat(updated.getViewCount()).isEqualTo(101L);
      assertThat(original.getViewCount()).isEqualTo(100L);
    }

    @Test
    void 조회수를_동기화하면_새_객체를_반환한다() {
      ProductStats original = new ProductStats(100L, 10);

      ProductStats updated = original.syncViewCount(5000L);

      assertThat(updated).isNotSameAs(original);
      assertThat(updated.getViewCount()).isEqualTo(5000L);
      assertThat(updated.getReservationCount()).isEqualTo(10);
    }
  }

  @Nested
  class 예매수_테스트 {

    @Test
    void 예매수가_증가하면_새_객체를_반환한다() {
      ProductStats original = new ProductStats(100L, 10);

      ProductStats updated = original.incrementReservationCount();

      assertThat(updated).isNotSameAs(original);
      assertThat(updated.getReservationCount()).isEqualTo(11);
      assertThat(original.getReservationCount()).isEqualTo(10);
    }

    @Test
    void 예매수가_감소하면_새_객체를_반환한다() {
      ProductStats original = new ProductStats(100L, 10);

      ProductStats updated = original.decrementReservationCount();

      assertThat(updated).isNotSameAs(original);
      assertThat(updated.getReservationCount()).isEqualTo(9);
      assertThat(original.getReservationCount()).isEqualTo(10);
    }

    @Test
    void 예매수_감소_시_0_미만이_되지_않는다() {
      ProductStats stats = new ProductStats(100L, 0);

      ProductStats updated = stats.decrementReservationCount();

      assertThat(updated.getReservationCount()).isEqualTo(0);
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값을_가진_ProductStats는_동등하다() {
      ProductStats stats1 = new ProductStats(100L, 10);
      ProductStats stats2 = new ProductStats(100L, 10);

      assertThat(stats1).isEqualTo(stats2);
      assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
    }
  }
}
