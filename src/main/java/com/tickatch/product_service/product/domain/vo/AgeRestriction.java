package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관람 제한 Value Object.
 *
 * <p>상품의 관람등급 및 입장 제한 정보를 관리한다. 불변 객체로 설계되어 값의 일관성을 보장한다.
 *
 * <p>필드 제약:
 *
 * <ul>
 *   <li>ageRating: 기본값 ALL (전체 관람가)
 *   <li>restrictionNotice: 최대 500자
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see AgeRating
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class AgeRestriction {

  private static final int RESTRICTION_NOTICE_MAX_LENGTH = 500;

  /** 관람 등급 */
  @Enumerated(EnumType.STRING)
  @Column(name = "age_rating")
  private AgeRating ageRating;

  /** 추가 제한사항 안내 */
  @Column(name = "restriction_notice", length = RESTRICTION_NOTICE_MAX_LENGTH)
  private String restrictionNotice;

  /**
   * 관람 제한 정보를 생성한다.
   *
   * @param ageRating 관람 등급 (null이면 ALL)
   * @param restrictionNotice 추가 제한사항 안내
   * @throws ProductException 길이 제한 초과 시 ({@link ProductErrorCode#INVALID_AGE_RESTRICTION})
   */
  public AgeRestriction(AgeRating ageRating, String restrictionNotice) {
    validate(restrictionNotice);
    this.ageRating = ageRating != null ? ageRating : AgeRating.ALL;
    this.restrictionNotice = restrictionNotice;
  }

  /**
   * 기본 관람 제한 정보를 생성한다.
   *
   * <p>전체 관람가(ALL)로 설정된다.
   *
   * @return 기본 AgeRestriction
   */
  public static AgeRestriction defaultRestriction() {
    return new AgeRestriction(AgeRating.ALL, null);
  }

  /**
   * 최소 관람 가능 나이를 반환한다.
   *
   * @return 최소 나이
   */
  public int getMinimumAge() {
    return ageRating.getMinimumAge();
  }

  /**
   * 성인 전용 상품인지 확인한다.
   *
   * @return 19세 이상 등급이면 true
   */
  public boolean isAdultOnly() {
    return ageRating.isAdultOnly();
  }

  /**
   * 해당 나이가 관람 가능한지 확인한다.
   *
   * @param age 확인할 나이
   * @return 관람 가능하면 true
   */
  public boolean canWatch(int age) {
    return ageRating.canWatch(age);
  }

  private static void validate(String restrictionNotice) {
    if (restrictionNotice != null && restrictionNotice.length() > RESTRICTION_NOTICE_MAX_LENGTH) {
      throw new ProductException(ProductErrorCode.INVALID_AGE_RESTRICTION);
    }
  }
}
