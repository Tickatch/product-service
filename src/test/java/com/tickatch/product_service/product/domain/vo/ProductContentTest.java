package com.tickatch.product_service.product.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProductContent 테스트")
class ProductContentTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_값으로_생성할_수_있다() {
      ProductContent content = new ProductContent(
          "상세 설명입니다",
          "https://example.com/poster.jpg",
          "[\"https://example.com/detail1.jpg\"]",
          "홍길동, 김철수",
          "주의사항입니다",
          "주최사",
          "주관사"
      );

      assertThat(content.getDescription()).isEqualTo("상세 설명입니다");
      assertThat(content.getPosterImageUrl()).isEqualTo("https://example.com/poster.jpg");
      assertThat(content.getDetailImageUrls()).isEqualTo("[\"https://example.com/detail1.jpg\"]");
      assertThat(content.getCastInfo()).isEqualTo("홍길동, 김철수");
      assertThat(content.getNotice()).isEqualTo("주의사항입니다");
      assertThat(content.getOrganizer()).isEqualTo("주최사");
      assertThat(content.getAgency()).isEqualTo("주관사");
    }

    @Test
    void 모든_필드가_null이여도_생성할_수_있다() {
      ProductContent content = new ProductContent(null, null, null, null, null, null, null);

      assertThat(content.getDescription()).isNull();
      assertThat(content.getPosterImageUrl()).isNull();
    }

    @Test
    void description이_5000자를_초과하면_예외가_발생한다() {
      String longDescription = "a".repeat(5001);

      assertThatThrownBy(() -> new ProductContent(
          longDescription, null, null, null, null, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_PRODUCT_CONTENT);
    }

    @Test
    void posterUImageUrl이_500자를_초과하면_예외가_발생한다() {
      String longUrl = "https://example.com/" + "a".repeat(500);

      assertThatThrownBy(() -> new ProductContent(
          null, longUrl, null, null, null, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_PRODUCT_CONTENT);
    }

    @Test
    void castInfo가_1000자를_초과하면_예외가_발생한다() {
      String longCastInfo = "a".repeat(1001);

      assertThatThrownBy(() -> new ProductContent(
          null, null, null, longCastInfo, null, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_PRODUCT_CONTENT);
    }

    @Test
    void notice가_2000자를_초과하면_예외가_발생한다() {
      String longNotice = "a".repeat(2001);

      assertThatThrownBy(() -> new ProductContent(
          null, null, null, null, longNotice, null, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_PRODUCT_CONTENT);
    }

    @Test
    void organizer가_100자를_초과하면_예외가_발생한다() {
      String longOrganizer = "a".repeat(101);

      assertThatThrownBy(() -> new ProductContent(
          null, null, null, null, null, longOrganizer, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_PRODUCT_CONTENT);
    }

    @Test
    void agency가_100자를_초과하면_예외가_발생한다() {
      String longAgency = "a".repeat(101);

      assertThatThrownBy(() -> new ProductContent(
          null, null, null, null, null, null, longAgency))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_PRODUCT_CONTENT);
    }

    @Test
    void 각_필드가_최대_길이와_같으면_생성에_성공한다() {
      ProductContent content = new ProductContent(
          "a".repeat(5000),
          "a".repeat(500),
          null,
          "a".repeat(1000),
          "a".repeat(2000),
          "a".repeat(100),
          "a".repeat(100)
      );

      assertThat(content.getDescription()).hasSize(5000);
      assertThat(content.getPosterImageUrl()).hasSize(500);
      assertThat(content.getCastInfo()).hasSize(1000);
      assertThat(content.getNotice()).hasSize(2000);
      assertThat(content.getOrganizer()).hasSize(100);
      assertThat(content.getAgency()).hasSize(100);
    }
  }

  @Nested
  class empty_테스트 {

    @Test
    void 빈_콘텐츠를_생성한다() {
      ProductContent content = ProductContent.empty();

      assertThat(content.getDescription()).isNull();
      assertThat(content.getPosterImageUrl()).isNull();
      assertThat(content.getDetailImageUrls()).isNull();
      assertThat(content.getCastInfo()).isNull();
      assertThat(content.getNotice()).isNull();
      assertThat(content.getOrganizer()).isNull();
      assertThat(content.getAgency()).isNull();
    }
  }

  @Nested
  class hasRequiredFields_메서드 {

    @Test
    void description과_posterImageUrl이_모두_있으면_true를_반환한다() {
      ProductContent content = new ProductContent(
          "상세 설명", "https://example.com/poster.jpg",
          null, null, null, null, null);

      assertThat(content.hasRequiredFields()).isTrue();
    }

    @Test
    void description이_없으면_false를_반환한다() {
      ProductContent content = new ProductContent(
          null, "https://example.com/poster.jpg",
          null, null, null, null, null);

      assertThat(content.hasRequiredFields()).isFalse();
    }

    @Test
    void description이_빈_문자열이면_false를_반환한다() {
      ProductContent content = new ProductContent(
          "   ", "https://example.com/poster.jpg",
          null, null, null, null, null);

      assertThat(content.hasRequiredFields()).isFalse();
    }

    @Test
    void posterImageUrl이_없으면_false를_반환한다() {
      ProductContent content = new ProductContent(
          "상세 설명", null,
          null, null, null, null, null);

      assertThat(content.hasRequiredFields()).isFalse();
    }

    @Test
    void posterImageUrl이_빈_문자열이면_false를_반환한다() {
      ProductContent content = new ProductContent(
          "상세 설명", "   ",
          null, null, null, null, null);

      assertThat(content.hasRequiredFields()).isFalse();
    }

    @Test
    void 빈_콘텐츠는_false를_반환한다() {
      ProductContent content = ProductContent.empty();

      assertThat(content.hasRequiredFields()).isFalse();
    }
  }
  
  @Nested
  class 동등성_테스트 {
    
    @Test
    void 같은_값을_가진_객체는_동등하다() {
      ProductContent content1 = new ProductContent(
          "설명", "https://example.com/poster.jpg",
          null, null, null, null, null);
      ProductContent content2 = new ProductContent(
          "설명", "https://example.com/poster.jpg",
          null, null, null, null, null);

      assertThat(content1).isEqualTo(content2);
      assertThat(content1.hashCode()).isEqualTo(content2.hashCode());
    }
    
    @Test
    void 다른_값을_가진_객체는_동등하지_않다() {
      
    }
  }
}