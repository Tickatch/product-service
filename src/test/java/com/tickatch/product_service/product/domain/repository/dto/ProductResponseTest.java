package com.tickatch.product_service.product.domain.repository.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.Schedule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductResponse 테스트")
class ProductResponseTest {

  @Test
  void Product로부터_ProductResponse를_생성할_수_있다() {
    LocalDateTime startAt = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime endAt = LocalDateTime.of(2025, 1, 1, 12, 0);
    Schedule schedule = new Schedule(startAt, endAt);
    Product product = Product.create("테스트 공연", ProductType.CONCERT, 120, schedule, 1L);

    ProductResponse response = ProductResponse.from(product);

    assertThat(response.getName()).isEqualTo("테스트 공연");
    assertThat(response.getProductType()).isEqualTo(ProductType.CONCERT);
    assertThat(response.getRunningTime()).isEqualTo(120);
    assertThat(response.getStartAt()).isEqualTo(startAt);
    assertThat(response.getEndAt()).isEqualTo(endAt);
    assertThat(response.getStageId()).isEqualTo(1L);
    assertThat(response.getStatus()).isEqualTo(ProductStatus.DRAFT);
  }

  @Test
  void startAt과_endAt이_Product의_일정과_일치한다() {
    LocalDateTime startAt = LocalDateTime.of(2025, 6, 15, 19, 0);
    LocalDateTime endAt = LocalDateTime.of(2025, 6, 15, 21, 30);
    Schedule schedule = new Schedule(startAt, endAt);
    Product product = Product.create("뮤지컬", ProductType.MUSICAL, 150, schedule, 2L);

    ProductResponse response = ProductResponse.from(product);

    assertThat(response.getStartAt()).isEqualTo(product.getStartAt());
    assertThat(response.getEndAt()).isEqualTo(product.getEndAt());
  }
}
