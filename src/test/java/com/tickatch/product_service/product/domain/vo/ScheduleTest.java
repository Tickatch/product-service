package com.tickatch.product_service.product.domain.vo;

import static org.junit.jupiter.api.Assertions.*;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Schedule 테스트")
class ScheduleTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_시작과_종료_일시로_shedule을_생성할_수_있다() {
      LocalDateTime startAt = LocalDateTime.now().plusDays(1);
      LocalDateTime endAt = LocalDateTime.now().plusDays(2);

      Schedule schedule = new Schedule(startAt, endAt);

      assertThat(schedule.startAt()).isEqualTo(startAt);
      assertThat(schedule.endAt()).isEqualTo(endAt);
    }

    @Test
    void 시작_일시가_null이면_ProductExceptin이_발생한다() {
      LocalDateTime endAt = LocalDateTime.now().plusDays(1);

      assertThatThrownBy(() -> new Schedule(null, endAt))
          .isInstanceOf(ProductException.class)
          .extracting(e ->
              ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_SCHEDULE);
    }

    @Test
    void 종료_일시가_null이면_ProductException이_발생한다() {
      LocalDateTime startAt = LocalDateTime.now().plusDays(1);

      assertThatThrownBy(() -> new Schedule(startAt, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_SCHEDULE);
    }

    @Test
    void 종료_일시가_시작_일시_이전이면_ProductException이_발생한다() {
      LocalDateTime startAt = LocalDateTime.now().plusDays(2);
      LocalDateTime endAt = LocalDateTime.now().plusDays(1);

      assertThatThrownBy(() -> new Schedule(startAt, endAt))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_SCHEDULE);
    }

    @Test
    void 종료_일시가_시작_일시와_같으면_ProductException이_발생한다() {
      LocalDateTime sameTime = LocalDateTime.now().plusDays(1);

      assertThatThrownBy(() -> new Schedule(sameTime, sameTime))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_SCHEDULE);
    }
  }

  @Nested
  class 일정_시작_여부_테스트 {

    @Test
    void 시작_일시가_지났으면_true를_반환한다() {
      Schedule schedule = new Schedule(
          LocalDateTime.now().minusDays(1),
          LocalDateTime.now().plusDays(1)
      );

      assertThat(schedule.isStarted()).isTrue();
    }

    @Test
    void 시작_일시가_지나지_않았으면_false를_반환한다() {
      Schedule schedule = new Schedule(
          LocalDateTime.now().plusDays(1),
          LocalDateTime.now().plusDays(2)
      );

      assertThat(schedule.isStarted()).isFalse();
    }
  }

  @Nested
  class 일정_종료_여부_테스트 {

    @Test
    void 종료_일시가_지났으면_true를_반환한다() {
      Schedule schedule = new Schedule(
          LocalDateTime.now().minusDays(2),
          LocalDateTime.now().minusDays(1)
      );

      assertThat(schedule.isEnded()).isTrue();
    }

    @Test
    void 종료_일시가_지나지_않았으면_false를_반환한다() {
      Schedule schedule = new Schedule(
          LocalDateTime.now().minusDays(1),
          LocalDateTime.now().plusDays(1)
      );

      assertThat(schedule.isEnded()).isFalse();
    }
  }

  @Nested
  class 진행_중_여부_테스트 {

    @Test
    void 현재_시각이_시작과_종료_사이면_true를_반환한다() {
      Schedule schedule = new Schedule(
          LocalDateTime.now().minusHours(1),
          LocalDateTime.now().plusHours(1)
      );

      assertThat(schedule.isOngoing()).isTrue();
    }

    @Test
    void 아직_시작_전이면_false를_반환한다() {
      Schedule schedule = new Schedule(
          LocalDateTime.now().plusDays(1),
          LocalDateTime.now().plusDays(2)
      );

      assertThat(schedule.isOngoing()).isFalse();
    }

    @Test
    void 이미_종료되었으면_false를_반환한다() {
      Schedule schedule = new Schedule(
          LocalDateTime.now().minusDays(2),
          LocalDateTime.now().minusDays(1)
      );

      assertThat(schedule.isOngoing()).isFalse();
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_시작과_종료_일시를_가진_schedule은_동등하다() {
      LocalDateTime startAt = LocalDateTime.of(
          2025, 6, 1, 10, 0);
      LocalDateTime endAt = LocalDateTime.of(
          2025, 6, 1, 12, 0);

      Schedule schedule1 = new Schedule(startAt, endAt);
      Schedule schedule2 = new Schedule(startAt, endAt);

      assertThat(schedule1).isEqualTo(schedule2);
      assertThat(schedule1.hashCode()).isEqualTo(schedule2.hashCode());
    }

    @Test
    void 다른_시작_일시를_가진_schedule은_동등하지_않다() {
      LocalDateTime endAt = LocalDateTime.of(
          2025, 6, 1, 12, 0);

      Schedule schedule1 = new Schedule(LocalDateTime.of(
          2025, 6, 1, 10, 0), endAt);
      Schedule schedule2 = new Schedule(LocalDateTime.of(
          2025, 6, 1, 11, 0), endAt);

      assertThat(schedule1).isNotEqualTo(schedule2);
    }
  }
}