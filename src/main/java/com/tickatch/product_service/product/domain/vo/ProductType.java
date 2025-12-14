package com.tickatch.product_service.product.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 타입.
 *
 * <p>상품의 종류를 나타내는 열거형이다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ProductType {

  /** 콘서트 */
  CONCERT("콘서트"),

  /** 뮤지컬 */
  MUSICAL("뮤지컬"),

  /** 연극 */
  PLAY("연극"),

  /** 스포츠 */
  SPORTS("스포츠");

  /** 타입 설명 */
  private final String description;
}
