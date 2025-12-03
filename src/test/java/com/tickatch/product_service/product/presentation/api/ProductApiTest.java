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
import com.tickatch.product_service.product.presentation.api.dto.StageChangeRequest;
import com.tickatch.product_service.product.presentation.api.dto.StatusChangeRequest;
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
                  .param("status", "DRAFT"))
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
      ProductResponse response = createProductResponse(1L, "테스트 공연");
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
    @MockUser(userId = "seller-1")
    void 상품을_생성할_수_있다() {
      ProductCreateRequest request =
          new ProductCreateRequest(
              "테스트 공연",
              ProductType.CONCERT,
              120,
              LocalDateTime.now().plusDays(7),
              LocalDateTime.now().plusDays(8),
              1L);
      given(productCommandService.createProduct(any(), any(), any(), any(), any(), any()))
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
    @MockUser(userId = "seller-1")
    void 상품명이_없으면_400을_반환한다() {
      ProductCreateRequest request =
          new ProductCreateRequest(
              "",
              ProductType.CONCERT,
              120,
              LocalDateTime.now().plusDays(7),
              LocalDateTime.now().plusDays(8),
              1L);

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
      ProductCreateRequest request =
          new ProductCreateRequest(
              "테스트 공연",
              ProductType.CONCERT,
              120,
              LocalDateTime.now().plusDays(7),
              LocalDateTime.now().plusDays(8),
              1L);

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
    @MockUser(userId = "seller-1")
    void 상품을_수정할_수_있다() {
      ProductUpdateRequest request =
          new ProductUpdateRequest(
              "수정된 공연",
              ProductType.MUSICAL,
              150,
              LocalDateTime.now().plusDays(10),
              LocalDateTime.now().plusDays(11));
      doNothing()
          .when(productCommandService)
          .updateProduct(any(), any(), any(), any(), any(), any());

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
    @MockUser(userId = "seller-1")
    void 존재하지_않는_상품_수정_시_404를_반환한다() {
      ProductUpdateRequest request =
          new ProductUpdateRequest(
              "수정된 공연",
              ProductType.MUSICAL,
              150,
              LocalDateTime.now().plusDays(10),
              LocalDateTime.now().plusDays(11));
      doThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, 999L))
          .when(productCommandService)
          .updateProduct(eq(999L), any(), any(), any(), any(), any());

      assertThat(
              mockMvc
                  .put()
                  .uri(BASE_URL + "/{id}", 999L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(404);
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/products/{id}/stage")
  class 스테이지_변경_API_테스트 {

    @Test
    @MockUser(userId = "seller-1")
    void 스테이지를_변경할_수_있다() {
      StageChangeRequest request = new StageChangeRequest(2L);
      doNothing().when(productCommandService).changeStage(any(), any());

      assertThat(
              mockMvc
                  .patch()
                  .uri(BASE_URL + "/{id}/stage", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = "seller-1")
    void 예매_시작_후_스테이지_변경_시_422를_반환한다() {
      StageChangeRequest request = new StageChangeRequest(2L);
      doThrow(new ProductException(ProductErrorCode.STAGE_CHANGE_NOT_ALLOWED))
          .when(productCommandService)
          .changeStage(eq(1L), eq(2L));

      assertThat(
              mockMvc
                  .patch()
                  .uri(BASE_URL + "/{id}/stage", 1L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .hasStatus(422);
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/products/{id}/status")
  class 상품_상태_변경_API_테스트 {

    @Test
    @MockUser(userId = "seller-1")
    void 상태를_변경할_수_있다() {
      StatusChangeRequest request = new StatusChangeRequest(ProductStatus.PENDING);
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
    @MockUser(userId = "seller-1")
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
    @MockUser(userId = "seller-1")
    void 상품을_취소할_수_있다() {
      doNothing().when(productCommandService).cancelProduct(any(), any());

      assertThat(mockMvc.delete().uri(BASE_URL + "/{id}", 1L))
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.success")
          .isEqualTo(true);
    }

    @Test
    @MockUser(userId = "seller-1")
    void 이미_취소된_상품을_다시_취소하면_422를_반환한다() {
      doThrow(new ProductException(ProductErrorCode.PRODUCT_ALREADY_CANCELLED))
          .when(productCommandService)
          .cancelProduct(eq(1L), any());

      assertThat(mockMvc.delete().uri(BASE_URL + "/{id}", 1L)).hasStatus(422);
    }
  }

  private ProductResponse createProductResponse(Long id, String name) {
    return ProductResponse.builder()
        .id(id)
        .name(name)
        .productType(ProductType.CONCERT)
        .runningTime(120)
        .startAt(LocalDateTime.now().plusDays(7))
        .endAt(LocalDateTime.now().plusDays(8))
        .stageId(1L)
        .status(ProductStatus.DRAFT)
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
