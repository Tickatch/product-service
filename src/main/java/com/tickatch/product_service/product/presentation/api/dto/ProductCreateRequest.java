package com.tickatch.product_service.product.presentation.api.dto;

import com.tickatch.product_service.product.domain.vo.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 상품 생성 요청 DTO.
 *
 * <p>새 상품 생성에 필요한 정보를 담는다. 생성된 상품은 DRAFT 상태로 시작된다.
 *
 * @param name 상품명 (필수, 최대 50자)
 * @param productType 상품 타입 (필수)
 * @param runningTime 상영 시간 (필수, 양수, 분 단위)
 * @param startAt 행사 시작 일시 (필수)
 * @param endAt 행사 종료 일시 (필수, 시작 일시 이후)
 * @param saleStartAt 예매 시작 일시 (필수, 행사 시작 전)
 * @param saleEndAt 예매 종료 일시 (필수, 행사 시작 전)
 * @param stageId 스테이지 ID (필수)
 * @param stageName 스테이지명 (필수)
 * @param artHallId 공연장 ID (필수)
 * @param artHallName 공연장명 (필수)
 * @param artHallAddress 공연장 주소 (필수)
 * @author Tickatch
 * @since 1.0.0
 */
@Schema(description = "상품 생성 요청")
public record ProductCreateRequest(
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
            description = "행사 시작 일시",
            example = "2025-03-01T19:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "행사 시작 시간은 필수입니다")
        LocalDateTime startAt,
    @Schema(
            description = "행사 종료 일시",
            example = "2025-03-01T21:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "행사 종료 시간은 필수입니다")
        LocalDateTime endAt,
    @Schema(
            description = "예매 시작 일시",
            example = "2025-02-01T10:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "예매 시작 시간은 필수입니다")
        LocalDateTime saleStartAt,
    @Schema(
            description = "예매 종료 일시",
            example = "2025-02-28T23:59:59",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "예매 종료 시간은 필수입니다")
        LocalDateTime saleEndAt,
    @Schema(description = "스테이지 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "스테이지 ID는 필수입니다")
        Long stageId,
    @Schema(description = "스테이지명", example = "올림픽홀", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "스테이지명은 필수입니다")
        String stageName,
    @Schema(description = "공연장 ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "공연장 ID는 필수입니다")
        Long artHallId,
    @Schema(description = "공연장명", example = "올림픽공원", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "공연장명은 필수입니다")
        String artHallName,
    @Schema(
            description = "공연장 주소",
            example = "서울시 송파구 올림픽로 424",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "공연장 주소는 필수입니다")
        String artHallAddress) {}
