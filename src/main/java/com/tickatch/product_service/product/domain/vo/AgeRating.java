package com.tickatch.product_service.product.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 관람 등급.
 *
 * <p>상품의 관람 가능 연령을 나타내는 열거형이다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum AgeRating {

  /** 전체 관람가 */
  ALL("전체 관람가", 0),

  /** 12세 이상 관람가 */
  TWELVE("12세 이상", 12),

  /** 15세 이상 관람가 */
  FIFTEEN("15세 이상", 15),

  /** 19세 이상 관람가 (성인) */
  NINETEEN("19세 이상", 19);

  /** 등급 설명 */
  private final String description;

  /** 최소 관람 가능 나이 */
  private final int minimumAge;

  /**
   * 해당 나이가 관람 가능한지 확인한다.
   *
   * @param age 확인할 나이
   * @return 관람 가능하면 true
   */
  public boolean canWatch(int age) {
    return age >= this.minimumAge;
  }

  /**
   * 성인 등급인지 확인한다.
   *
   * @return 19세 이상 등급이면 true
   */
  public boolean isAdultOnly() {
    return this == NINETEEN;
  }
}