package com.tickatch.product_service.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tickatch.product_service.product.application.dto.ProductCreateCommand;
import com.tickatch.product_service.product.application.dto.ProductCreateCommand.SeatCreateInfo;
import com.tickatch.product_service.product.application.dto.ProductCreateCommand.SeatGradeInfo;
import com.tickatch.product_service.product.application.dto.ProductUpdateCommand;
import com.tickatch.product_service.product.application.messaging.ProductEventPublisher;
import com.tickatch.product_service.product.application.messaging.ProductLogEventPublisher;
import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.AdmissionPolicy;
import com.tickatch.product_service.product.domain.vo.AgeRestriction;
import com.tickatch.product_service.product.domain.vo.BookingPolicy;
import com.tickatch.product_service.product.domain.vo.ProductContent;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.RefundPolicy;
import com.tickatch.product_service.product.domain.vo.SaleSchedule;
import com.tickatch.product_service.product.domain.vo.Schedule;
import com.tickatch.product_service.product.domain.vo.Venue;
import com.tickatch.product_service.product.infrastructure.client.ReservationSeatClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCommandService 테스트")
class ProductCommandServiceTest {

  private static final String DEFAULT_SELLER_ID = "seller-001";
  private static final String OTHER_SELLER_ID = "seller-002";
  private static final String DEFAULT_PRODUCT_NAME = "테스트 공연";
  private static final ProductType DEFAULT_PRODUCT_TYPE = ProductType.CONCERT;
  private static final int DEFAULT_RUNNING_TIME = 120;
  private static final Long DEFAULT_STAGE_ID = 1L;
  private static final String DEFAULT_STAGE_NAME = "올림픽홀";
  private static final Long DEFAULT_ART_HALL_ID = 100L;
  private static final String DEFAULT_ART_HALL_NAME = "올림픽공원";
  private static final String DEFAULT_ART_HALL_ADDRESS = "서울시 송파구";

  @InjectMocks private ProductCommandService productCommandService;

  @Mock private ProductRepository productRepository;

  @Mock private ProductEventPublisher productEventPublisher;

  @Mock private ProductLogEventPublisher productLogEventPublisher;

  @Mock private ReservationSeatClient reservationSeatClient;

  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private LocalDateTime saleStartAt;
  private LocalDateTime saleEndAt;

  @BeforeEach
  void setUp() {
    startAt = LocalDateTime.now().plusDays(30);
    endAt = LocalDateTime.now().plusDays(31);
    saleStartAt = LocalDateTime.now().plusDays(1);
    saleEndAt = LocalDateTime.now().plusDays(29);
  }

  @Nested
  class 상품_생성_테스트 {

    @Test
    void 상품을_생성하고_ID를_반환한다() {
      Product savedProduct = createProduct(1L);
      given(productRepository.save(any(Product.class))).willReturn(savedProduct);

      ProductCreateCommand command = createDefaultCommand();

      Long productId = productCommandService.createProduct(command);

      assertThat(productId).isEqualTo(1L);
      verify(productRepository).save(any(Product.class));
      verify(reservationSeatClient).createSeats(any());
    }

    @Test
    void 잘못된_일정으로_생성하면_예외가_발생한다() {
      LocalDateTime invalidEndAt = startAt.minusDays(1);

      ProductCreateCommand command =
          ProductCreateCommand.builder()
              .sellerId(DEFAULT_SELLER_ID)
              .name(DEFAULT_PRODUCT_NAME)
              .productType(DEFAULT_PRODUCT_TYPE)
              .runningTime(DEFAULT_RUNNING_TIME)
              .startAt(startAt)
              .endAt(invalidEndAt)
              .saleStartAt(saleStartAt)
              .saleEndAt(saleEndAt)
              .stageId(DEFAULT_STAGE_ID)
              .stageName(DEFAULT_STAGE_NAME)
              .artHallId(DEFAULT_ART_HALL_ID)
              .artHallName(DEFAULT_ART_HALL_NAME)
              .artHallAddress(DEFAULT_ART_HALL_ADDRESS)
              .seatGradeInfos(createDefaultSeatGradeInfos())
              .seatCreateInfos(createDefaultSeatCreateInfos())
              .build();

      assertThatThrownBy(() -> productCommandService.createProduct(command))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_SCHEDULE);
    }
  }

  @Nested
  class 상품정보_수정_테스트 {

    @Test
    void 상품을_수정할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductUpdateCommand command =
          ProductUpdateCommand.builder()
              .productId(1L)
              .sellerId(DEFAULT_SELLER_ID)
              .name("수정된 공연")
              .productType(ProductType.MUSICAL)
              .runningTime(150)
              .startAt(startAt)
              .endAt(endAt)
              .saleStartAt(saleStartAt)
              .saleEndAt(saleEndAt)
              .build();

      productCommandService.updateProduct(command);

      assertThat(product.getName()).isEqualTo("수정된 공연");
      assertThat(product.getProductType()).isEqualTo(ProductType.MUSICAL);
      assertThat(product.getRunningTime()).isEqualTo(150);
    }

    @Test
    void 소유자가_아니면_수정할_수_없다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductUpdateCommand command =
          ProductUpdateCommand.builder()
              .productId(1L)
              .sellerId(OTHER_SELLER_ID)
              .name("수정된 공연")
              .build();

      assertThatThrownBy(() -> productCommandService.updateProduct(command))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_OWNED);
    }

    @Test
    void 존재하지_않는_상품을_수정하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      ProductUpdateCommand command =
          ProductUpdateCommand.builder()
              .productId(999L)
              .sellerId(DEFAULT_SELLER_ID)
              .name("수정된 공연")
              .build();

      assertThatThrownBy(() -> productCommandService.updateProduct(command))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 장소_변경_테스트 {

    @Test
    void 장소를_변경할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductUpdateCommand command =
          ProductUpdateCommand.builder()
              .productId(1L)
              .sellerId(DEFAULT_SELLER_ID)
              .stageId(2L)
              .stageName("대공연장")
              .artHallId(200L)
              .artHallName("세종문화회관")
              .artHallAddress("서울시 종로구")
              .build();

      productCommandService.updateProduct(command);

      assertThat(product.getStageId()).isEqualTo(2L);
      assertThat(product.getVenue().getStageName()).isEqualTo("대공연장");
    }

    @Test
    void 소유자가_아니면_장소를_변경할_수_없다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      ProductUpdateCommand command =
          ProductUpdateCommand.builder()
              .productId(1L)
              .sellerId(OTHER_SELLER_ID)
              .stageId(2L)
              .stageName("대공연장")
              .artHallId(200L)
              .artHallName("세종문화회관")
              .artHallAddress("서울시 종로구")
              .build();

      assertThatThrownBy(() -> productCommandService.updateProduct(command))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_OWNED);
    }

    @Test
    void 존재하지_않는_상품의_장소를_변경하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      ProductUpdateCommand command =
          ProductUpdateCommand.builder()
              .productId(999L)
              .sellerId(DEFAULT_SELLER_ID)
              .stageId(2L)
              .stageName("대공연장")
              .artHallId(200L)
              .artHallName("세종문화회관")
              .artHallAddress("서울시 종로구")
              .build();

      assertThatThrownBy(() -> productCommandService.updateProduct(command))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 심사_제출_테스트 {

    @Test
    void 심사를_제출할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.submitForApproval(1L, DEFAULT_SELLER_ID);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
    }

    @Test
    void 소유자가_아니면_심사를_제출할_수_없다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.submitForApproval(1L, OTHER_SELLER_ID))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_OWNED);
    }

    @Test
    void 존재하지_않는_상품의_심사를_제출하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.submitForApproval(999L, DEFAULT_SELLER_ID))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 승인_테스트 {

    @Test
    void 상품을_승인할_수_있다() {
      Product product = createPendingProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.approveProduct(1L);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.APPROVED);
    }

    @Test
    void PENDING_상태가_아니면_승인할_수_없다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.approveProduct(1L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_PENDING);
    }

    @Test
    void 존재하지_않는_상품을_승인하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.approveProduct(999L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 반려_테스트 {

    @Test
    void 상품을_반려할_수_있다() {
      Product product = createPendingProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.rejectProduct(1L, "내용 부족");

      assertThat(product.getStatus()).isEqualTo(ProductStatus.REJECTED);
      assertThat(product.getRejectionReason()).isEqualTo("내용 부족");
    }

    @Test
    void PENDING_상태가_아니면_반려할_수_없다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.rejectProduct(1L, "내용 부족"))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_PENDING);
    }

    @Test
    void 반려_사유가_없으면_예외가_발생한다() {
      Product product = createPendingProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.rejectProduct(1L, null))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.INVALID_REJECTION_REASON);
    }
  }

  @Nested
  class 재제출_테스트 {

    @Test
    void 반려된_상품을_재제출할_수_있다() {
      Product product = createRejectedProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.resubmitProduct(1L, DEFAULT_SELLER_ID);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
      assertThat(product.getRejectionReason()).isNull();
    }

    @Test
    void 소유자가_아니면_재제출할_수_없다() {
      Product product = createRejectedProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.resubmitProduct(1L, OTHER_SELLER_ID))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_OWNED);
    }

    @Test
    void REJECTED_상태가_아니면_재제출할_수_없다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.resubmitProduct(1L, DEFAULT_SELLER_ID))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_REJECTED);
    }
  }

  @Nested
  class 상태_변경_테스트 {

    @Test
    void DRAFT에서_PENDING으로_변경할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.submitForApproval(1L, DEFAULT_SELLER_ID);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
    }

    @Test
    void DRAFT에서_바로_ON_SALE로_변경하면_예외가_발생한다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      // startSale은 SCHEDULED 상태에서만 가능
      assertThatThrownBy(() -> productCommandService.startSale(1L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_STATUS_CHANGE_NOT_ALLOWED);
    }

    @Test
    void 존재하지_않는_상품의_상태를_변경하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.scheduleProduct(999L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 상품_취소_테스트 {

    @Test
    void 상품을_취소할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.cancelProduct(1L, "admin");

      assertThat(product.isCancelled()).isTrue();
      assertThat(product.getDeletedBy()).isEqualTo("admin");
    }

    @Test
    void 이미_취소된_상품을_다시_취소하면_예외가_발생한다() {
      Product product = createProduct(1L);
      product.cancel("admin");
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.cancelProduct(1L, "admin"))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_ALREADY_CANCELLED);
    }

    @Test
    void 존재하지_않는_상품을_취소하면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.cancelProduct(999L, "admin"))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 좌석_현황_테스트 {

    @Test
    void 잔여_좌석을_차감할_수_있다() {
      Product product = createProductWithSeatGrade(1L);
      given(productRepository.findByIdForUpdate(1L)).willReturn(Optional.of(product));

      productCommandService.decreaseAvailableSeats(1L, 10);

      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(20);
    }

    @Test
    void 잔여_좌석보다_많이_차감하면_예외가_발생한다() {
      Product product = createProductWithSeatGrade(1L);
      given(productRepository.findByIdForUpdate(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.decreaseAvailableSeats(1L, 50))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.NOT_ENOUGH_SEATS);
    }

    @Test
    void 좌석_차감시_상품이_없으면_예외가_발생한다() {
      given(productRepository.findByIdForUpdate(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.decreaseAvailableSeats(999L, 10))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 잔여_좌석을_복구할_수_있다() {
      Product product = createProductWithSeatGrade(1L);
      product.decreaseAvailableSeats(10);
      given(productRepository.findByIdForUpdate(1L)).willReturn(Optional.of(product));

      productCommandService.increaseAvailableSeats(1L, 5);

      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(25);
    }

    @Test
    void 좌석_복구시_총_좌석수를_초과하지_않는다() {
      Product product = createProductWithSeatGrade(1L);
      product.decreaseAvailableSeats(5);
      given(productRepository.findByIdForUpdate(1L)).willReturn(Optional.of(product));

      productCommandService.increaseAvailableSeats(1L, 10);

      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(30);
    }

    @Test
    void 좌석_복구시_상품이_없으면_예외가_발생한다() {
      given(productRepository.findByIdForUpdate(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.increaseAvailableSeats(999L, 5))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 통계_테스트 {

    @Test
    void 조회수를_동기화할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.syncViewCount(1L, 1000L);

      assertThat(product.getStats().getViewCount()).isEqualTo(1000L);
    }

    @Test
    void 조회수_동기화시_상품이_없으면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.syncViewCount(999L, 1000L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 예매수를_증가할_수_있다() {
      Product product = createProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.incrementReservationCount(1L);

      assertThat(product.getStats().getReservationCount()).isEqualTo(1);
    }

    @Test
    void 예매수_증가시_상품이_없으면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.incrementReservationCount(999L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 예매수를_감소할_수_있다() {
      Product product = createProduct(1L);
      product.incrementReservationCount();
      product.incrementReservationCount();
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.decrementReservationCount(1L);

      assertThat(product.getStats().getReservationCount()).isEqualTo(1);
    }

    @Test
    void 예매수_감소시_상품이_없으면_예외가_발생한다() {
      given(productRepository.findById(999L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> productCommandService.decrementReservationCount(999L))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
  }

  @Nested
  class 상태_전이_상세_테스트 {

    @Test
    void APPROVED에서_SCHEDULED로_변경할_수_있다() {
      Product product = createApprovedProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.scheduleProduct(1L);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.SCHEDULED);
    }

    @Test
    void SCHEDULED에서_ON_SALE로_변경할_수_있다() {
      Product product = createScheduledProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.startSale(1L);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    void ON_SALE에서_CLOSED로_변경할_수_있다() {
      Product product = createOnSaleProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.closeSale(1L);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.CLOSED);
    }

    @Test
    void CLOSED에서_COMPLETED로_변경할_수_있다() {
      Product product = createClosedProduct(1L);
      given(productRepository.findById(1L)).willReturn(Optional.of(product));

      productCommandService.completeProduct(1L);

      assertThat(product.getStatus()).isEqualTo(ProductStatus.COMPLETED);
    }
  }

  @Nested
  class 등급별_좌석_테스트 {

    @Test
    void 등급별_좌석을_차감할_수_있다() {
      Product product = createProductWithSeatGrade(1L);
      given(productRepository.findByIdForUpdate(1L)).willReturn(Optional.of(product));

      productCommandService.decreaseSeatGradeAvailable(1L, "VIP", 2);

      assertThat(product.getSeatGrades().get(0).getAvailableSeats()).isEqualTo(8);
      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(28);
    }

    @Test
    void 등급별_좌석을_복구할_수_있다() {
      Product product = createProductWithSeatGrade(1L);
      product.decreaseSeatGradeAvailable("VIP", 5);
      given(productRepository.findByIdForUpdate(1L)).willReturn(Optional.of(product));

      productCommandService.increaseSeatGradeAvailable(1L, "VIP", 3);

      assertThat(product.getSeatGrades().get(0).getAvailableSeats()).isEqualTo(8);
      assertThat(product.getSeatSummary().getAvailableSeats()).isEqualTo(28);
    }

    @Test
    void 존재하지_않는_등급으로_차감하면_예외가_발생한다() {
      Product product = createProductWithSeatGrade(1L);
      given(productRepository.findByIdForUpdate(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.decreaseSeatGradeAvailable(1L, "INVALID", 1))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.SEAT_GRADE_NOT_FOUND);
    }

    @Test
    void 등급별_잔여좌석보다_많이_차감하면_예외가_발생한다() {
      Product product = createProductWithSeatGrade(1L);
      given(productRepository.findByIdForUpdate(1L)).willReturn(Optional.of(product));

      assertThatThrownBy(() -> productCommandService.decreaseSeatGradeAvailable(1L, "VIP", 20))
          .isInstanceOf(ProductException.class)
          .extracting(e -> ((ProductException) e).getErrorCode())
          .isEqualTo(ProductErrorCode.NOT_ENOUGH_SEATS);
    }
  }

  // ========== Helper Methods ==========

  private Product createProduct(Long id) {
    Schedule schedule = new Schedule(startAt, endAt);
    SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);
    Venue venue =
        new Venue(
            DEFAULT_STAGE_ID,
            DEFAULT_STAGE_NAME,
            DEFAULT_ART_HALL_ID,
            DEFAULT_ART_HALL_NAME,
            DEFAULT_ART_HALL_ADDRESS);

    Product product =
        Product.create(
            DEFAULT_SELLER_ID,
            DEFAULT_PRODUCT_NAME,
            DEFAULT_PRODUCT_TYPE,
            DEFAULT_RUNNING_TIME,
            schedule,
            saleSchedule,
            venue,
            ProductContent.empty(),
            AgeRestriction.defaultRestriction(),
            BookingPolicy.defaultPolicy(),
            AdmissionPolicy.defaultPolicy(),
            RefundPolicy.defaultPolicy());
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  private Product createPendingProduct(Long id) {
    Product product = createProduct(id);
    product.changeStatus(ProductStatus.PENDING);
    return product;
  }

  private Product createRejectedProduct(Long id) {
    Product product = createPendingProduct(id);
    product.reject("테스트 반려 사유");
    return product;
  }

  private Product createApprovedProduct(Long id) {
    Product product = createPendingProduct(id);
    product.approve();
    return product;
  }

  private Product createScheduledProduct(Long id) {
    Product product = createApprovedProduct(id);
    product.changeStatus(ProductStatus.SCHEDULED);
    return product;
  }

  private Product createOnSaleProduct(Long id) {
    Product product = createScheduledProduct(id);
    product.changeStatus(ProductStatus.ON_SALE);
    return product;
  }

  private Product createClosedProduct(Long id) {
    Product product = createOnSaleProduct(id);
    product.changeStatus(ProductStatus.CLOSED);
    return product;
  }

  private Product createProductWithSeatGrade(Long id) {
    Product product = createProduct(id);
    product.addSeatGrade("VIP", 150000L, 10, 1);
    product.addSeatGrade("R", 120000L, 20, 2);
    return product;
  }

  private ProductCreateCommand createDefaultCommand() {
    return ProductCreateCommand.builder()
        .sellerId(DEFAULT_SELLER_ID)
        .name(DEFAULT_PRODUCT_NAME)
        .productType(DEFAULT_PRODUCT_TYPE)
        .runningTime(DEFAULT_RUNNING_TIME)
        .startAt(startAt)
        .endAt(endAt)
        .saleStartAt(saleStartAt)
        .saleEndAt(saleEndAt)
        .stageId(DEFAULT_STAGE_ID)
        .stageName(DEFAULT_STAGE_NAME)
        .artHallId(DEFAULT_ART_HALL_ID)
        .artHallName(DEFAULT_ART_HALL_NAME)
        .artHallAddress(DEFAULT_ART_HALL_ADDRESS)
        .seatGradeInfos(createDefaultSeatGradeInfos())
        .seatCreateInfos(createDefaultSeatCreateInfos())
        .build();
  }

  private List<SeatGradeInfo> createDefaultSeatGradeInfos() {
    return List.of(new SeatGradeInfo("VIP", 150000L, 2), new SeatGradeInfo("R", 120000L, 2));
  }

  private List<SeatCreateInfo> createDefaultSeatCreateInfos() {
    return List.of(
        new SeatCreateInfo("A1", "VIP", 150000L),
        new SeatCreateInfo("A2", "VIP", 150000L),
        new SeatCreateInfo("B1", "R", 120000L),
        new SeatCreateInfo("B2", "R", 120000L));
  }
}
