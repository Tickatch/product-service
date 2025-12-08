package com.tickatch.product_service.product.presentation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 상품 반려 요청 DTO.
 *
 * <p>심사 중인 상품을 반려할 때 사용된다. 반려 사유는 필수이며, 판매자에게 전달된다.
 *
 * @param reason 반려 사유 (필수, 최대 500자)
 * @author Tickatch
 * @since 1.0.0
 */
@Schema(description = "상품 반려 요청")
public record RejectRequest(
    @Schema(
            description = "반려 사유",
            example = "상품 설명이 부족합니다. 상세 내용을 추가해주세요.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "반려 사유는 필수입니다")
        @Size(max = 500, message = "반려 사유는 500자 이하여야 합니다")
        String reason) {}
