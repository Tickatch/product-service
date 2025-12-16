package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 상품 콘텐츠 Value Object.
 *
 * <p>상품 상세 페이지에 표시될 콘텐츠 정보를 관리한다. 불변 객체로 설계되어 값의 일관성을 보장한다.
 *
 * <p>필드 제약:
 *
 * <ul>
 *   <li>description: 최대 5000자
 *   <li>posterImageUrl: 최대 500자
 *   <li>detailImageUrls: JSON 배열 형태
 *   <li>castInfo: 최대 1000자
 *   <li>notice: 최대 2000자
 *   <li>organizer: 최대 100자
 *   <li>agency: 최대 100자
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ProductContent {

  private static final int DESCRIPTION_MAX_LENGTH = 5000;
  private static final int POSTER_IMAGE_URL_MAX_LENGTH = 500;
  private static final int CAST_INFO_MAX_LENGTH = 1000;
  private static final int NOTICE_MAX_LENGTH = 2000;
  private static final int ORGANIZER_MAX_LENGTH = 100;
  private static final int AGENCY_MAX_LENGTH = 100;

  /** 상세 설명 */
  @Column(name = "description", length = DESCRIPTION_MAX_LENGTH)
  private String description;

  /** 메인 포스터 이미지 URL */
  @Column(name = "poster_image_url", length = POSTER_IMAGE_URL_MAX_LENGTH)
  private String posterImageUrl;

  /** 상세 이미지 URL 배열 (JSON) */
  @Column(name = "detail_image_urls", columnDefinition = "JSON")
  @JdbcTypeCode(SqlTypes.JSON)
  private String detailImageUrls;

  /** 출연진/아티스트 정보 */
  @Column(name = "cast_info", length = CAST_INFO_MAX_LENGTH)
  private String castInfo;

  /** 유의사항 */
  @Column(name = "notice", length = NOTICE_MAX_LENGTH)
  private String notice;

  /** 주최사 */
  @Column(name = "organizer", length = ORGANIZER_MAX_LENGTH)
  private String organizer;

  /** 주관사/기획사 */
  @Column(name = "agency", length = AGENCY_MAX_LENGTH)
  private String agency;

  /**
   * 상품 콘텐츠를 생성한다.
   *
   * <p>모든 필드는 nullable이며, 심사 제출 시 필수 항목을 별도 검증한다.
   *
   * @param description 상세 설명
   * @param posterImageUrl 포스터 이미지 URL
   * @param detailImageUrls 상세 이미지 URL 배열 (JSON)
   * @param castInfo 출연진 정보
   * @param notice 유의사항
   * @param organizer 주최사
   * @param agency 주관사
   * @throws ProductException 길이 제한 초과 시 ({@link ProductErrorCode#INVALID_PRODUCT_CONTENT})
   */
  public ProductContent(
      String description,
      String posterImageUrl,
      String detailImageUrls,
      String castInfo,
      String notice,
      String organizer,
      String agency) {
    validate(description, posterImageUrl, castInfo, notice, organizer, agency);
    this.description = description;
    this.posterImageUrl = posterImageUrl;
    this.detailImageUrls = detailImageUrls;
    this.castInfo = castInfo;
    this.notice = notice;
    this.organizer = organizer;
    this.agency = agency;
  }

  /**
   * 빈 콘텐츠를 생성한다.
   *
   * <p>DRAFT 상태에서 점진적으로 입력할 때 사용한다.
   *
   * @return 빈 ProductContent
   */
  public static ProductContent empty() {
    return new ProductContent(null, null, null, null, null, null, null);
  }

  /**
   * 심사 제출 시 필수 항목이 입력되었는지 확인한다.
   *
   * @return description과 posterImageUrl이 있으면 true
   */
  public boolean hasRequiredFields() {
    return description != null
        && !description.isBlank()
        && posterImageUrl != null
        && !posterImageUrl.isBlank();
  }

  private static void validate(
      String description,
      String posterImageUrl,
      String castInfo,
      String notice,
      String organizer,
      String agency) {
    validateLength(description, DESCRIPTION_MAX_LENGTH, "description");
    validateLength(posterImageUrl, POSTER_IMAGE_URL_MAX_LENGTH, "posterImageUrl");
    validateLength(castInfo, CAST_INFO_MAX_LENGTH, "castInfo");
    validateLength(notice, NOTICE_MAX_LENGTH, "notice");
    validateLength(organizer, ORGANIZER_MAX_LENGTH, "organizer");
    validateLength(agency, AGENCY_MAX_LENGTH, "agency");
  }

  private static void validateLength(String value, int maxLength, String fieldName) {
    if (value != null && value.length() > maxLength) {
      throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CONTENT);
    }
  }
}
