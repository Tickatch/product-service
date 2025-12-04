package com.tickatch.product_service.product.presentation.api.dto;

import com.tickatch.product_service.product.domain.vo.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 상품 수정 요청 DTO.
 *
 * <p>기존 상품의 기본 정보 수정에 필요한 데이터를 담는다. 스테이지 변경은 별도의 API를 통해 처리된다.
 *
 * @param name 상품명 (필수, 최대 50자)
 * @param productType 상품 타입 (필수)
 * @param runningTime 상영 시간 (필수, 양수, 분 단위)
 * @param startAt 시작 일시 (필수)
 * @param endAt 종료 일시 (필수, 시작 일시 이후)
 * @author Tickatch
 * @since 1.0.0
 * @see StageChangeRequest
 */
@Schema(description = "상품 수정 요청")
public record ProductUpdateRequest(
    @Schema(description = "상품명", example = "레미제라블", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 50, message = "상품명은 50자 이하여야 합니다")
        String name,
    @Schema(description = "상품 타입", example = "MUSICAL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "상품 타입은 필수입니다")
        ProductType productType,
    @Schema(description = "상영 시간 (분)", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "상영 시간은 필수입니다")
        @Positive(message = "상영 시간은 양수여야 합니다")
        Integer runningTime,
    @Schema(
            description = "시작 일시",
            example = "2025-03-01T19:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "시작 시간은 필수입니다")
        LocalDateTime startAt,
    @Schema(
            description = "종료 일시",
            example = "2025-03-01T21:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "종료 시간은 필수입니다")
        LocalDateTime endAt) {}
