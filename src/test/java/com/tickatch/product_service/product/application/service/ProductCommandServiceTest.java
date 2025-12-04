package com.tickatch.product_service.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.Schedule;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCommandService 테스트")
class ProductCommandServiceTest {

  @InjectMocks
  private ProductCommandService productCommandService;

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
  class 상품을_생성_테스트 {

    @Test
    void 상품을_생성하고_ID를_반환한다() {
      Product savedProduct = createProduct(1L);
      given(productRepository.save(any(Product.class))).willReturn(savedProduct);

      Long productId = productCommandService.createProduct(
          "테스트 공연",
          ProductType.CONCERT,
          120,
          startAt,
          endAt,
          1L
      );

      assertThat(productId).isEqualTo(1L);
      verify(productRepository).save(any(Product.class));
    }

    @Test
    void 잘못된_일정으로_생성하면_예외가_발생한다() {
      LocalDateTime invalidEndAt = startAt.minusDays(1);

      assertThatThrownBy(() -> productCommandService.createProduct(
          "테스트 공연",
          ProductType.CONCERT,
          120,
          startAt,
          invalidEndAt,
          1L
      ))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_SCHEDULE);
    }
  }

  @Nested
  class 상품정보_수정_테스트 {

    @Test
    void 상품을_수정할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.updateProduct(
          1L,
          "수정된 공연",
          ProductType.MUSICAL,
          150,
          startAt,
          endAt
      );

      assertThat(product.getName()).isEqualTo("수정된 공연");
      assertThat(product.getProductType()).isEqualTo(ProductType.MUSICAL);
      assertThat(product.getRunningTime()).isEqualTo(150);
    }

    @Test
    void 존재하지_않는_상품을_수정하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.updateProduct(
          999L,
          "수정된 공연",
          ProductType.MUSICAL,
          150,
          startAt,
          endAt
      ))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 스테이지_변경_테스트 {

    @Test
    void 스테이지를_변경할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.changeStage(1L, 2L);

      assertThat(product.getStageId()).isEqualTo(2L);
    }

    @Test
    void 존재하지_않는_상품의_스테이지를_변경하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.changeStage(999L, 2L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 상태_변경_테스트 {

    @Test
    void 상태를_변경할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.changeStatus(1L, ProductStatus.PENDING);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
    }

    @Test
    void 허용되지_않는_상태로_변경하면_예외가_발생한다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.changeStatus(1L, ProductStatus.ON_SALE))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
    }

    @Test
    void 존재하지_않는_상품의_상태를_변경하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.changeStatus(999L, ProductStatus.PENDING))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 상품_취소_테스트 {

    @Test
    void 상품을_취소할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.cancelProduct(1L, "admin");

      assertThat(product.isCancelled()).isTrue();
      assertThat(product.getDeletedBy()).isEqualTo("admin");
    }

    @Test
    void 이미_취소된_상품을_다시_취소하면_예외가_발생한다() {
      Product product = createProduct(1L);
      product.cancel("admin");
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.cancelProduct(1L, "admin"))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }

    @Test
    void 존재하지_않는_상품을_취소하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.cancelProduct(999L, "admin"))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  private Product createProduct(Long id) {
    Schedule schedule = new Schedule(startAt, endAt);
    Product product = Product.create("테스트 공연", ProductType.CONCERT, 120, schedule, 1L);
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }
}