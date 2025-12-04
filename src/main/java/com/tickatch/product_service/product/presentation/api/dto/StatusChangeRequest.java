package com.tickatch.product_service.product.presentation.api.dto;

import com.tickatch.product_service.product.domain.vo.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 상품 상태 변경 요청 DTO.
 *
 * <p>상품의 상태를 변경할 때 사용된다. 상태 전이 규칙에 따라 유효한 상태로만 변경할 수 있다.
 *
 * <p>상태 전이 규칙:
 *
 * <ul>
 *   <li>DRAFT → OPEN (예매 오픈)
 *   <li>OPEN → CLOSED (예매 마감)
 *   <li>DRAFT, OPEN, CLOSED → CANCELLED (취소)
 * </ul>
 *
 * @param status 변경할 상태 (필수)
 * @author Tickatch
 * @since 1.0.0
 * @see ProductStatus
 */
@Schema(description = "상품 상태 변경 요청")
public record StatusChangeRequest(
    @Schema(description = "변경할 상태", example = "OPEN", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "상태는 필수입니다")
        ProductStatus status) {}
