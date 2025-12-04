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
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.Schedule;
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

  @InjectMocks
  private ProductQueryService productQueryService;

  @Mock
  private ProductRepository productRepository;

  private LocalDateTime startAt;
  private LocalDateTime endAt;

  @BeforeEach
  void setUp() {
    startAt = LocalDateTime.now().plusDays(7);
    endAt = LocalDateTime.now().plusDays(8);
  }

  @Nested
  class 상품정보_조회_테스트 {

    @Test
    void ID로_상품을_조회할_수_있다() {
      Product product = createProduct(1L, "테스트 공연");
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.getId()).isEqualTo(1L);
      assertThat(response.getName()).isEqualTo("테스트 공연");
      assertThat(response.getProductType()).isEqualTo(ProductType.CONCERT);
      assertThat(response.getRunningTime()).isEqualTo(120);
      assertThat(response.getStageId()).isEqualTo(1L);
      assertThat(response.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void startAt과_endAt이_정확히_매핑된다() {
      Product product = createProduct(1L, "테스트 공연");
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductResponse response = productQueryService.getProduct(1L);

      assertThat(response.getStartAt()).isEqualTo(startAt);
      assertThat(response.getEndAt()).isEqualTo(endAt);
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
          .satisfies(e -> {
            ProductException pe = (ProductException) e;
            assertThat(pe.getErrorArgs()).containsExactly(999L);
          });
    }
  }

  @Nested
  class 상품목록_조회_테스트 {

    @Test
    void 조건에_맞는_상품_목록을_페이징_조회할_수_있다() {
      List<Product> products = List.of(
          createProduct(1L, "콘서트A"),
          createProduct(2L, "콘서트B")
      );
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
      ProductSearchCondition condition = ProductSearchCondition.builder()
          .name("존재하지않는상품")
          .build();
      Pageable pageable = PageRequest.of(0, 10);

      given(productRepository.findAllByCondition(condition, pageable)).willReturn(emptyPage);

      Page<ProductResponse> result = productQueryService.getProducts(condition, pageable);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void 페이지_정보가_정확히_반환된다() {
      List<Product> products = List.of(
          createProduct(1L, "콘서트A"),
          createProduct(2L, "콘서트B")
      );
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

  private Product createProduct(Long id, String name) {
    Schedule schedule = new Schedule(startAt, endAt);
    Product product = Product.create(name, ProductType.CONCERT, 120, schedule, 1L);
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }
}