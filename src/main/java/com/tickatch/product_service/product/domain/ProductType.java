package com.tickatch.product_service.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductType {
  CONCERT("콘서트"),
  MUSICAL("뮤지컬"),
  PLAY("연극"),
  SPORTS("스포츠");

  private final String description;
}
