package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SaleSchedule 테스트")
class SaleScheduleTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_판매_일정을_생성한다() {
      LocalDateTime saleStartAt = LocalDateTime.of(2025, 1, 1, 10, 0);
      LocalDateTime saleEndAt = LocalDateTime.of(2025, 1, 10, 18, 0);

      SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);

      assertThat(saleSchedule.getSaleStartAt()).isEqualTo(saleStartAt);
      assertThat(saleSchedule.getSaleEndAt()).isEqualTo(saleEndAt);
    }

    @Test
    void 시작_일시가_null이면_예외를_던진다() {
      LocalDateTime saleStartAt = null;
      LocalDateTime saleEndAt = LocalDateTime.of(2025, 1, 10, 18, 0);

      assertThatThrownBy(() -> new SaleSchedule(saleStartAt, saleEndAt))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void 종료_일시가_null이면_예외를_던진다() {
      LocalDateTime saleStartAt = LocalDateTime.of(2025, 1, 1, 10, 0);
      LocalDateTime saleEndAt = null;

      assertThatThrownBy(() -> new SaleSchedule(saleStartAt, saleEndAt))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void 종료_일시가_시작_일시보다_이전이면_예외를_던진다() {
      LocalDateTime saleStartAt = LocalDateTime.of(2025, 1, 10, 10, 0);
      LocalDateTime saleEndAt = LocalDateTime.of(2025, 1, 1, 18, 0);

      assertThatThrownBy(() -> new SaleSchedule(saleStartAt, saleEndAt))
          .isInstanceOf(ProductException.class);
    }

    @Test
    void 종료_일시가_시작_일시와_같으면_예외를_던진다() {
      LocalDateTime sameDateTime = LocalDateTime.of(2025, 1, 1, 10, 0);

      assertThatThrownBy(() -> new SaleSchedule(sameDateTime, sameDateTime))
          .isInstanceOf(ProductException.class);
    }
  }

  @Nested
  class 상태_확인_테스트 {

    @Test
    void 예매_시작_전이면_isBeforSaleStart가_true를_반환한다() {
      LocalDateTime saleStartAt = LocalDateTime.now().plusDays(1);
      LocalDateTime saleEndAt = LocalDateTime.now().plusDays(10);
      SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);

      assertThat(saleSchedule.isBeforeSaleStart()).isTrue();
      assertThat(saleSchedule.isSaleStarted()).isFalse();
    }

    @Test
    void 예매_기간_중이면_isInSalePeriod가_true를_반환한다() {
      LocalDateTime saleStartAt = LocalDateTime.now().minusDays(1);
      LocalDateTime saleEndAt = LocalDateTime.now().plusDays(10);
      SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);

      assertThat(saleSchedule.isInSalePeriod()).isTrue();
      assertThat(saleSchedule.isSaleStarted()).isTrue();
      assertThat(saleSchedule.isSaleEnded()).isFalse();
    }

    @Test
    void 예매_종료_후이면_isSaleEnded가_true를_반환한다() {
      LocalDateTime saleStartAt = LocalDateTime.now().minusDays(10);
      LocalDateTime saleEndAt = LocalDateTime.now().minusDays(1);
      SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);

      assertThat(saleSchedule.isSaleEnded()).isTrue();
      assertThat(saleSchedule.isInSalePeriod()).isFalse();
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값을_가진_SaleSchedule은_동등하다() {
      LocalDateTime saleStartAt = LocalDateTime.of(2025, 1, 1, 10, 0);
      LocalDateTime saleEndAt = LocalDateTime.of(2025, 1, 10, 18, 0);

      SaleSchedule schedule1 = new SaleSchedule(saleStartAt, saleEndAt);
      SaleSchedule schedule2 = new SaleSchedule(saleStartAt, saleEndAt);

      assertThat(schedule1).isEqualTo(schedule2);
      assertThat(schedule1.hashCode()).isEqualTo(schedule2.hashCode());
    }
  }
}