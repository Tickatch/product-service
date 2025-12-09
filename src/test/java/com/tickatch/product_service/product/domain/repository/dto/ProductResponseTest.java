package com.tickatch.product_service.product.domain.repository.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.vo.AdmissionPolicy;
import com.tickatch.product_service.product.domain.vo.AgeRestriction;
import com.tickatch.product_service.product.domain.vo.BookingPolicy;
import com.tickatch.product_service.product.domain.vo.ProductContent;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.RefundPolicy;
import com.tickatch.product_service.product.domain.vo.SaleSchedule;
import com.tickatch.product_service.product.domain.vo.Schedule;
import com.tickatch.product_service.product.domain.vo.Venue;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductResponse 테스트")
class ProductResponseTest {

  private static final String SELLER_ID = "seller-001";
  private static final Long STAGE_ID = 1L;
  private static final String STAGE_NAME = "올림픽홀";
  private static final Long ART_HALL_ID = 100L;
  private static final String ART_HALL_NAME = "올림픽공원";
  private static final String ART_HALL_ADDRESS = "서울시 송파구";

  @Test
  void Product로부터_ProductResponse를_생성할_수_있다() {
    LocalDateTime startAt = LocalDateTime.of(2025, 3, 1, 19, 0);
    LocalDateTime endAt = LocalDateTime.of(2025, 3, 1, 21, 0);
    LocalDateTime saleStartAt = LocalDateTime.of(2025, 2, 1, 10, 0);
    LocalDateTime saleEndAt = LocalDateTime.of(2025, 2, 28, 23, 59);

    Schedule schedule = new Schedule(startAt, endAt);
    SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);
    Venue venue = new Venue(STAGE_ID, STAGE_NAME, ART_HALL_ID, ART_HALL_NAME, ART_HALL_ADDRESS);

    Product product =
        Product.create(
            SELLER_ID,
            "테스트 공연",
            ProductType.CONCERT,
            120,
            schedule,
            saleSchedule,
            venue,
            ProductContent.empty(),
            AgeRestriction.defaultRestriction(),
            BookingPolicy.defaultPolicy(),
            AdmissionPolicy.defaultPolicy(),
            RefundPolicy.defaultPolicy());

    ProductResponse response = ProductResponse.from(product);

    // 기본 정보
    assertThat(response.getSellerId()).isEqualTo(SELLER_ID);
    assertThat(response.getName()).isEqualTo("테스트 공연");
    assertThat(response.getProductType()).isEqualTo(ProductType.CONCERT);
    assertThat(response.getRunningTime()).isEqualTo(120);
    assertThat(response.getStatus()).isEqualTo(ProductStatus.DRAFT);

    // 행사 일정
    assertThat(response.getStartAt()).isEqualTo(startAt);
    assertThat(response.getEndAt()).isEqualTo(endAt);

    // 예매 일정
    assertThat(response.getSaleStartAt()).isEqualTo(saleStartAt);
    assertThat(response.getSaleEndAt()).isEqualTo(saleEndAt);

    // 장소 정보
    assertThat(response.getStageId()).isEqualTo(STAGE_ID);
    assertThat(response.getStageName()).isEqualTo(STAGE_NAME);
    assertThat(response.getArtHallId()).isEqualTo(ART_HALL_ID);
    assertThat(response.getArtHallName()).isEqualTo(ART_HALL_NAME);
    assertThat(response.getArtHallAddress()).isEqualTo(ART_HALL_ADDRESS);

    // 좌석/통계 (초기값)
    assertThat(response.getTotalSeats()).isZero();
    assertThat(response.getAvailableSeats()).isZero();
    assertThat(response.getViewCount()).isZero();
    assertThat(response.getReservationCount()).isZero();

    // 구매 가능 여부
    assertThat(response.isPurchasable()).isFalse();
  }

  @Test
  void 일정_정보가_Product와_일치한다() {
    LocalDateTime startAt = LocalDateTime.of(2025, 6, 15, 19, 0);
    LocalDateTime endAt = LocalDateTime.of(2025, 6, 15, 21, 30);
    LocalDateTime saleStartAt = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime saleEndAt = LocalDateTime.of(2025, 6, 14, 23, 59);

    Schedule schedule = new Schedule(startAt, endAt);
    SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);
    Venue venue = new Venue(2L, "대공연장", 200L, "세종문화회관", "서울시 종로구");

    Product product =
        Product.create(
            SELLER_ID,
            "뮤지컬",
            ProductType.MUSICAL,
            150,
            schedule,
            saleSchedule,
            venue,
            ProductContent.empty(),
            AgeRestriction.defaultRestriction(),
            BookingPolicy.defaultPolicy(),
            AdmissionPolicy.defaultPolicy(),
            RefundPolicy.defaultPolicy());

    ProductResponse response = ProductResponse.from(product);

    assertThat(response.getStartAt()).isEqualTo(product.getStartAt());
    assertThat(response.getEndAt()).isEqualTo(product.getEndAt());
    assertThat(response.getSaleStartAt()).isEqualTo(product.getSaleStartAt());
    assertThat(response.getSaleEndAt()).isEqualTo(product.getSaleEndAt());
  }

  @Test
  void 좌석_정보가_초기화된_Product의_Response에_반영된다() {
    Product product = createTestProduct();
    product.initializeSeatSummary(100);

    ProductResponse response = ProductResponse.from(product);

    assertThat(response.getTotalSeats()).isEqualTo(100);
    assertThat(response.getAvailableSeats()).isEqualTo(100);
  }

  @Test
  void 좌석_차감이_Response에_반영된다() {
    Product product = createTestProduct();
    product.initializeSeatSummary(100);
    product.decreaseAvailableSeats(30);

    ProductResponse response = ProductResponse.from(product);

    assertThat(response.getTotalSeats()).isEqualTo(100);
    assertThat(response.getAvailableSeats()).isEqualTo(70);
  }

  @Test
  void 통계_정보가_Response에_반영된다() {
    Product product = createTestProduct();
    product.syncViewCount(500L);
    product.incrementReservationCount();
    product.incrementReservationCount();

    ProductResponse response = ProductResponse.from(product);

    assertThat(response.getViewCount()).isEqualTo(500L);
    assertThat(response.getReservationCount()).isEqualTo(2);
  }

  @Test
  void 구매_가능_상태가_Response에_반영된다() {
    // 현재 판매 기간 중인 상품 생성
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime eventStart = now.plusDays(30);
    LocalDateTime eventEnd = eventStart.plusHours(2);
    LocalDateTime saleStart = now.minusDays(1); // 이미 시작됨
    LocalDateTime saleEnd = now.plusDays(10); // 아직 안 끝남

    Schedule schedule = new Schedule(eventStart, eventEnd);
    SaleSchedule saleSchedule = new SaleSchedule(saleStart, saleEnd);
    Venue venue = new Venue(STAGE_ID, STAGE_NAME, ART_HALL_ID, ART_HALL_NAME, ART_HALL_ADDRESS);

    Product product =
        Product.create(
            SELLER_ID,
            "테스트 공연",
            ProductType.CONCERT,
            120,
            schedule,
            saleSchedule,
            venue,
            ProductContent.empty(),
            AgeRestriction.defaultRestriction(),
            BookingPolicy.defaultPolicy(),
            AdmissionPolicy.defaultPolicy(),
            RefundPolicy.defaultPolicy());

    // canPurchase() 조건: ON_SALE + 판매기간 내 + 좌석 있음
    product.initializeSeatSummary(100);
    product.changeStatus(ProductStatus.PENDING);
    product.approve();
    product.changeStatus(ProductStatus.SCHEDULED);
    product.changeStatus(ProductStatus.ON_SALE);

    ProductResponse response = ProductResponse.from(product);

    assertThat(response.isPurchasable()).isTrue();
  }

  // ========== Helper Methods ==========

  private Product createTestProduct() {
    LocalDateTime eventStart = LocalDateTime.now().plusDays(30);
    LocalDateTime eventEnd = eventStart.plusHours(2);
    LocalDateTime saleStart = LocalDateTime.now().plusDays(1);
    LocalDateTime saleEnd = eventStart.minusDays(1);

    Schedule schedule = new Schedule(eventStart, eventEnd);
    SaleSchedule saleSchedule = new SaleSchedule(saleStart, saleEnd);
    Venue venue = new Venue(STAGE_ID, STAGE_NAME, ART_HALL_ID, ART_HALL_NAME, ART_HALL_ADDRESS);

    return Product.create(
        SELLER_ID,
        "테스트 공연",
        ProductType.CONCERT,
        120,
        schedule,
        saleSchedule,
        venue,
        ProductContent.empty(),
        AgeRestriction.defaultRestriction(),
        BookingPolicy.defaultPolicy(),
        AdmissionPolicy.defaultPolicy(),
        RefundPolicy.defaultPolicy());
  }
}
