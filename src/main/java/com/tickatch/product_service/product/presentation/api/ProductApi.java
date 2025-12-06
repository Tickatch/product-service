package com.tickatch.product_service.product.presentation.api;

import com.tickatch.product_service.product.application.service.ProductCommandService;
import com.tickatch.product_service.product.application.service.ProductQueryService;
import com.tickatch.product_service.product.domain.repository.dto.ProductResponse;
import com.tickatch.product_service.product.presentation.api.dto.ProductCreateRequest;
import com.tickatch.product_service.product.presentation.api.dto.ProductSearchRequest;
import com.tickatch.product_service.product.presentation.api.dto.ProductUpdateRequest;
import com.tickatch.product_service.product.presentation.api.dto.RejectRequest;
import com.tickatch.product_service.product.presentation.api.dto.StatusChangeRequest;
import com.tickatch.product_service.product.presentation.api.dto.VenueChangeRequest;
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

  // ========== 조회 ==========

  /**
   * 상품 목록을 조회한다.
   *
   * @param request 검색 조건 (상품명, 타입, 상태, 스테이지 ID, 판매자 ID)
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

  // ========== 생성/수정 ==========

  /**
   * 상품을 생성한다.
   *
   * @param request 상품 생성 요청
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
            user.getUserId(),
            request.name(),
            request.productType(),
            request.runningTime(),
            request.startAt(),
            request.endAt(),
            request.saleStartAt(),
            request.saleEndAt(),
            request.stageId(),
            request.stageName(),
            request.artHallId(),
            request.artHallName(),
            request.artHallAddress());
    return ApiResponse.success(productId);
  }

  /**
   * 상품을 수정한다.
   *
   * @param id 상품 ID
   * @param request 상품 수정 요청
   * @param user 인증된 사용자 정보
   * @return 빈 응답
   */
  @Operation(summary = "상품 수정", description = "상품의 기본 정보를 수정한다. DRAFT, REJECTED 상태에서만 가능하다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "소유자가 아님"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "수정 불가 상태")
  })
  @PutMapping("/{id}")
  public ApiResponse<Void> updateProduct(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Valid @RequestBody ProductUpdateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.updateProduct(
        id,
        user.getUserId(),
        request.name(),
        request.productType(),
        request.runningTime(),
        request.startAt(),
        request.endAt(),
        request.saleStartAt(),
        request.saleEndAt());
    return ApiResponse.success();
  }

  /**
   * 상품의 장소를 변경한다.
   *
   * @param id 상품 ID
   * @param request 장소 변경 요청
   * @param user 인증된 사용자 정보
   * @return 빈 응답
   */
  @Operation(summary = "장소 변경", description = "상품의 공연 장소를 변경한다. 행사 시작 전에만 변경 가능하다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "변경 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "소유자가 아님"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "장소 변경 불가")
  })
  @PatchMapping("/{id}/venue")
  public ApiResponse<Void> changeVenue(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Valid @RequestBody VenueChangeRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.changeVenue(
        id,
        user.getUserId(),
        request.stageId(),
        request.stageName(),
        request.artHallId(),
        request.artHallName(),
        request.artHallAddress());
    return ApiResponse.success();
  }

  // ========== 심사 ==========

  /**
   * 상품을 심사에 제출한다.
   *
   * @param id 상품 ID
   * @param user 인증된 사용자 정보
   * @return 빈 응답
   */
  @Operation(summary = "심사 제출", description = "DRAFT 상태의 상품을 심사에 제출한다. 상태가 PENDING으로 변경된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "제출 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "소유자가 아님"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "제출 불가 상태")
  })
  @PostMapping("/{id}/submit")
  public ApiResponse<Void> submitForApproval(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.submitForApproval(id, user.getUserId());
    return ApiResponse.success();
  }

  /**
   * 상품을 승인한다.
   *
   * @param id 상품 ID
   * @return 빈 응답
   */
  @Operation(summary = "상품 승인", description = "PENDING 상태의 상품을 승인한다. 상태가 APPROVED로 변경된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "승인 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "PENDING 상태가 아님")
  })
  @PostMapping("/{id}/approve")
  public ApiResponse<Void> approveProduct(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id) {
    productCommandService.approveProduct(id);
    return ApiResponse.success();
  }

  /**
   * 상품을 반려한다.
   *
   * @param id 상품 ID
   * @param request 반려 요청 (사유 포함)
   * @return 빈 응답
   */
  @Operation(summary = "상품 반려", description = "PENDING 상태의 상품을 반려한다. 상태가 REJECTED로 변경된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "반려 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "반려 사유 누락"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "PENDING 상태가 아님")
  })
  @PostMapping("/{id}/reject")
  public ApiResponse<Void> rejectProduct(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Valid @RequestBody RejectRequest request) {
    productCommandService.rejectProduct(id, request.reason());
    return ApiResponse.success();
  }

  /**
   * 반려된 상품을 재제출한다.
   *
   * @param id 상품 ID
   * @param user 인증된 사용자 정보
   * @return 빈 응답
   */
  @Operation(summary = "재제출", description = "REJECTED 상태의 상품을 수정 후 재제출한다. 상태가 DRAFT로 변경된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "재제출 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "소유자가 아님"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "상품을 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "REJECTED 상태가 아님")
  })
  @PostMapping("/{id}/resubmit")
  public ApiResponse<Void> resubmitProduct(
      @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    productCommandService.resubmitProduct(id, user.getUserId());
    return ApiResponse.success();
  }

  // ========== 상태 관리 ==========

  /**
   * 상품의 상태를 변경한다.
   *
   * @param id 상품 ID
   * @param request 상태 변경 요청
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
      @Valid @RequestBody StatusChangeRequest request) {
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
