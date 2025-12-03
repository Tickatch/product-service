package com.tickatch.product_service.product.presentation.api;

import com.tickatch.product_service.product.application.service.ProductCommandService;
import com.tickatch.product_service.product.application.service.ProductQueryService;
import com.tickatch.product_service.product.domain.repository.dto.ProductResponse;
import com.tickatch.product_service.product.presentation.api.dto.ProductCreateRequest;
import com.tickatch.product_service.product.presentation.api.dto.ProductSearchRequest;
import com.tickatch.product_service.product.presentation.api.dto.ProductUpdateRequest;
import com.tickatch.product_service.product.presentation.api.dto.StageChangeRequest;
import com.tickatch.product_service.product.presentation.api.dto.StatusChangeRequest;
import io.github.tickatch.common.api.ApiResponse;
import io.github.tickatch.common.api.PageResponse;
import io.github.tickatch.common.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductApi {
  private final ProductCommandService productCommandService;
  private final ProductQueryService productQueryService;

  @GetMapping
  public ApiResponse<PageResponse<ProductResponse>> getProducts(
      @ModelAttribute ProductSearchRequest request,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    var products = productQueryService.getProducts(request.toCondition(), pageable);
    return ApiResponse.success(PageResponse.from(products));
  }

  @GetMapping("/{id}")
  public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
    var product = productQueryService.getProduct(id);
    return ApiResponse.success(product);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Long> createProduct(
      @Valid @RequestBody ProductCreateRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    Long productId =
        productCommandService.createProduct(
            request.name(),
            request.productType(),
            request.runningTime(),
            request.startAt(),
            request.endAt(),
            request.stageId());
    return ApiResponse.success(productId);
  }

  @PutMapping("/{id}")
  public ApiResponse<Void> updateProduct(
      @PathVariable Long id,
      @Valid @RequestBody ProductUpdateRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.updateProduct(
        id,
        request.name(),
        request.productType(),
        request.runningTime(),
        request.startAt(),
        request.endAt());
    return ApiResponse.success();
  }

  @PatchMapping("/{id}/stage")
  public ApiResponse<Void> changeStage(
      @PathVariable Long id,
      @Valid @RequestBody StageChangeRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.changeStage(id, request.stageId());
    return ApiResponse.success();
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> changeStatus(
      @PathVariable Long id,
      @Valid @RequestBody StatusChangeRequest request,
      @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.changeStatus(id, request.status());
    return ApiResponse.success();
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> cancelProduct(
      @PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.cancelProduct(id, user.getUserId());
    return ApiResponse.success();
  }
}
