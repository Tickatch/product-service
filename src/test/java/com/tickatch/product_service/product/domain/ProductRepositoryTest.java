package com.tickatch.product_service.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("ProductRepository 통합 테스트")
class ProductRepositoryTest {

  private static final String DEFAULT_SELLER_ID = "seller-001";
  private static final String OTHER_SELLER_ID = "seller-002";
  private static final int RUNNING_TIME = 120;
  private static final Long STAGE_ID = 1L;
  private static final String STAGE_NAME = "올림픽홀";
  private static final Long ART_HALL_ID = 100L;
  private static final String ART_HALL_NAME = "올림픽공원";
  private static final String ART_HALL_ADDRESS = "서울시 송파구";
  private static final String PRODUCT_NAME = "테스트 공연";
  private static final ProductType PRODUCT_TYPE = ProductType.CONCERT;
  private static final Long FAIL_PRODUCT_ID = 999L;

  @Autowired private ProductRepository productRepository;

  private Schedule futureSchedule;
  private SaleSchedule futureSaleSchedule;
  private Venue defaultVenue;

  @BeforeEach
  void setUp() {
    LocalDateTime eventStart = LocalDateTime.now().plusDays(30);
    LocalDateTime eventEnd = LocalDateTime.now().plusDays(31);
    LocalDateTime saleStart = LocalDateTime.now().plusDays(1);
    LocalDateTime saleEnd = LocalDateTime.now().plusDays(29);

    futureSchedule = new Schedule(eventStart, eventEnd);
    futureSaleSchedule = new SaleSchedule(saleStart, saleEnd);
    defaultVenue = new Venue(STAGE_ID, STAGE_NAME, ART_HALL_ID, ART_HALL_NAME, ART_HALL_ADDRESS);
  }

  @Nested
  class 저장_메서드_테스트 {

    @Test
    void 저장_성공() {
      Product product = createProduct(PRODUCT_NAME, PRODUCT_TYPE);

      Product saved = productRepository.save(product);

      assertThat(saved.getId()).isNotNull();
      assertThat(saved.getName()).isEqualTo(PRODUCT_NAME);
      assertThat(saved.getSellerId()).isEqualTo(DEFAULT_SELLER_ID);
    }
  }

  @Nested
  class 단일_조회_메서드_테스트 {

    @Test
    void 단일_조회_성공할_수_있다() {
      Product product = productRepository.save(createProduct(PRODUCT_NAME, PRODUCT_TYPE));

      Optional<Product> found = productRepository.findById(product.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getName()).isEqualTo(PRODUCT_NAME);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_빈_Optional을_반환한다() {
      Optional<Product> found = productRepository.findById(FAIL_PRODUCT_ID);

      assertThat(found).isEmpty();
    }

    @Test
    void 비관적_락으로_조회할_수_있다() {
      Product product = productRepository.save(createProduct(PRODUCT_NAME, PRODUCT_TYPE));

      Optional<Product> found = productRepository.findByIdForUpdate(product.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getId()).isEqualTo(product.getId());
    }
  }

  @Nested
  class 조건에_따른_목록_조회_메서드_테스트 {

    @BeforeEach
    void 상품들_초기화() {
      productRepository.save(createProduct("콘서트A", ProductType.CONCERT, DEFAULT_SELLER_ID));
      productRepository.save(createProduct("콘서트B", ProductType.CONCERT, DEFAULT_SELLER_ID));
      productRepository.save(createProduct("뮤지컬A", ProductType.MUSICAL, OTHER_SELLER_ID));
      productRepository.save(createProduct("연극A", ProductType.PLAY, OTHER_SELLER_ID));
    }

    @Nested
    class 검색_조건_메서드_테스트 {

      @Test
      void 조건_없이_전체_조회가_가능하다() {
        ProductSearchCondition condition = ProductSearchCondition.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
      }

      @Test
      void 상품명으로_검색이_가능하다() {
        ProductSearchCondition condition = ProductSearchCondition.builder().name("콘서트").build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.getName().contains("콘서트"));
      }

      @Test
      void 상품타입으로_검색이_가능하다() {
        ProductSearchCondition condition =
            ProductSearchCondition.builder().productType(ProductType.MUSICAL).build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductType()).isEqualTo(ProductType.MUSICAL);
      }

      @Test
      void 상태로_검색이_가능하다() {
        Product onSaleProduct = createProduct("판매중 공연", PRODUCT_TYPE);
        productRepository.save(onSaleProduct);
        onSaleProduct.changeStatus(ProductStatus.PENDING);
        onSaleProduct.approve();
        onSaleProduct.changeStatus(ProductStatus.SCHEDULED);
        onSaleProduct.changeStatus(ProductStatus.ON_SALE);

        ProductSearchCondition condition =
            ProductSearchCondition.builder().status(ProductStatus.ON_SALE).build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ProductStatus.ON_SALE);
      }

      @Test
      void 판매자ID로_검색이_가능하다() {
        ProductSearchCondition condition =
            ProductSearchCondition.builder().sellerId(DEFAULT_SELLER_ID).build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.getSellerId().equals(DEFAULT_SELLER_ID));
      }

      @Test
      void 스테이지ID로_검색이_가능하다() {
        ProductSearchCondition condition =
            ProductSearchCondition.builder().stageId(STAGE_ID).build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent()).allMatch(p -> p.getStageId().equals(STAGE_ID));
      }

      @Test
      void 복합_조건으로_검색이_가능하다() {
        ProductSearchCondition condition =
            ProductSearchCondition.builder()
                .name("콘서트")
                .productType(ProductType.CONCERT)
                .status(ProductStatus.DRAFT)
                .sellerId(DEFAULT_SELLER_ID)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(2);
      }

      @Test
      void 삭제된_상품은_조회되지_않는다() {
        Product deletedProduct = createProduct("삭제된 공연", PRODUCT_TYPE);
        productRepository.save(deletedProduct);
        deletedProduct.cancel("admin");
        productRepository.save(deletedProduct);

        ProductSearchCondition condition = ProductSearchCondition.builder().name("삭제된").build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).isEmpty();
      }
    }

    @Nested
    class 페이징_테스트 {

      @Test
      void 페이지_크기에_맞게_조회할_수_있다() {
        ProductSearchCondition condition = ProductSearchCondition.builder().build();
        Pageable pageable = PageRequest.of(0, 2);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
      }

      @Test
      void 두_번째_페이지_조회할_수_있다() {
        ProductSearchCondition condition = ProductSearchCondition.builder().build();
        Pageable pageable = PageRequest.of(1, 2);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(1);
      }
    }

    @Nested
    class 정렬_테스트 {

      @Test
      void 상품명_오름차순_정렬할_수_있다() {
        ProductSearchCondition condition = ProductSearchCondition.builder().build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent().get(0).getName()).isEqualTo("뮤지컬A");
        assertThat(result.getContent().get(1).getName()).isEqualTo("연극A");
      }

      @Test
      void 상품명_내림차순_정렬할_수_있다() {
        ProductSearchCondition condition = ProductSearchCondition.builder().build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent().get(0).getName()).isEqualTo("콘서트B");
      }

      @Test
      void 정렬_조건이_없으면_생성일_내림차순으로_정렬된다() {
        ProductSearchCondition condition = ProductSearchCondition.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> result = productRepository.findAllByCondition(condition, pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().getFirst().getName()).isEqualTo("연극A");
        // 기본 정렬: createdAt DESC (마지막에 생성된 것이 첫 번째)
      }
    }
  }

  @Nested
  @DisplayName("좌석 등급 제거 테스트 (JPA 환경)")
  class 좌석_등급_제거_테스트 {

    @Test
    void DRAFT_상태에서_좌석_등급을_제거할_수_있다() {
      // given
      Product product = productRepository.save(createProduct(PRODUCT_NAME, PRODUCT_TYPE));
      SeatGrade vip = product.addSeatGrade("VIP", 150000L, 100, 1);
      product.addSeatGrade("R석", 120000L, 200, 2);
      productRepository.flush(); // ID 부여

      // when
      product.removeSeatGrade(vip.getId());

      // then
      assertThat(product.getSeatGrades()).hasSize(1);
      assertThat(product.getSeatGrades().get(0).getGradeName()).isEqualTo("R석");
    }

    @Test
    void 좌석_등급_제거시_SeatSummary가_자동_재계산된다() {
      // given
      Product product = productRepository.save(createProduct(PRODUCT_NAME, PRODUCT_TYPE));
      SeatGrade vip = product.addSeatGrade("VIP", 150000L, 100, 1);
      product.addSeatGrade("R석", 120000L, 200, 2);
      productRepository.flush();

      // when
      product.removeSeatGrade(vip.getId());

      // then
      assertThat(product.getSeatSummary().getTotalSeats()).isEqualTo(200);
      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(200);
    }

    @Test
    void 존재하지_않는_좌석_등급_제거시_예외가_발생한다() {
      // given
      Product product = productRepository.save(createProduct(PRODUCT_NAME, PRODUCT_TYPE));
      product.addSeatGrade("VIP", 150000L, 100, 1);
      productRepository.flush();

      // when & then
      assertThatThrownBy(() -> product.removeSeatGrade(999L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.SEAT_GRADE_NOT_FOUND);
    }

    @Test
    void 좌석_등급_제거_후_저장이_정상적으로_된다() {
      // given
      Product product = productRepository.save(createProduct(PRODUCT_NAME, PRODUCT_TYPE));
      SeatGrade vip = product.addSeatGrade("VIP", 150000L, 100, 1);
      product.addSeatGrade("R석", 120000L, 200, 2);
      productRepository.flush();

      // when
      product.removeSeatGrade(vip.getId());
      productRepository.flush();

      // then - 재조회해서 확인
      Product found = productRepository.findById(product.getId()).orElseThrow();
      assertThat(found.getSeatGrades()).hasSize(1);
      assertThat(found.getSeatGrades().get(0).getGradeName()).isEqualTo("R석");
      assertThat(found.getSeatSummary().getTotalSeats()).isEqualTo(200);
    }
  }

  // ========== Helper Methods ==========

  private Product createProduct(String name, ProductType type) {
    return createProduct(name, type, DEFAULT_SELLER_ID);
  }

  private Product createProduct(String name, ProductType type, String sellerId) {
    return Product.create(
        sellerId,
        name,
        type,
        RUNNING_TIME,
        futureSchedule,
        futureSaleSchedule,
        defaultVenue,
        ProductContent.empty(),
        AgeRestriction.defaultRestriction(),
        BookingPolicy.defaultPolicy(),
        AdmissionPolicy.defaultPolicy(),
        RefundPolicy.defaultPolicy());
  }
}
