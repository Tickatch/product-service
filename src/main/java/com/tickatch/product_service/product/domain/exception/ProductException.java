package com.tickatch.product_service.product.domain.exception;

import io.github.tickatch.common.error.BusinessException;

public class ProductException extends BusinessException {

  public ProductException(ProductErrorCode errorCode) {
    super(errorCode);
  }

  public ProductException(ProductErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public ProductException(ProductErrorCode errorCode, Throwable cause, Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}
