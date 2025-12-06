package com.tickatch.product_service.product.presentation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 장소 변경 요청 DTO.
 *
 * <p>상품의 공연 장소를 변경할 때 사용된다. 행사 시작 전에만 장소를 변경할 수 있다.
 *
 * @param stageId 변경할 스테이지 ID (필수)
 * @param stageName 스테이지명 (필수)
 * @param artHallId 공연장 ID (필수)
 * @param artHallName 공연장명 (필수)
 * @param artHallAddress 공연장 주소 (필수)
 * @author Tickatch
 * @since 1.0.0
 */
@Schema(description = "장소 변경 요청")
public record VenueChangeRequest(
    @Schema(description = "스테이지 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "스테이지 ID는 필수입니다")
        Long stageId,
    @Schema(description = "스테이지명", example = "대공연장", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "스테이지명은 필수입니다")
        String stageName,
    @Schema(description = "공연장 ID", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "공연장 ID는 필수입니다")
        Long artHallId,
    @Schema(description = "공연장명", example = "세종문화회관", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "공연장명은 필수입니다")
        String artHallName,
    @Schema(
            description = "공연장 주소",
            example = "서울시 종로구 세종대로 175",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "공연장 주소는 필수입니다")
        String artHallAddress) {}
