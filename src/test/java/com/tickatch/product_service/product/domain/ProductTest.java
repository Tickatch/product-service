package com.tickatch.product_service.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.SaleSchedule;
import com.tickatch.product_service.product.domain.vo.Schedule;
import com.tickatch.product_service.product.domain.vo.Venue;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Product 도메인 테스트")
class ProductTest {

  private static final String DEFAULT_SELLER_ID = "seller-001";
  private static final String DEFAULT_PRODUCT_NAME = "테스트 공연";
  private static final ProductType DEFAULT_PRODUCT_TYPE = ProductType.CONCERT;
  private static final int DEFAULT_RUNNING_TIME = 120;

  private Schedule futureSchedule;
  private Schedule startedSchedule;
  private SaleSchedule futureSaleSchedule;
  private SaleSchedule startedSaleSchedule;
  private Venue defaultVenue;

  @BeforeEach
  void setUp() {
    futureSchedule =
        new Schedule(LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(31));

    startedSchedule =
        new Schedule(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

    futureSaleSchedule =
        new SaleSchedule(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(29));

    // 이미 시작된 행사용 예매 일정 (이미 종료된 예매)
    startedSaleSchedule =
        new SaleSchedule(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(2));

    defaultVenue = new Venue(1L, "올림픽홀", 100L, "올림픽공원", "서울시 송파구");
  }

  @Nested
  class Product_생성_테스트 {

    @Test
    void 유효한_정보로_상품을_생성할_수_있다() {
      Product product =
          Product.create(
              DEFAULT_SELLER_ID,
              DEFAULT_PRODUCT_NAME,
              DEFAULT_PRODUCT_TYPE,
              DEFAULT_RUNNING_TIME,
              futureSchedule,
              futureSaleSchedule,
              defaultVenue);

      assertThat(product.getSellerId()).isEqualTo(DEFAULT_SELLER_ID);
      assertThat(product.getName()).isEqualTo(DEFAULT_PRODUCT_NAME);
      assertThat(product.getProductType()).isEqualTo(DEFAULT_PRODUCT_TYPE);
      assertThat(product.getRunningTime()).isEqualTo(DEFAULT_RUNNING_TIME);
      assertThat(product.getSchedule()).isEqualTo(futureSchedule);
      assertThat(product.getSaleSchedule()).isEqualTo(futureSaleSchedule);
      assertThat(product.getVenue()).isEqualTo(defaultVenue);
      assertThat(product.getStageId()).isEqualTo(1L);
      assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void 생성_시_상태는_DRAFT이다() {
      Product product = createDefaultProduct();

      assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
      assertThat(product.isDraft()).isTrue();
    }

    @Test
    void 생성_시_좌석현황은_빈_상태로_초기화된다() {
      Product product = createDefaultProduct();

      assertThat(product.getSeatSummary().getTotalSeats()).isEqualTo(0);
      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(0);
    }

    @Test
    void 생성_시_통계는_빈_상태로_초기화된다() {
      Product product = createDefaultProduct();

      assertThat(product.getStats().getViewCount()).isEqualTo(0L);
      assertThat(product.getStats().getReservationCount()).isEqualTo(0);
    }

    @Nested
    class 일정_조회_테스트 {

      @Test
      void getStartAt은_스케줄의_시작시간을_반환한다() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(30);
        LocalDateTime endAt = LocalDateTime.now().plusDays(31);
        Schedule schedule = new Schedule(startAt, endAt);

        Product product =
            Product.create(
                DEFAULT_SELLER_ID,
                DEFAULT_PRODUCT_NAME,
                DEFAULT_PRODUCT_TYPE,
                DEFAULT_RUNNING_TIME,
                schedule,
                futureSaleSchedule,
                defaultVenue);

        assertThat(product.getStartAt()).isEqualTo(startAt);
      }

      @Test
      void getEndAt은_스케줄의_종료시간을_반환한다() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(30);
        LocalDateTime endAt = LocalDateTime.now().plusDays(31);
        Schedule schedule = new Schedule(startAt, endAt);

        Product product =
            Product.create(
                DEFAULT_SELLER_ID,
                DEFAULT_PRODUCT_NAME,
                DEFAULT_PRODUCT_TYPE,
                DEFAULT_RUNNING_TIME,
                schedule,
                futureSaleSchedule,
                defaultVenue);

        assertThat(product.getEndAt()).isEqualTo(endAt);
      }

      @Test
      void getSaleStartAt은_예매_시작시간을_반환한다() {
        Product product = createDefaultProduct();

        assertThat(product.getSaleStartAt()).isEqualTo(futureSaleSchedule.getSaleStartAt());
      }

      @Test
      void getSaleEndAt은_예매_종료시간을_반환한다() {
        Product product = createDefaultProduct();

        assertThat(product.getSaleEndAt()).isEqualTo(futureSaleSchedule.getSaleEndAt());
      }
    }

    @Nested
    class 판매자_ID_검증_테스트 {

      @Test
      void 판매자_ID가_null이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        null,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_SELLER_ID);
      }

      @Test
      void 판매자_ID가_빈_문자열이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        "",
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_SELLER_ID);
      }

      @Test
      void 판매자_ID가_공백만_있으면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        "   ",
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_SELLER_ID);
      }
    }

    @Nested
    class 상품명_검증_테스트 {

      @Test
      void 상품명이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        null,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
      }

      @Test
      void 상품명이_빈_문자열이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        "",
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
      }

      @Test
      void 상품명이_공백만_있으면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        "   ",
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
      }

      @Test
      void 상품명이_50자를_초과하면_ProductException이_발생한다() {
        String longName = "a".repeat(51);

        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        longName,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
      }

      @Test
      void 상품명이_정확히_50자이면_생성에_성공한다() {
        String exactName = "a".repeat(50);

        Product product =
            Product.create(
                DEFAULT_SELLER_ID,
                exactName,
                DEFAULT_PRODUCT_TYPE,
                DEFAULT_RUNNING_TIME,
                futureSchedule,
                futureSaleSchedule,
                defaultVenue);

        assertThat(product.getName()).isEqualTo(exactName);
      }
    }

    @Nested
    class 상품_타입_검증_테스트 {

      @Test
      void 상품_타입이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        null,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_TYPE);
      }
    }

    @Nested
    class 상영_시간_검증_테스트 {

      @Test
      void 상영_시간이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        null,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_RUNNING_TIME);
      }

      @Test
      void 상영_시간이_0이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        0,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_RUNNING_TIME);
      }

      @Test
      void 상영_시간이_음수이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        -1,
                        futureSchedule,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_RUNNING_TIME);
      }
    }

    @Nested
    class 일정_검증_테스트 {

      @Test
      void 일정이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        null,
                        futureSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_SCHEDULE);
      }

      @Test
      void 예매_일정이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        null,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_SALE_SCHEDULE);
      }

      @Test
      void 예매_시작일이_행사_시작일_이후이면_예외가_발생한다() {
        Schedule eventSchedule =
            new Schedule(LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(11));
        SaleSchedule invalidSaleSchedule =
            new SaleSchedule(LocalDateTime.now().plusDays(15), LocalDateTime.now().plusDays(20));

        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        eventSchedule,
                        invalidSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.SALE_MUST_START_BEFORE_EVENT);
      }

      @Test
      void 예매_시작일이_행사_시작일과_같으면_예외가_발생한다() {
        LocalDateTime eventStart = LocalDateTime.now().plusDays(10);
        Schedule eventSchedule = new Schedule(eventStart, eventStart.plusDays(1));
        SaleSchedule invalidSaleSchedule = new SaleSchedule(eventStart, eventStart.plusHours(12));

        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        eventSchedule,
                        invalidSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.SALE_MUST_START_BEFORE_EVENT);
      }

      @Test
      void 예매_종료일이_행사_시작일_이후이면_예외가_발생한다() {
        Schedule eventSchedule =
            new Schedule(LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(11));
        SaleSchedule invalidSaleSchedule =
            new SaleSchedule(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(15));

        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        eventSchedule,
                        invalidSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.SALE_MUST_END_BEFORE_EVENT);
      }

      @Test
      void 예매_종료일이_행사_시작일과_같으면_예외가_발생한다() {
        LocalDateTime eventStart = LocalDateTime.now().plusDays(10);
        Schedule eventSchedule = new Schedule(eventStart, eventStart.plusDays(1));
        SaleSchedule invalidSaleSchedule =
            new SaleSchedule(LocalDateTime.now().plusDays(1), eventStart);

        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        eventSchedule,
                        invalidSaleSchedule,
                        defaultVenue))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.SALE_MUST_END_BEFORE_EVENT);
      }
    }

    @Nested
    class 장소_검증_테스트 {

      @Test
      void 장소가_null이면_ProductException이_발생한다() {
        assertThatThrownBy(
                () ->
                    Product.create(
                        DEFAULT_SELLER_ID,
                        DEFAULT_PRODUCT_NAME,
                        DEFAULT_PRODUCT_TYPE,
                        DEFAULT_RUNNING_TIME,
                        futureSchedule,
                        futureSaleSchedule,
                        null))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_VENUE);
      }
    }
  }

  @Nested
  class 상품_수정_테스트 {

    @Test
    void 유효한_정보로_상품을_수정할_수_있다() {
      Product product = createDefaultProduct();
      Schedule newSchedule =
          new Schedule(LocalDateTime.now().plusDays(40), LocalDateTime.now().plusDays(41));
      SaleSchedule newSaleSchedule =
          new SaleSchedule(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(39));

      product.update("수정된 공연", ProductType.MUSICAL, 150, newSchedule, newSaleSchedule);

      assertThat(product.getName()).isEqualTo("수정된 공연");
      assertThat(product.getProductType()).isEqualTo(ProductType.MUSICAL);
      assertThat(product.getRunningTime()).isEqualTo(150);
      assertThat(product.getSchedule()).isEqualTo(newSchedule);
      assertThat(product.getSaleSchedule()).isEqualTo(newSaleSchedule);
    }

    @Test
    void REJECTED_상태에서도_수정할_수_있다() {
      Product product = createRejectedProduct();
      Schedule newSchedule =
          new Schedule(LocalDateTime.now().plusDays(40), LocalDateTime.now().plusDays(41));
      SaleSchedule newSaleSchedule =
          new SaleSchedule(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(39));

      product.update("수정된 공연", ProductType.MUSICAL, 150, newSchedule, newSaleSchedule);

      assertThat(product.getName()).isEqualTo("수정된 공연");
    }

    @Test
    void PENDING_상태에서는_수정할_수_없다() {
      Product product = createDefaultProduct();
      product.changeStatus(ProductStatus.PENDING);

      assertThatThrownBy(
              () ->
                  product.update(
                      "수정된 공연", ProductType.MUSICAL, 150, futureSchedule, futureSaleSchedule))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_EDITABLE);
    }

    @Test
    void 취소된_상품은_수정할_수_없다() {
      Product product = createDefaultProduct();
      product.cancel("admin");

      assertThatThrownBy(
              () ->
                  product.update(
                      "수정된 공연", ProductType.MUSICAL, 150, futureSchedule, futureSaleSchedule))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_EDITABLE);
    }

    @Test
    void 수정시에도_예매_일정_정합성_검증이_적용된다() {
      Product product = createDefaultProduct();
      Schedule newSchedule =
          new Schedule(LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(11));
      SaleSchedule invalidSaleSchedule =
          new SaleSchedule(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(15));

      assertThatThrownBy(
              () ->
                  product.update(
                      "수정된 공연", ProductType.MUSICAL, 150, newSchedule, invalidSaleSchedule))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.SALE_MUST_END_BEFORE_EVENT);
    }
  }

  @Nested
  class 장소_변경_테스트 {

    @Test
    void 행사_시작_전에는_장소를_변경할_수_있다() {
      Product product = createDefaultProduct();
      Venue newVenue = new Venue(2L, "대공연장", 200L, "세종문화회관", "서울시 종로구");

      product.changeVenue(newVenue);

      assertThat(product.getVenue()).isEqualTo(newVenue);
      assertThat(product.getStageId()).isEqualTo(2L);
    }

    @Test
    void 행사_시작_후에는_장소를_변경할_수_없다() {
      Product product =
          Product.create(
              DEFAULT_SELLER_ID,
              DEFAULT_PRODUCT_NAME,
              DEFAULT_PRODUCT_TYPE,
              DEFAULT_RUNNING_TIME,
              startedSchedule,
              startedSaleSchedule,
              defaultVenue);
      Venue newVenue = new Venue(2L, "대공연장", 200L, "세종문화회관", "서울시 종로구");

      assertThatThrownBy(() -> product.changeVenue(newVenue))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.VENUE_CHANGE_NOT_ALLOWED);
    }

    @Test
    void 취소된_상품의_장소는_변경할_수_없다() {
      Product product = createDefaultProduct();
      product.cancel("admin");
      Venue newVenue = new Venue(2L, "대공연장", 200L, "세종문화회관", "서울시 종로구");

      assertThatThrownBy(() -> product.changeVenue(newVenue))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }

    @Test
    void 장소가_null이면_ProductException이_발생한다() {
      Product product = createDefaultProduct();

      assertThatThrownBy(() -> product.changeVenue(null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_VENUE);
    }
  }

  @Nested
  class 심사_테스트 {

    @Nested
    class 승인_테스트 {

      @Test
      void PENDING_상태에서_승인할_수_있다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        product.approve();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.APPROVED);
        assertThat(product.isApproved()).isTrue();
      }

      @Test
      void DRAFT_상태에서는_승인할_수_없다() {
        Product product = createDefaultProduct();

        assertThatThrownBy(() -> product.approve())
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_NOT_PENDING);
      }

      @Test
      void APPROVED_상태에서는_승인할_수_없다() {
        Product product = createApprovedProduct();

        assertThatThrownBy(() -> product.approve())
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_NOT_PENDING);
      }
    }

    @Nested
    class 반려_테스트 {

      @Test
      void PENDING_상태에서_반려할_수_있다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        product.reject("내용 부족");

        assertThat(product.getStatus()).isEqualTo(ProductStatus.REJECTED);
        assertThat(product.isRejected()).isTrue();
        assertThat(product.getRejectionReason()).isEqualTo("내용 부족");
      }

      @Test
      void DRAFT_상태에서는_반려할_수_없다() {
        Product product = createDefaultProduct();

        assertThatThrownBy(() -> product.reject("내용 부족"))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_NOT_PENDING);
      }

      @Test
      void 반려_사유가_null이면_예외가_발생한다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        assertThatThrownBy(() -> product.reject(null))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_REJECTION_REASON);
      }

      @Test
      void 반려_사유가_빈_문자열이면_예외가_발생한다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        assertThatThrownBy(() -> product.reject(""))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_REJECTION_REASON);
      }
    }

    @Nested
    class 재제출_테스트 {

      @Test
      void REJECTED_상태에서_재제출할_수_있다() {
        Product product = createRejectedProduct();

        product.resubmit();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.isDraft()).isTrue();
        assertThat(product.getRejectionReason()).isNull();
      }

      @Test
      void DRAFT_상태에서는_재제출할_수_없다() {
        Product product = createDefaultProduct();

        assertThatThrownBy(() -> product.resubmit())
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_NOT_REJECTED);
      }

      @Test
      void PENDING_상태에서는_재제출할_수_없다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        assertThatThrownBy(() -> product.resubmit())
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_NOT_REJECTED);
      }
    }
  }

  @Nested
  class 좌석_현황_테스트 {

    @Test
    void 좌석_현황을_초기화할_수_있다() {
      Product product = createDefaultProduct();

      product.initializeSeatSummary(100);

      assertThat(product.getSeatSummary().getTotalSeats()).isEqualTo(100);
      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(100);
    }

    @Test
    void 잔여_좌석을_차감할_수_있다() {
      Product product = createDefaultProduct();
      product.initializeSeatSummary(100);

      product.decreaseAvailableSeats(10);

      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(90);
    }

    @Test
    void 잔여_좌석을_복구할_수_있다() {
      Product product = createDefaultProduct();
      product.initializeSeatSummary(100);
      product.decreaseAvailableSeats(10);

      product.increaseAvailableSeats(5);

      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(95);
    }

    @Test
    void 매진_여부를_확인할_수_있다() {
      Product product = createDefaultProduct();
      product.initializeSeatSummary(10);

      product.decreaseAvailableSeats(10);

      assertThat(product.isSoldOut()).isTrue();
    }

    @Test
    void 잔여_좌석보다_많이_차감하면_예외가_발생한다() {
      Product product = createDefaultProduct();
      product.initializeSeatSummary(10);

      assertThatThrownBy(() -> product.decreaseAvailableSeats(20))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.NOT_ENOUGH_SEATS);
    }
  }

  @Nested
  class 통계_테스트 {

    @Test
    void 조회수를_증가할_수_있다() {
      Product product = createDefaultProduct();

      product.incrementViewCount();

      assertThat(product.getStats().getViewCount()).isEqualTo(1L);
    }

    @Test
    void 조회수를_동기화할_수_있다() {
      Product product = createDefaultProduct();

      product.syncViewCount(1000L);

      assertThat(product.getStats().getViewCount()).isEqualTo(1000L);
    }

    @Test
    void 예매수를_증가할_수_있다() {
      Product product = createDefaultProduct();

      product.incrementReservationCount();

      assertThat(product.getStats().getReservationCount()).isEqualTo(1);
    }

    @Test
    void 예매수를_감소할_수_있다() {
      Product product = createDefaultProduct();
      product.incrementReservationCount();
      product.incrementReservationCount();

      product.decrementReservationCount();

      assertThat(product.getStats().getReservationCount()).isEqualTo(1);
    }
  }

  @Nested
  class 상태_변경_테스트 {

    @Nested
    class DRAFT에서_상태_변경_테스트 {

      @Test
      void DRAFT에서_PENDING으로_변경할_수_있다() {
        Product product = createDefaultProduct();

        product.changeStatus(ProductStatus.PENDING);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
        assertThat(product.isPending()).isTrue();
      }

      @Test
      void DRAFT에서_ON_SALE로_직접_변경할_수_없다() {
        Product product = createDefaultProduct();

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.ON_SALE))
            .isInstanceOf(ProductException.class)
            .satisfies(
                e -> {
                  ProductException pe = (ProductException) e;
                  assertThat(pe.getErrorCode())
                      .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
                  assertThat(pe.getErrorArgs()).containsExactly("DRAFT", "ON_SALE");
                });
      }

      @Test
      void DRAFT에서_APPROVED로_직접_변경할_수_없다() {
        Product product = createDefaultProduct();

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.APPROVED))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }
    }

    @Nested
    class PENDING에서_상태_변경_테스트 {

      @Test
      void PENDING에서_APPROVED로_변경할_수_있다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        product.changeStatus(ProductStatus.APPROVED);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.APPROVED);
        assertThat(product.isApproved()).isTrue();
      }

      @Test
      void PENDING에서_REJECTED로_변경할_수_있다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        product.changeStatus(ProductStatus.REJECTED);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.REJECTED);
        assertThat(product.isRejected()).isTrue();
      }

      @Test
      void PENDING에서_DRAFT로_직접_변경할_수_없다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.DRAFT))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }

      @Test
      void PENDING에서_ON_SALE로_직접_변경할_수_없다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.ON_SALE))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }
    }

    @Nested
    class APPROVED에서_상태_변경_테스트 {

      @Test
      void APPROVED에서_SCHEDULED로_변경할_수_있다() {
        Product product = createApprovedProduct();

        product.changeStatus(ProductStatus.SCHEDULED);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.SCHEDULED);
        assertThat(product.isScheduled()).isTrue();
      }

      @Test
      void APPROVED에서_ON_SALE로_직접_변경할_수_없다() {
        Product product = createApprovedProduct();

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.ON_SALE))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }
    }

    @Nested
    class SCHEDULED에서_상태_변경_테스트 {

      @Test
      void SCHEDULED에서_ON_SALE로_변경할_수_있다() {
        Product product = createScheduledProduct();

        product.changeStatus(ProductStatus.ON_SALE);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(product.isOnSale()).isTrue();
      }

      @Test
      void SCHEDULED에서_CLOSED로_직접_변경할_수_없다() {
        Product product = createScheduledProduct();

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.CLOSED))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }
    }

    @Nested
    class ON_SALE에서_상태_변경_테스트 {

      @Test
      void ON_SALE에서_CLOSED로_변경할_수_있다() {
        Product product = createOnSaleProduct();

        product.changeStatus(ProductStatus.CLOSED);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.CLOSED);
        assertThat(product.isClosed()).isTrue();
      }

      @Test
      void ON_SALE에서_DRAFT로_변경할_수_없다() {
        Product product = createOnSaleProduct();

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.DRAFT))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }

      @Test
      void ON_SALE에서_PENDING으로_변경할_수_없다() {
        Product product = createOnSaleProduct();

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.PENDING))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }
    }

    @Nested
    class CLOSED에서_상태_변경_테스트 {

      @Test
      void CLOSED에서_COMPLETED로_변경할_수_있다() {
        Product product = createClosedProduct();

        product.changeStatus(ProductStatus.COMPLETED);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.COMPLETED);
        assertThat(product.isCompleted()).isTrue();
      }

      @Test
      void CLOSED에서_ON_SALE로_변경할_수_없다() {
        Product product = createClosedProduct();

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.ON_SALE))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }
    }

    @Test
    void 취소된_상품의_상태는_변경할_수_없다() {
      Product product = createDefaultProduct();
      product.cancel("admin");

      assertThatThrownBy(() -> product.changeStatus(ProductStatus.PENDING))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }

    @Test
    void 상태가_null이면_ProductException이_발생한다() {
      Product product = createDefaultProduct();

      assertThatThrownBy(() -> product.changeStatus(null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_PRODUCT_STATUS);
    }
  }

  @Nested
  class 상품_취소_테스트 {

    @Test
    void 상품을_취소할_수_있다() {
      Product product = createDefaultProduct();

      product.cancel("admin");

      assertThat(product.getStatus()).isEqualTo(ProductStatus.CANCELLED);
      assertThat(product.isCancelled()).isTrue();
      assertThat(product.getDeletedAt()).isNotNull();
      assertThat(product.getDeletedBy()).isEqualTo("admin");
    }

    @Test
    void 이미_취소된_상품은_다시_취소할_수_없다() {
      Product product = createDefaultProduct();
      product.cancel("admin");

      assertThatThrownBy(() -> product.cancel("admin"))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }

    @Test
    void PENDING_상태에서_취소할_수_있다() {
      Product product = createDefaultProduct();
      product.changeStatus(ProductStatus.PENDING);

      product.cancel("admin");

      assertThat(product.isCancelled()).isTrue();
    }

    @Test
    void ON_SALE_상태에서_취소할_수_있다() {
      Product product = createOnSaleProduct();

      product.cancel("admin");

      assertThat(product.isCancelled()).isTrue();
    }

    @Test
    void CLOSED_상태에서_취소할_수_있다() {
      Product product = createClosedProduct();

      product.cancel("admin");

      assertThat(product.isCancelled()).isTrue();
    }
  }

  @Nested
  class 상태_확인_메서드_테스트 {

    @Test
    void isDraft메서드는_임시저장_상태에서_true를_반환한다() {
      Product product = createDefaultProduct();

      assertThat(product.isDraft()).isTrue();
    }

    @Test
    void isPending메서드는_심사대기_상태에서_true를_반환한다() {
      Product product = createDefaultProduct();
      product.changeStatus(ProductStatus.PENDING);

      assertThat(product.isPending()).isTrue();
    }

    @Test
    void isApproved메서드는_승인됨_상태에서_true를_반환한다() {
      Product product = createApprovedProduct();

      assertThat(product.isApproved()).isTrue();
    }

    @Test
    void isRejected메서드는_반려됨_상태에서_true를_반환한다() {
      Product product = createRejectedProduct();

      assertThat(product.isRejected()).isTrue();
    }

    @Test
    void isScheduled메서드는_예매예정_상태에서_true를_반환한다() {
      Product product = createScheduledProduct();

      assertThat(product.isScheduled()).isTrue();
    }

    @Test
    void isOnSale메서드는_판매중_상태에서_true를_반환한다() {
      Product product = createOnSaleProduct();

      assertThat(product.isOnSale()).isTrue();
    }

    @Test
    void isClosed메서드는_판매종료_상태에서_true를_반환한다() {
      Product product = createClosedProduct();

      assertThat(product.isClosed()).isTrue();
    }

    @Test
    void isCompleted메서드는_행사종료_상태에서_true를_반환한다() {
      Product product = createCompletedProduct();

      assertThat(product.isCompleted()).isTrue();
    }

    @Test
    void isCancelled메서드는_취소된_상태에서_true를_반환한다() {
      Product product = createDefaultProduct();
      product.cancel("admin");

      assertThat(product.isCancelled()).isTrue();
    }

    @Test
    void isEditable메서드는_DRAFT와_REJECTED에서_true를_반환한다() {
      Product draft = createDefaultProduct();
      Product rejected = createRejectedProduct();
      Product pending = createDefaultProduct();
      pending.changeStatus(ProductStatus.PENDING);

      assertThat(draft.isEditable()).isTrue();
      assertThat(rejected.isEditable()).isTrue();
      assertThat(pending.isEditable()).isFalse();
    }

    @Test
    void isTerminal메서드는_COMPLETED와_CANCELLED에서_true를_반환한다() {
      Product completed = createCompletedProduct();
      Product cancelled = createDefaultProduct();
      cancelled.cancel("admin");
      Product onSale = createOnSaleProduct();

      assertThat(completed.isTerminal()).isTrue();
      assertThat(cancelled.isTerminal()).isTrue();
      assertThat(onSale.isTerminal()).isFalse();
    }
  }

  @Nested
  class 소유자_확인_테스트 {

    @Test
    void isOwnedBy메서드는_소유자이면_true를_반환한다() {
      Product product = createDefaultProduct();

      assertThat(product.isOwnedBy(DEFAULT_SELLER_ID)).isTrue();
    }

    @Test
    void isOwnedBy메서드는_소유자가_아니면_false를_반환한다() {
      Product product = createDefaultProduct();

      assertThat(product.isOwnedBy("other-seller")).isFalse();
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_객체는_동등하다() {
      Product product = createDefaultProduct();

      assertThat(product).isEqualTo(product);
    }

    @Test
    void 다른_타입의_객체와는_동등하지_않다() {
      Product product = createDefaultProduct();

      assertThat(product).isNotEqualTo("not a product");
      assertThat(product).isNotEqualTo(null);
    }
  }

  // ========== Helper Methods ==========

  private Product createDefaultProduct() {
    return Product.create(
        DEFAULT_SELLER_ID,
        DEFAULT_PRODUCT_NAME,
        DEFAULT_PRODUCT_TYPE,
        DEFAULT_RUNNING_TIME,
        futureSchedule,
        futureSaleSchedule,
        defaultVenue);
  }

  private Product createApprovedProduct() {
    Product product = createDefaultProduct();
    product.changeStatus(ProductStatus.PENDING);
    product.approve();
    return product;
  }

  private Product createRejectedProduct() {
    Product product = createDefaultProduct();
    product.changeStatus(ProductStatus.PENDING);
    product.reject("테스트 반려 사유");
    return product;
  }

  private Product createScheduledProduct() {
    Product product = createApprovedProduct();
    product.changeStatus(ProductStatus.SCHEDULED);
    return product;
  }

  private Product createOnSaleProduct() {
    Product product = createScheduledProduct();
    product.changeStatus(ProductStatus.ON_SALE);
    return product;
  }

  private Product createClosedProduct() {
    Product product = createOnSaleProduct();
    product.changeStatus(ProductStatus.CLOSED);
    return product;
  }

  private Product createCompletedProduct() {
    Product product = createClosedProduct();
    product.changeStatus(ProductStatus.COMPLETED);
    return product;
  }
}
