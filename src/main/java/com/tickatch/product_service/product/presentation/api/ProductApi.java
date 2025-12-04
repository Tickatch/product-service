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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * 상품 API 컨트롤러.
 *
 * <p>상품의 CRUD 및 상태 관리를 위한 REST API를 제공한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductCommandService
 * @see ProductQueryService
 */
@Tag(name = "Product", description = "상품 관리 API")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductApi {

  private final ProductCommandService productCommandService;
  private final ProductQueryService productQueryService;

  /**
   * 상품 목록을 조회한다.
   *
   * @param request 검색 조건 (상품명, 타입, 상태, 스테이지 ID)
   * @param pageable 페이징 정보 (기본값: size=10, sort=createdAt DESC)
   * @return 페이징된 상품 목록
   */
  @Operation(summary = "상품 목록 조회", description = "검색 조건과 페이징을 적용하여 상품 목록을 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공")
  })
  @GetMapping
  public ApiResponse<PageResponse<ProductResponse>> getProducts(
      @ModelAttribute ProductSearchRequest request,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    var products = productQueryService.getProducts(request.toCondition(), pageable);
    return ApiResponse.success(PageResponse.from(products));
  }

  /**
   * 상품 단건을 조회한다.
   *
   * @param id 상품 ID
   * @return 상품 상세 정보
   */
  @Operation(summary = "상품 단건 조회", description = "상품 ID로 상품 상세 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음")
  })
  @GetMapping("/{id}")
  public ApiResponse<ProductResponse> getProduct(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id) {
    var product = productQueryService.getProduct(id);
    return ApiResponse.success(product);
  }

  /**
   * 상품을 생성한다.
   *
   * @param request 상품 생성 요청 (상품명, 타입, 상영시간, 일정, 스테이지 ID)
   * @param user 인증된 사용자 정보
   * @return 생성된 상품 ID
   */
  @Operation(summary = "상품 생성", description = "새 상품을 DRAFT 상태로 생성한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "생성 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Long> createProduct(
      @Valid @RequestBody ProductCreateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
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

  /**
   * 상품을 수정한다.
   *
   * @param id 상품 ID
   * @param request 상품 수정 요청 (상품명, 타입, 상영시간, 일정)
   * @param user 인증된 사용자 정보
   * @return 빈 응답
   */
  @Operation(summary = "상품 수정", description = "상품의 기본 정보를 수정한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음")
  })
  @PutMapping("/{id}")
  public ApiResponse<Void> updateProduct(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Valid @RequestBody ProductUpdateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.updateProduct(
        id,
        request.name(),
        request.productType(),
        request.runningTime(),
        request.startAt(),
        request.endAt());
    return ApiResponse.success();
  }

  /**
   * 상품의 스테이지를 변경한다.
   *
   * @param id 상품 ID
   * @param request 스테이지 변경 요청 (스테이지 ID)
   * @param user 인증된 사용자 정보
   * @return 빈 응답
   */
  @Operation(summary = "스테이지 변경", description = "상품의 스테이지를 변경한다. 예매 시작 전에만 변경 가능하다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "변경 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "스테이지 변경 불가")
  })
  @PatchMapping("/{id}/stage")
  public ApiResponse<Void> changeStage(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Valid @RequestBody StageChangeRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.changeStage(id, request.stageId());
    return ApiResponse.success();
  }

  /**
   * 상품의 상태를 변경한다.
   *
   * @param id 상품 ID
   * @param request 상태 변경 요청 (상태)
   * @param user 인증된 사용자 정보
   * @return 빈 응답
   */
  @Operation(summary = "상태 변경", description = "상품의 상태를 변경한다. 상태 전이 규칙에 따라 유효한 상태로만 변경 가능하다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "변경 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "상태 변경 불가")
  })
  @PatchMapping("/{id}/status")
  public ApiResponse<Void> changeStatus(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Valid @RequestBody StatusChangeRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.changeStatus(id, request.status());
    return ApiResponse.success();
  }

  /**
   * 상품을 취소한다.
   *
   * @param id 상품 ID
   * @param user 인증된 사용자 정보
   * @return 빈 응답
   */
  @Operation(summary = "상품 취소", description = "상품을 취소한다. 관련 서비스로 취소 이벤트가 발행된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "취소 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "이미 취소된 상품")
  })
  @DeleteMapping("/{id}")
  public ApiResponse<Void> cancelProduct(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.cancelProduct(id, user.getUserId());
    return ApiResponse.success();
  }
}
