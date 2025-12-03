package com.tickatch.product_service.product.presentation.api.dto;

import com.tickatch.product_service.product.domain.vo.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
    @NotNull(message = "상태는 필수입니다")
    ProductStatus status
) {
}