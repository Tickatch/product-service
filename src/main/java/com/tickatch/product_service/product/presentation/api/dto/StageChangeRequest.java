package com.tickatch.product_service.product.presentation.api.dto;

import jakarta.validation.constraints.NotNull;

public record StageChangeRequest(@NotNull(message = "스테이지 ID는 필수입니다") Long stageId) {}
