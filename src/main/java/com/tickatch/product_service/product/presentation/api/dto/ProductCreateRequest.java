package com.tickatch.product_service.product.presentation.api.dto;

import com.tickatch.product_service.product.domain.vo.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ProductCreateRequest(
    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 50, message = "상품명은 50자 이하여야 합니다")
    String name,

    @NotNull(message = "상품 타입은 필수입니다")
    ProductType productType,

    @NotNull(message = "상영 시간은 필수입니다")
    @Positive(message = "상영 시간은 양수여야 합니다")
    Integer runningTime,

    @NotNull(message = "시작 시간은 필수입니다")
    LocalDateTime startAt,

    @NotNull(message = "종료 시간은 필수입니다")
    LocalDateTime endAt,

    @NotNull(message = "스테이지 ID는 필수입니다")
    Long stageId
) {
}