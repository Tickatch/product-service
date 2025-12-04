package com.tickatch.product_service.product.presentation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 스테이지 변경 요청 DTO.
 *
 * <p>상품의 스테이지를 변경할 때 사용된다. 예매 시작 전에만 스테이지를 변경할 수 있다.
 *
 * @param stageId 변경할 스테이지 ID (필수)
 * @author Tickatch
 * @since 1.0.0
 */
@Schema(description = "스테이지 변경 요청")
public record StageChangeRequest(
    @Schema(description = "스테이지 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "스테이지 ID는 필수입니다")
        Long stageId) {}
