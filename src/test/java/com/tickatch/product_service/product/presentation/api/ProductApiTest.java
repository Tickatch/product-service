package com.tickatch.product_service.product.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickatch.product_service.product.application.service.ProductCommandService;
import com.tickatch.product_service.product.application.service.ProductQueryService;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.repository.dto.ProductResponse;
import com.tickatch.product_service.product.domain.repository.dto.ProductSearchCondition;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.presentation.api.dto.ProductCreateRequest;
import com.tickatch.product_service.product.presentation.api.dto.ProductUpdateRequest;
import com.tickatch.product_service.product.presentation.api.dto.RejectRequest;
import com.tickatch.product_service.product.presentation.api.dto.StatusChangeRequest;
import com.tickatch.product_service.product.presentation.api.dto.VenueChangeRequest;
import io.github.tickatch.common.security.test.MockUser;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProductApi 컨트롤러 테스트")
class ProductApiTest {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ProductCommandService productCommandService;

  @MockitoBean private ProductQueryService productQueryService;

  private static final String BASE_URL = "/api/v1/products";

  // 테스트용 상수
  private static final String SELLER_ID = "seller-1";
  private static final String PRODUCT_NAME = "테스트 공연";
  private static final ProductType PRODUCT_TYPE = ProductType.CONCERT;
  private static final int RUNNING_TIME = 120;
  private static final Long STAGE_ID = 1L;
  private static final String STAGE_NAME = "올림픽홀";
  private static final Long ART_HALL_ID = 100L;
  private static final String ART_HALL_NAME = "올림픽공원";
  private static final String ART_HALL_ADDRESS = "서울시 송파구";

  @Nested
  @DisplayName("GET /api/v1/products")
  class 상품_목록_조회_API_테스트 {

    @Test
    void 상품_목록을_조회할_수_있다() {
      List<ProductResponse> content =
          List.of(createProductResponse(1L, "콘서트A"), createProductResponse(2L, "콘서트B"));
      Page<ProductResponse> page = new PageImpl<>(content);
      given(productQueryService.getProducts(any(ProductSearchCondition.class), any(Pageable.class)))
          .willReturn(page);

      assertThat(mockMvc.get().uri(BASE_URL).param("page", "0").param("size", "10"))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    void 검색_조건으로_상품_목록을_조회할_수_있다() {
      List<ProductResponse> content = List.of(createProductResponse(1L, "콘서트A"));
      Page<ProductResponse> page = new PageImpl<>(content);
      given(productQueryService.getProducts(any(ProductSearchCondition.class), any(Pageable.class)))
          .willReturn(page);

      assertThat(
              mockMvc
                  .get()
                  .uri(BASE_URL)
                  .param("name", "콘서트")
                  .param("productType", "CONCERT")
                  .param("status", "DRAFT")
                  .param("sellerId", SELLER_ID))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }
  }

  @Nested
  @DisplayName("GET /api/v1/products/{id}")
  class 상품_조회_API_테스트 {

    @Test
    void 상품_상세를_조회할_수_있다() {
      ProductResponse response = createProductResponse(1L, PRODUCT_NAME);
      given(productQueryService.getProduct(1L)).willReturn(response);

      assertThat(mockMvc.get().uri(BASE_URL + "/{id}", 1L))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    void 존재하지_않는_상품_조회_시_404를_반환한다() {
      given(productQueryService.getProduct(999L))
          .willThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, 999L));

      assertThat(mockMvc.get().uri(BASE_URL + "/{id}", 999L)).hasStatus(404);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/products")
  class 상품_생성_API_테스트 {

    @Test
    @MockUser(userId = SELLER_ID)
    void 상품을_생성할_수_있다() {
      ProductCreateRequest request = createValidCreateRequest();
      given(
              productCommandService.createProduct(
                  any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                  any(), any()))
          .willReturn(1L);

      assertThat(
              mockMvc
                  .post()
                  .uri(BASE_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(201)
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = SELLER_ID)
    void 상품명이_없으면_400을_반환한다() {
      ProductCreateRequest request =
          new ProductCreateRequest(
              "",
              PRODUCT_TYPE,
              RUNNING_TIME,
              LocalDateTime.now().plusDays(30),
              LocalDateTime.now().plusDays(31),
              LocalDateTime.now().plusDays(1),
              LocalDateTime.now().plusDays(29),
              STAGE_ID,
              STAGE_NAME,
              ART_HALL_ID,
              ART_HALL_NAME,
              ART_HALL_ADDRESS);

      assertThat(
              mockMvc
                  .post()
                  .uri(BASE_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(400);
    }

    @Test
    @MockUser(userId = SELLER_ID)
    void 예매_일정이_없으면_400을_반환한다() {
      ProductCreateRequest request =
          new ProductCreateRequest(
              PRODUCT_NAME,
              PRODUCT_TYPE,
              RUNNING_TIME,
              LocalDateTime.now().plusDays(30),
              LocalDateTime.now().plusDays(31),
              null, // saleStartAt 누락
              null, // saleEndAt 누락
              STAGE_ID,
              STAGE_NAME,
              ART_HALL_ID,
              ART_HALL_NAME,
              ART_HALL_ADDRESS);

      assertThat(
              mockMvc
                  .post()
                  .uri(BASE_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(400);
    }

    @Test
    void 인증_없이_요청하면_401을_반환한다() {
      ProductCreateRequest request = createValidCreateRequest();

      assertThat(
              mockMvc
                  .post()
                  .uri(BASE_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(401);
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/products/{id}")
  class 상품_정보_수정_API_테스트 {

    @Test
    @MockUser(userId = SELLER_ID)
    void 상품을_수정할_수_있다() {
      ProductUpdateRequest request = createValidUpdateRequest();
      doNothing()
          .when(productCommandService)
          .updateProduct(any(), any(), any(), any(), any(), any(), any(), any(), any());

      assertThat(
              mockMvc
                  .put()
                  .uri(BASE_URL + "/{id}", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = SELLER_ID)
    void 존재하지_않는_상품_수정_시_404를_반환한다() {
      ProductUpdateRequest request = createValidUpdateRequest();
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, 999L))
          .when(productCommandService)
          .updateProduct(eq(999L), any(), any(), any(), any(), any(), any(), any(), any());

      assertThat(
              mockMvc
                  .put()
                  .uri(BASE_URL + "/{id}", 999L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(404);
    }

    @Test
    @MockUser(userId = "other-seller")
    void 소유자가_아니면_403을_반환한다() {
      ProductUpdateRequest request = createValidUpdateRequest();
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_OWNED))
          .when(productCommandService)
          .updateProduct(
              eq(1L), eq("other-seller"), any(), any(), any(), any(), any(), any(), any());

      assertThat(
              mockMvc
                  .put()
                  .uri(BASE_URL + "/{id}", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(403);
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/products/{id}/venue")
  class 장소_변경_API_테스트 {

    @Test
    @MockUser(userId = SELLER_ID)
    void 장소를_변경할_수_있다() {
      VenueChangeRequest request = new VenueChangeRequest(2L, "대공연장", 200L, "세종문화회관", "서울시 종로구");
      doNothing()
          .when(productCommandService)
          .changeVenue(any(), any(), any(), any(), any(), any(), any());

      assertThat(
              mockMvc
                  .patch()
                  .uri(BASE_URL + "/{id}/venue", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = SELLER_ID)
    void 행사_시작_후_장소_변경_시_422를_반환한다() {
      VenueChangeRequest request = new VenueChangeRequest(2L, "대공연장", 200L, "세종문화회관", "서울시 종로구");
      doThrow(new ProductException(ProductErrorCode.VENUE_CHANGE_NOT_ALLOWED))
          .when(productCommandService)
          .changeVenue(eq(1L), any(), any(), any(), any(), any(), any());

      assertThat(
              mockMvc
                  .patch()
                  .uri(BASE_URL + "/{id}/venue", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(422);
    }

    @Test
    @MockUser(userId = "other-seller")
    void 소유자가_아니면_403을_반환한다() {
      VenueChangeRequest request = new VenueChangeRequest(2L, "대공연장", 200L, "세종문화회관", "서울시 종로구");
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_OWNED))
          .when(productCommandService)
          .changeVenue(eq(1L), eq("other-seller"), any(), any(), any(), any(), any());

      assertThat(
              mockMvc
                  .patch()
                  .uri(BASE_URL + "/{id}/venue", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(403);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/products/{id}/submit")
  class 심사_제출_API_테스트 {

    @Test
    @MockUser(userId = SELLER_ID)
    void 심사를_제출할_수_있다() {
      doNothing().when(productCommandService).submitForApproval(any(), any());

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/submit", 1L))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = "other-seller")
    void 소유자가_아니면_403을_반환한다() {
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_OWNED))
          .when(productCommandService)
          .submitForApproval(eq(1L), eq("other-seller"));

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/submit", 1L)).hasStatus(403);
    }

    @Test
    @MockUser(userId = SELLER_ID)
    void DRAFT_상태가_아니면_422를_반환한다() {
      doThrow(
              new ProductException(
                  ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED, "PENDING", "PENDING"))
          .when(productCommandService)
          .submitForApproval(eq(1L), any());

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/submit", 1L)).hasStatus(422);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/products/{id}/approve")
  class 승인_API_테스트 {

    @Test
    @MockUser(userId = "admin")
    void 상품을_승인할_수_있다() {
      doNothing().when(productCommandService).approveProduct(any());

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/approve", 1L))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = "admin")
    void PENDING_상태가_아니면_422를_반환한다() {
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_PENDING))
          .when(productCommandService)
          .approveProduct(eq(1L));

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/approve", 1L)).hasStatus(422);
    }

    @Test
    @MockUser(userId = "admin")
    void 존재하지_않는_상품_승인_시_404를_반환한다() {
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, 999L))
          .when(productCommandService)
          .approveProduct(eq(999L));

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/approve", 999L)).hasStatus(404);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/products/{id}/reject")
  class 반려_API_테스트 {

    @Test
    @MockUser(userId = "admin")
    void 상품을_반려할_수_있다() {
      RejectRequest request = new RejectRequest("상품 설명이 부족합니다.");
      doNothing().when(productCommandService).rejectProduct(any(), any());

      assertThat(
              mockMvc
                  .post()
                  .uri(BASE_URL + "/{id}/reject", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = "admin")
    void 반려_사유가_없으면_400을_반환한다() {
      RejectRequest request = new RejectRequest("");

      assertThat(
              mockMvc
                  .post()
                  .uri(BASE_URL + "/{id}/reject", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(400);
    }

    @Test
    @MockUser(userId = "admin")
    void PENDING_상태가_아니면_422를_반환한다() {
      RejectRequest request = new RejectRequest("반려 사유");
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_PENDING))
          .when(productCommandService)
          .rejectProduct(eq(1L), any());

      assertThat(
              mockMvc
                  .post()
                  .uri(BASE_URL + "/{id}/reject", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(422);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/products/{id}/resubmit")
  class 재제출_API_테스트 {

    @Test
    @MockUser(userId = SELLER_ID)
    void 재제출할_수_있다() {
      doNothing().when(productCommandService).resubmitProduct(any(), any());

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/resubmit", 1L))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = "other-seller")
    void 소유자가_아니면_403을_반환한다() {
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_OWNED))
          .when(productCommandService)
          .resubmitProduct(eq(1L), eq("other-seller"));

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/resubmit", 1L)).hasStatus(403);
    }

    @Test
    @MockUser(userId = SELLER_ID)
    void REJECTED_상태가_아니면_422를_반환한다() {
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_REJECTED))
          .when(productCommandService)
          .resubmitProduct(eq(1L), any());

      assertThat(mockMvc.post().uri(BASE_URL + "/{id}/resubmit", 1L)).hasStatus(422);
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/products/{id}/status")
  class 상품_상태_변경_API_테스트 {

    @Test
    @MockUser(userId = "admin")
    void 상태를_변경할_수_있다() {
      StatusChangeRequest request = new StatusChangeRequest(ProductStatus.SCHEDULED);
      doNothing().when(productCommandService).changeStatus(any(), any());

      assertThat(
              mockMvc
                  .patch()
                  .uri(BASE_URL + "/{id}/status", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = "admin")
    void 허용되지_않는_상태_변경_시_422를_반환한다() {
      StatusChangeRequest request = new StatusChangeRequest(ProductStatus.ON_SALE);
      doThrow(
              new ProductException(
                  ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED, "DRAFT", "ON_SALE"))
          .when(productCommandService)
          .changeStatus(eq(1L), eq(ProductStatus.ON_SALE));

      assertThat(
              mockMvc
                  .patch()
                  .uri(BASE_URL + "/{id}/status", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(422);
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/products/{id}")
  class 상품_취소_API_테스트 {

    @Test
    @MockUser(userId = SELLER_ID)
    void 상품을_취소할_수_있다() {
      doNothing().when(productCommandService).cancelProduct(any(), any());

      assertThat(mockMvc.delete().uri(BASE_URL + "/{id}", 1L))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = SELLER_ID)
    void 이미_취소된_상품을_다시_취소하면_422를_반환한다() {
      doThrow(new ProductException(ProductErrorCode.PRODUCT_ALREADY_CANCELLED))
          .when(productCommandService)
          .cancelProduct(eq(1L), any());

      assertThat(mockMvc.delete().uri(BASE_URL + "/{id}", 1L)).hasStatus(422);
    }

    @Test
    @MockUser(userId = SELLER_ID)
    void 존재하지_않는_상품_취소_시_404를_반환한다() {
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, 999L))
          .when(productCommandService)
          .cancelProduct(eq(999L), any());

      assertThat(mockMvc.delete().uri(BASE_URL + "/{id}", 999L)).hasStatus(404);
    }
  }

  // ========== Helper Methods ==========

  private ProductCreateRequest createValidCreateRequest() {
    return new ProductCreateRequest(
        PRODUCT_NAME,
        PRODUCT_TYPE,
        RUNNING_TIME,
        LocalDateTime.now().plusDays(30),
        LocalDateTime.now().plusDays(31),
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now().plusDays(29),
        STAGE_ID,
        STAGE_NAME,
        ART_HALL_ID,
        ART_HALL_NAME,
        ART_HALL_ADDRESS);
  }

  private ProductUpdateRequest createValidUpdateRequest() {
    return new ProductUpdateRequest(
        "수정된 공연",
        ProductType.MUSICAL,
        150,
        LocalDateTime.now().plusDays(30),
        LocalDateTime.now().plusDays(31),
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now().plusDays(29));
  }

  private ProductResponse createProductResponse(Long id, String name) {
    return ProductResponse.builder()
        .id(id)
        .sellerId(SELLER_ID)
        .name(name)
        .productType(PRODUCT_TYPE)
        .runningTime(RUNNING_TIME)
        .startAt(LocalDateTime.now().plusDays(30))
        .endAt(LocalDateTime.now().plusDays(31))
        .saleStartAt(LocalDateTime.now().plusDays(1))
        .saleEndAt(LocalDateTime.now().plusDays(29))
        .stageId(STAGE_ID)
        .stageName(STAGE_NAME)
        .artHallId(ART_HALL_ID)
        .artHallName(ART_HALL_NAME)
        .artHallAddress(ART_HALL_ADDRESS)
        .status(ProductStatus.DRAFT)
        .totalSeats(0)
        .availableSeats(0)
        .viewCount(0L)
        .reservationCount(0)
        .purchasable(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
