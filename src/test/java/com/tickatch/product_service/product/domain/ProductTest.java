package com.tickatch.product_service.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.Schedule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Product 도메인 테스트")
class ProductTest {

  private Schedule futureSchedule;
  private Schedule startedSchedule;

  @BeforeEach
  void setUp() {
    futureSchedule =
        new Schedule(
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(8)
        );

    startedSchedule =
        new Schedule(
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1));
  }

  @Nested
  class Product_생성_테스트 {

    @Test
    void 유효한_정보로_상품을_생성할_수_있다() {
      Product product = Product.create(
          "테스트 공연",
          ProductType.CONCERT,
          120,
          futureSchedule,
          1L
      );

      assertThat(product.getName()).isEqualTo("테스트 공연");
      assertThat(product.getProductType()).isEqualTo(ProductType.CONCERT);
      assertThat(product.getRunningTime()).isEqualTo(120);
      assertThat(product.getSchedule()).isEqualTo(futureSchedule);
      assertThat(product.getStageId()).isEqualTo(1L);
      assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void 생성_시_상태는_DRAFT이다() {
      Product product = createDefaultProduct();

      assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
      assertThat(product.isDraft()).isTrue();
    }

    @Nested
    class 상품명_검증_테스트 {

      @Test
      void 상품명이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            null,
            ProductType.CONCERT,
            120,
            futureSchedule,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
      }

      @Test
      void 상품명이_빈_문자열이면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            "",
            ProductType.CONCERT,
            120,
            futureSchedule,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
      }

      @Test
      void 상품명이_공백만_있으면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            "   ",
            ProductType.CONCERT,
            120,
            futureSchedule,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
      }

      @Test
      void 상품명이_50자를_초과하면_ProductEcetpion이_발생한다() {
        String longName = "a".repeat(51);

        assertThatThrownBy(() -> Product.create(
            longName,
            ProductType.CONCERT,
            120,
            futureSchedule,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
      }

      @Test
      void 상품명이_정확히_50자이면_생성에_성공한다() {
        String exactName = "a".repeat(50);

        Product product = Product.create(
            exactName,
            ProductType.CONCERT,
            120,
            futureSchedule,
            1L
        );

        assertThat(product.getName()).isEqualTo(exactName);
      }
    }

    @Nested
    class 상품_타입_검증_테스트 {

      @Test
      void 상품_타입이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            "테스트 공연",
            null,
            120,
            futureSchedule,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_PRODUCT_TYPE);
      }
    }

    @Nested
    class 상영_시간_검증_테스트 {

      @Test
      void 상영_시간이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            "테스트 공연",
            ProductType.CONCERT,
            null,
            futureSchedule,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_RUNNING_TIME);
      }

      @Test
      void 상영_시간이_0이면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            "테스트 공연",
            ProductType.CONCERT,
            0,
            futureSchedule,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_RUNNING_TIME);
      }

      @Test
      void 상영_시간이_음수이면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            "테스트 공연",
            ProductType.CONCERT,
            -1,
            futureSchedule,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_RUNNING_TIME);
      }
    }

    @Nested
    class 일정_검증_테스트 {

      @Test
      void 일정이_null이면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            "테스트 공연",
            ProductType.CONCERT,
            120,
            null,
            1L
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_SCHEDULE);
      }
      }
    }

    @Nested
    class 스테이지_ID_검증_테스트 {

      @Test
      void 스테이지_ID가_null이면_ProductException이_발생한다() {
        assertThatThrownBy(() -> Product.create(
            "테스트 공연",
            ProductType.CONCERT,
            120,
            futureSchedule,
            null
        ))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.INVALID_STAGE_ID);
      }
    }

  @Nested
  class 상품_수정_테스트 {

    @Test
    void 유효한_정보로_상품을_수정할_수_있다() {
      Product product = createDefaultProduct();
      Schedule newSchedule = new Schedule(
          LocalDateTime.now().plusDays(10),
          LocalDateTime.now().plusDays(11)
      );

      product.update("수정된 공연", ProductType.MUSICAL, 150, newSchedule);

      assertThat(product.getName()).isEqualTo("수정된 공연");
      assertThat(product.getProductType()).isEqualTo(ProductType.MUSICAL);
      assertThat(product.getRunningTime()).isEqualTo(150);
      assertThat(product.getSchedule()).isEqualTo(newSchedule);
    }

    @Test
    void 취소된_상품은_수정할_수_없다() {
      Product product = createDefaultProduct();
      product.cancel("admin");

      assertThatThrownBy(() -> product.update(
          "수정된 공연",
          ProductType.MUSICAL,
          150,
          futureSchedule

      ))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }
  }

  @Nested
  class 스테이지_변경_테스트 {

    @Test
    void 예매_시작_전에는_스테이지를_변경할_수_있다() {
      Product product = createDefaultProduct();

      product.changeStage(2L);

      assertThat(product.getStageId()).isEqualTo(2L);
    }

    @Test
    void 예매_시작_후에는_스테이지를_변경할_수_없다() {
      Product product = Product.create(
          "테스트 공연",
          ProductType.CONCERT,
          120,
          startedSchedule,
          1L
      );

      assertThatThrownBy(() -> product.changeStage(2L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.STAGE_CHANGE_NOT_ALLOWED);
    }

    @Test
    void 취소된_상품의_스테이지는_변경할_수_없다() {
      Product product = createDefaultProduct();
      product.cancel("admin");

      assertThatThrownBy(() -> product.changeStage(2L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }

    @Test
    void 스테이지_ID가_null이면_ProductExceptio이_발생한다() {
      Product product = createDefaultProduct();

      assertThatThrownBy(() -> product.changeStage(null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_STAGE_ID);
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
            .satisfies(e -> {
              ProductException pe = (ProductException) e;
              assertThat(pe.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
              assertThat(pe.getErrorArgs()).containsExactly("DRAFT", "ON_SALE");
            });
      }

      @Test
      void DRAFT에서_SOLD_OUT으로_직접_변경할_수_없다() {
        Product product = createDefaultProduct();

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.SOLD_OUT))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }
    }

    @Nested
    class PENDING에서_상태_변경_테스트 {

      @Test
      void PENDING에서_DRAFT로_변경할_수_있다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        product.changeStatus(ProductStatus.DRAFT);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.isDraft()).isTrue();
      }

      @Test
      void PENDING에서_ON_SALE로_변경할_수_있다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        product.changeStatus(ProductStatus.ON_SALE);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(product.isOnSale()).isTrue();
      }

      @Test
      void PENDING에서_SOLD_OUT으로_직접_변경할_수_없다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        assertThatThrownBy(() -> product.changeStatus(ProductStatus.SOLD_OUT))
            .isInstanceOf(ProductException.class)
            .extracting(e -> ((ProductException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
      }
    }

    @Nested
    class ON_SALE에서_상태_변경_테스트 {

      @Test
      void ON_SALE에서_SOLD_OUT으로_변경할_수_있다() {
        Product product = createOnSaleProduct();

        product.changeStatus(ProductStatus.SOLD_OUT);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        assertThat(product.isSoldOut()).isTrue();
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

      @Nested
      class SOLD_OUT에서_상태_변경_테스트 {

        @Test
        void SOLD_OUT에서_ON_SALE로_변경할_수_있다() {
          Product product = createOnSaleProduct();
          product.changeStatus(ProductStatus.SOLD_OUT);

          product.changeStatus(ProductStatus.ON_SALE);

          assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        }

        @Test
        void SOLD_OUT에서_DRAFT로_변경할_수_없다() {
          Product product = createOnSaleProduct();
          product.changeStatus(ProductStatus.SOLD_OUT);

          assertThatThrownBy(() -> product.changeStatus(ProductStatus.DRAFT))
              .isInstanceOf(ProductException.class)
              .extracting(e -> ((ProductException) e).getErrorCode())
              .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
        }

        @Test
        void SOLD_OUT에서_PENDING으로_변경할_수_없다() {
          Product product = createOnSaleProduct();
          product.changeStatus(ProductStatus.SOLD_OUT);

          assertThatThrownBy(() -> product.changeStatus(ProductStatus.PENDING))
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
      void SOLD_OUT_상태에서_취소할_수_있다() {
        Product product = createOnSaleProduct();
        product.changeStatus(ProductStatus.SOLD_OUT);

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
      void isPending메서드는_판매대기_상태에서_true를_반환한다() {
        Product product = createDefaultProduct();
        product.changeStatus(ProductStatus.PENDING);

        assertThat(product.isPending()).isTrue();
      }

      @Test
      void isOnSale메서드는_판매중_상태에서_true를_반환한다() {
        Product product = createOnSaleProduct();

        assertThat(product.isOnSale()).isTrue();
      }

      @Test
      void isSoldOut메서드는_매진_상태에서_true를_반환한다() {
        Product product = createOnSaleProduct();
        product.changeStatus(ProductStatus.SOLD_OUT);

        assertThat(product.isSoldOut()).isTrue();
      }

      @Test
      void isCancelled메서드는_취소된_상태에서_true를_반환한다() {
        Product product = createDefaultProduct();
        product.cancel("admin");

        assertThat(product.isCancelled()).isTrue();
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
  }
  private Product createDefaultProduct() {
    return Product.create(
        "테스트 공연",
        ProductType.CONCERT,
        120,
        futureSchedule,
        1L
    );
  }

  private Product createOnSaleProduct() {
    Product product = createDefaultProduct();
    product.changeStatus(ProductStatus.PENDING);
    product.changeStatus(ProductStatus.ON_SALE);
    return product;
  }
}



