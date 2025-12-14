package com.tickatch.product_service.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.repository.dto.ProductResponse;
import com.tickatch.product_service.product.domain.repository.dto.ProductSearchCondition;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductQueryService 테스트")
class ProductQueryServiceTest {

  private static final String DEFAULT_SELLER_ID = "seller-001";
  private static final String DEFAULT_PRODUCT_NAME = "테스트 공연";
  private static final ProductType DEFAULT_PRODUCT_TYPE = ProductType.CONCERT;
  private static final int DEFAULT_RUNNING_TIME = 120;
  private static final Long DEFAULT_STAGE_ID = 1L;
  private static final String DEFAULT_STAGE_NAME = "올림픽홀";
  private static final Long DEFAULT_ART_HALL_ID = 100L;
  private static final String DEFAULT_ART_HALL_NAME = "올림픽공원";
  private static final String DEFAULT_ART_HALL_ADDRESS = "서울시 송파구";

  @InjectMocks private ProductQueryService productQueryService;

  @Mock private ProductRepository productRepository;

  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private LocalDateTime saleStartAt;
  private LocalDateTime saleEndAt;

  @BeforeEach
  void setUp() {
    startAt = LocalDateTime.now().plusDays(30);
    endAt = LocalDateTime.now().plusDays(31);
    saleStartAt = LocalDateTime.now().plusDays(1);
    saleEndAt = LocalDateTime.now().plusDays(29);
  }

  @Nested
  class 상품정보_조회_테스트 {

    @Test
    void ID로_상품을_조회할_수_있다() {
      Product product = createProduct(1L, DEFAULT_PRODUCT_NAME);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.getId()).isEqualTo(1L);
      assertThat(response.getSellerId()).isEqualTo(DEFAULT_SELLER_ID);
      assertThat(response.getName()).isEqualTo(DEFAULT_PRODUCT_NAME);
      assertThat(response.getProductType()).isEqualTo(DEFAULT_PRODUCT_TYPE);
      assertThat(response.getRunningTime()).isEqualTo(DEFAULT_RUNNING_TIME);
      assertThat(response.getStageId()).isEqualTo(DEFAULT_STAGE_ID);
      assertThat(response.getStageName()).isEqualTo(DEFAULT_STAGE_NAME);
      assertThat(response.getArtHallId()).isEqualTo(DEFAULT_ART_HALL_ID);
      assertThat(response.getArtHallName()).isEqualTo(DEFAULT_ART_HALL_NAME);
      assertThat(response.getArtHallAddress()).isEqualTo(DEFAULT_ART_HALL_ADDRESS);
      assertThat(response.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void startAt과_endAt이_정확히_매핑된다() {
      Product product = createProduct(1L, DEFAULT_PRODUCT_NAME);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.getStartAt()).isEqualTo(startAt);
      assertThat(response.getEndAt()).isEqualTo(endAt);
    }

    @Test
    void saleStartAt과_saleEndAt이_정확히_매핑된다() {
      Product product = createProduct(1L, DEFAULT_PRODUCT_NAME);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.getSaleStartAt()).isEqualTo(saleStartAt);
      assertThat(response.getSaleEndAt()).isEqualTo(saleEndAt);
    }

    @Test
    void 좌석_현황이_정확히_매핑된다() {
      Product product = createProductWithSeatGrade(1L, DEFAULT_PRODUCT_NAME);
      product.decreaseAvailableSeats(10);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.getTotalSeats()).isEqualTo(30);
      assertThat(response.getAvailableSeats()).isEqualTo(20);
      assertThat(response.isSoldOut()).isFalse();
    }

    @Test
    void 통계가_정확히_매핑된다() {
      Product product = createProduct(1L, DEFAULT_PRODUCT_NAME);
      product.syncViewCount(1000L);
      product.incrementReservationCount();
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.getViewCount()).isEqualTo(1000L);
      assertThat(response.getReservationCount()).isEqualTo(1);
    }

    @Test
    void 반려_사유가_정확히_매핑된다() {
      Product product = createProduct(1L, DEFAULT_PRODUCT_NAME);
      product.changeStatus(ProductStatus.PENDING);
      product.reject("내용 부족");
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.getStatus()).isEqualTo(ProductStatus.REJECTED);
      assertThat(response.getRejectionReason()).isEqualTo("내용 부족");
    }

    @Test
    void 구매_가능_여부가_정확히_매핑된다() {
      Product product = createOnSaleProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.isPurchasable()).isTrue();
    }

    @Test
    void 판매중이_아니면_구매_불가능하다() {
      Product product = createProduct(1L, DEFAULT_PRODUCT_NAME);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.isPurchasable()).isFalse();
    }

    @Test
    void 존재하지_않는_ID로_조회하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productQueryService.getProduct(999L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 예외_발생_시_productId가_errorArgs에_포함된다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productQueryService.getProduct(999L))
          .isInstanceOf(ProductException.class)
          .satisfies(
              e -> {
                ProductException pe = (ProductException) e;
                assertThat(pe.getErrorArgs()).containsExactly(999L);
              });
    }
  }

  @Nested
  class 상품목록_조회_테스트 {

    @Test
    void 조건에_맞는_상품_목록을_페이징_조회할_수_있다() {
      List<Product> products = List.of(createProduct(1L, "콘서트A"), createProduct(2L, "콘서트B"));
      Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 2);
      ProductSearchCondition condition = ProductSearchCondition.builder().build();
      Pageable pageable = PageRequest.of(0, 10);

      given(productRepository.findAllByCondition(condition, pageable)).willReturn(productPage);

      Page<ProductResponse> result = productQueryService.getProducts(condition, pageable);

      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getTotalElements()).isEqualTo(2);
      assertThat(result.getContent().get(0).getName()).isEqualTo("콘서트A");
      assertThat(result.getContent().get(1).getName()).isEqualTo("콘서트B");
    }

    @Test
    void 검색_결과가_없으면_빈_페이지를_반환한다() {
      Page<Product> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
      ProductSearchCondition condition = ProductSearchCondition.builder().name("존재하지않는상품").build();
      Pageable pageable = PageRequest.of(0, 10);

      given(productRepository.findAllByCondition(condition, pageable)).willReturn(emptyPage);

      Page<ProductResponse> result = productQueryService.getProducts(condition, pageable);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void 페이지_정보가_정확히_반환된다() {
      List<Product> products = List.of(createProduct(1L, "콘서트A"), createProduct(2L, "콘서트B"));
      Page<Product> productPage = new PageImpl<>(products, PageRequest.of(1, 2), 10);
      ProductSearchCondition condition = ProductSearchCondition.builder().build();
      Pageable pageable = PageRequest.of(1, 2);

      given(productRepository.findAllByCondition(condition, pageable)).willReturn(productPage);

      Page<ProductResponse> result = productQueryService.getProducts(condition, pageable);

      assertThat(result.getNumber()).isEqualTo(1);
      assertThat(result.getSize()).isEqualTo(2);
      assertThat(result.getTotalElements()).isEqualTo(10);
      assertThat(result.getTotalPages()).isEqualTo(5);
    }
  }

  // ========== Helper Methods ==========

  private Product createProduct(Long id, String name) {
    Schedule schedule = new Schedule(startAt, endAt);
    SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);
    Venue venue =
        new Venue(
            DEFAULT_STAGE_ID,
            DEFAULT_STAGE_NAME,
            DEFAULT_ART_HALL_ID,
            DEFAULT_ART_HALL_NAME,
            DEFAULT_ART_HALL_ADDRESS);

    Product product =
        Product.create(
            DEFAULT_SELLER_ID,
            name,
            DEFAULT_PRODUCT_TYPE,
            DEFAULT_RUNNING_TIME,
            schedule,
            saleSchedule,
            venue,
            ProductContent.empty(),
            AgeRestriction.defaultRestriction(),
            BookingPolicy.defaultPolicy(),
            AdmissionPolicy.defaultPolicy(),
            RefundPolicy.defaultPolicy());
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  private Product createProductWithSeatGrade(Long id, String name) {
    Product product = createProduct(id, name);
    product.addSeatGrade("VIP", 150000L, 10, 1);
    product.addSeatGrade("R", 120000L, 20, 2);
    return product;
  }

  private Product createOnSaleProduct(Long id) {
    // 현재 판매 기간 중인 상품 생성
    LocalDateTime now = LocalDateTime.now();
    Schedule schedule = new Schedule(now.plusDays(30), now.plusDays(31));
    SaleSchedule saleSchedule = new SaleSchedule(now.minusDays(1), now.plusDays(10));
    Venue venue =
        new Venue(
            DEFAULT_STAGE_ID,
            DEFAULT_STAGE_NAME,
            DEFAULT_ART_HALL_ID,
            DEFAULT_ART_HALL_NAME,
            DEFAULT_ART_HALL_ADDRESS);

    Product product =
        Product.create(
            DEFAULT_SELLER_ID,
            DEFAULT_PRODUCT_NAME,
            DEFAULT_PRODUCT_TYPE,
            DEFAULT_RUNNING_TIME,
            schedule,
            saleSchedule,
            venue,
            ProductContent.empty(),
            AgeRestriction.defaultRestriction(),
            BookingPolicy.defaultPolicy(),
            AdmissionPolicy.defaultPolicy(),
            RefundPolicy.defaultPolicy());
    ReflectionTestUtils.setField(product, "id", id);

    // 좌석 초기화 (canPurchase 조건 충족 필요)
    product.addSeatGrade("R", 100000L, 100, 1);

    // ON_SALE 상태로 전이
    product.changeStatus(ProductStatus.PENDING);
    product.approve();
    product.changeStatus(ProductStatus.SCHEDULED);
    product.changeStatus(ProductStatus.ON_SALE);

    return product;
  }
}
