package com.tickatch.product_service.product.application.service;

import com.tickatch.product_service.product.application.dto.ProductCreateCommand;
import com.tickatch.product_service.product.application.dto.ProductCreateCommand.SeatCreateInfo;
import com.tickatch.product_service.product.application.dto.ProductCreateCommand.SeatGradeInfo;
import com.tickatch.product_service.product.application.dto.ProductUpdateCommand;
import com.tickatch.product_service.product.application.dto.SeatCreateRequest;
import com.tickatch.product_service.product.application.messaging.ProductEventPublisher;
import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.AdmissionPolicy;
import com.tickatch.product_service.product.domain.vo.AgeRating;
import com.tickatch.product_service.product.domain.vo.AgeRestriction;
import com.tickatch.product_service.product.domain.vo.BookingPolicy;
import com.tickatch.product_service.product.domain.vo.ProductContent;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.RefundPolicy;
import com.tickatch.product_service.product.domain.vo.SaleSchedule;
import com.tickatch.product_service.product.domain.vo.Schedule;
import com.tickatch.product_service.product.domain.vo.Venue;
import com.tickatch.product_service.product.infrastructure.client.ReservationSeatClient;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 명령 서비스.
 *
 * <p>상품의 생성, 수정, 삭제 등 상태 변경과 관련된 비즈니스 로직을 처리한다.
 * 서비스는 "명세서 조립자" 역할을 수행하며, Command에서 받은 데이터를 VO로 조립하여
 * 도메인에 전달한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductQueryService
 * @see ProductEventPublisher
 */
@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ProductCommandService {

  private final ProductRepository productRepository;
  private final ProductEventPublisher eventPublisher;
  private final ReservationSeatClient reservationSeatClient;

  // ========== 생성 ==========

  /**
   * 상품을 생성한다.
   *
   * <p>새 상품은 DRAFT 상태로 생성된다. 서비스가 VO를 조립하여 도메인에 주입한다.
   * 생성 후 SeatGrade를 추가하고 ReservationSeat 서비스에 개별 좌석 생성을 요청한다.
   *
   * @param command 상품 생성 명령
   * @return 생성된 상품 ID
   * @throws IllegalArgumentException Command 검증 실패 시
   * @throws ProductException 도메인 검증 실패 시
   */
  public Long createProduct(ProductCreateCommand command) {
    // 1. Command 검증
    command.validateRequired();
    command.validateValueObjectSets();
    command.validateSeatConsistency();

    // 2. VO 조립
    Schedule schedule = new Schedule(command.getStartAt(), command.getEndAt());
    SaleSchedule saleSchedule = new SaleSchedule(command.getSaleStartAt(), command.getSaleEndAt());
    Venue venue = assembleVenue(command);
    ProductContent content = assembleContent(command);
    AgeRestriction ageRestriction = assembleAgeRestriction(command);
    BookingPolicy bookingPolicy = assembleBookingPolicy(command);
    AdmissionPolicy admissionPolicy = assembleAdmissionPolicy(command);
    RefundPolicy refundPolicy = assembleRefundPolicy(command);

    // 3. Product 생성
    Product product = Product.create(
        command.getSellerId(),
        command.getName(),
        command.getProductType(),
        command.getRunningTime(),
        schedule,
        saleSchedule,
        venue,
        content,
        ageRestriction,
        bookingPolicy,
        admissionPolicy,
        refundPolicy);

    // 4. SeatGrade 추가 (배열 순서대로 displayOrder)
    List<SeatGradeInfo> gradeInfos = command.getSeatGradeInfos();
    for (int i = 0; i < gradeInfos.size(); i++) {
      SeatGradeInfo info = gradeInfos.get(i);
      product.addSeatGrade(info.gradeName(), info.price(), info.totalSeats(), i + 1);
    }

    // 5. 저장
    Product saved = productRepository.save(product);

    // 6. ReservationSeat 서비스에 개별 좌석 생성 요청
    createReservationSeats(saved.getId(), command.getSeatCreateInfos());

    log.info("상품 생성 완료. productId: {}, seatGrades: {}, seatCreateInfos: {}",
        saved.getId(), gradeInfos.size(), command.getSeatCreateInfos().size());

    return saved.getId();
  }

  // ========== 수정 ==========

  /**
   * 상품 정보를 수정한다.
   *
   * <p>DRAFT 또는 REJECTED 상태에서만 수정 가능하다.
   * null이 아닌 필드만 수정되며, 값객체는 세트 단위로 수정된다.
   *
   * @param command 상품 수정 명령
   * @throws IllegalArgumentException Command 검증 실패 시
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 소유자가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_OWNED})
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   */
  public void updateProduct(ProductUpdateCommand command) {
    // 1. Command 검증
    command.validateRequired();
    command.validateValueObjectSets();
    command.validateSeatConsistency();

    // 2. 상품 조회 및 소유권 검증
    Product product = findProductById(command.getProductId());
    validateOwnership(product, command.getSellerId());

    // 3. 기본 정보 수정
    if (command.hasBasicInfo()) {
      updateBasicInfo(product, command);
    }

    // 4. 일정 수정
    if (command.hasSchedule()) {
      Schedule schedule = new Schedule(command.getStartAt(), command.getEndAt());
      SaleSchedule saleSchedule = new SaleSchedule(command.getSaleStartAt(), command.getSaleEndAt());
      product.update(
          command.getName() != null ? command.getName() : product.getName(),
          command.getProductType() != null ? command.getProductType() : product.getProductType(),
          command.getRunningTime() != null ? command.getRunningTime() : product.getRunningTime(),
          schedule,
          saleSchedule);
    }

    // 5. 장소 수정
    if (command.hasVenue()) {
      Venue venue = assembleVenueFromUpdate(command);
      product.changeVenue(venue);
    }

    // 6. 콘텐츠 수정
    if (command.hasContent()) {
      ProductContent content = assembleContentFromUpdate(command, product);
      product.updateContent(content);
    }

    // 7. 관람 제한 수정
    if (command.hasAgeRestriction()) {
      AgeRestriction ageRestriction = assembleAgeRestrictionFromUpdate(command);
      product.updateAgeRestriction(ageRestriction);
    }

    // 8. 예매 정책 수정
    if (command.hasBookingPolicy()) {
      BookingPolicy bookingPolicy = assembleBookingPolicyFromUpdate(command);
      product.updateBookingPolicy(bookingPolicy);
    }

    // 9. 입장 정책 수정
    if (command.hasAdmissionPolicy()) {
      AdmissionPolicy admissionPolicy = assembleAdmissionPolicyFromUpdate(command);
      product.updateAdmissionPolicy(admissionPolicy);
    }

    // 10. 환불 정책 수정
    if (command.hasRefundPolicy()) {
      RefundPolicy refundPolicy = assembleRefundPolicyFromUpdate(command);
      product.updateRefundPolicy(refundPolicy);
    }

    // 11. 좌석 정보 수정 (전체 교체)
    if (command.hasSeatGradeInfos()) {
      updateSeatGrades(product, command);
    }

    log.info("상품 수정 완료. productId: {}", command.getProductId());
  }

  /**
   * 기본 정보만 수정한다 (일정 변경 없이).
   */
  private void updateBasicInfo(Product product, ProductUpdateCommand command) {
    // 기본 정보만 변경하고 일정은 기존 유지
    if (command.getName() != null || command.getProductType() != null || command.getRunningTime() != null) {
      Schedule currentSchedule = product.getSchedule();
      SaleSchedule currentSaleSchedule = product.getSaleSchedule();
      product.update(
          command.getName() != null ? command.getName() : product.getName(),
          command.getProductType() != null ? command.getProductType() : product.getProductType(),
          command.getRunningTime() != null ? command.getRunningTime() : product.getRunningTime(),
          currentSchedule,
          currentSaleSchedule);
    }
  }

  /**
   * 좌석 등급을 전체 교체한다.
   */
  private void updateSeatGrades(Product product, ProductUpdateCommand command) {
    // 기존 SeatGrade 모두 제거
    List<Long> existingIds = product.getSeatGrades().stream()
        .map(sg -> sg.getId())
        .toList();
    for (Long id : existingIds) {
      product.removeSeatGrade(id);
    }

    // 새 SeatGrade 추가
    List<ProductUpdateCommand.SeatGradeInfo> gradeInfos = command.getSeatGradeInfos();
    for (int i = 0; i < gradeInfos.size(); i++) {
      ProductUpdateCommand.SeatGradeInfo info = gradeInfos.get(i);
      product.addSeatGrade(info.gradeName(), info.price(), info.totalSeats(), i + 1);
    }

    // ReservationSeat 서비스에 좌석 재생성 요청
    // TODO: 기존 좌석 삭제 API 호출 필요 (추후 구현)
    List<SeatCreateRequest.SeatInfo> seatInfos = command.getSeatCreateInfos().stream()
        .map(info -> SeatCreateRequest.SeatInfo.builder()
            .seatNumber(info.seatNumber())
            .grade(info.grade())
            .price(info.price())
            .build())
        .collect(Collectors.toList());

    SeatCreateRequest request = SeatCreateRequest.builder()
        .productId(product.getId())
        .seatCreateInfos(seatInfos)
        .build();

    reservationSeatClient.createSeats(request);
    log.info("좌석 등급 수정 완료. productId: {}, newGrades: {}", product.getId(), gradeInfos.size());
  }

  // ========== 심사 관련 ==========

  /**
   * 상품을 심사 요청한다.
   *
   * <p>DRAFT 상태에서 PENDING 상태로 변경한다.
   *
   * @param productId 상품 ID
   * @param sellerId 요청한 판매자 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 소유자가 아닌 경우
   * @throws ProductException 상태 변경이 불가능한 경우
   */
  public void submitForApproval(Long productId, String sellerId) {
    Product product = findProductById(productId);
    validateOwnership(product, sellerId);

    product.changeStatus(ProductStatus.PENDING);
    log.info("심사 요청 완료. productId: {}", productId);
  }

  /**
   * 상품을 승인한다.
   *
   * <p>PENDING 상태에서 APPROVED 상태로 변경한다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException PENDING 상태가 아닌 경우
   */
  public void approveProduct(Long productId) {
    Product product = findProductById(productId);
    product.approve();
    log.info("상품 승인 완료. productId: {}", productId);
  }

  /**
   * 상품을 반려한다.
   *
   * <p>PENDING 상태에서 REJECTED 상태로 변경한다.
   *
   * @param productId 상품 ID
   * @param reason 반려 사유
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException PENDING 상태가 아닌 경우
   * @throws ProductException 반려 사유가 없는 경우
   */
  public void rejectProduct(Long productId, String reason) {
    Product product = findProductById(productId);
    product.reject(reason);
    log.info("상품 반려 완료. productId: {}, reason: {}", productId, reason);
  }

  /**
   * 반려된 상품을 재제출한다.
   *
   * <p>REJECTED 상태에서 DRAFT 상태로 변경한다.
   *
   * @param productId 상품 ID
   * @param sellerId 요청한 판매자 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 소유자가 아닌 경우
   * @throws ProductException REJECTED 상태가 아닌 경우
   */
  public void resubmitProduct(Long productId, String sellerId) {
    Product product = findProductById(productId);
    validateOwnership(product, sellerId);

    product.resubmit();
    log.info("상품 재제출 완료. productId: {}", productId);
  }

  // ========== 상태 변경 ==========

  /**
   * 상품을 판매 예정 상태로 변경한다.
   *
   * <p>APPROVED 상태에서 SCHEDULED 상태로 변경한다.
   * 예매 시작일이 다가오면 스케줄러가 호출한다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 상태 변경이 불가능한 경우
   */
  public void scheduleProduct(Long productId) {
    Product product = findProductById(productId);
    product.changeStatus(ProductStatus.SCHEDULED);
    log.info("상품 판매 예정 상태 변경. productId: {}", productId);
  }

  /**
   * 상품을 판매중 상태로 변경한다.
   *
   * <p>SCHEDULED 상태에서 ON_SALE 상태로 변경한다.
   * 예매 시작일이 되면 스케줄러가 호출한다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 상태 변경이 불가능한 경우
   */
  public void startSale(Long productId) {
    Product product = findProductById(productId);
    product.changeStatus(ProductStatus.ON_SALE);
    log.info("상품 판매 시작. productId: {}", productId);
  }

  /**
   * 상품의 판매를 종료한다.
   *
   * <p>ON_SALE 상태에서 CLOSED 상태로 변경한다.
   * 예매 종료일이 되거나 행사 시작일이 되면 스케줄러가 호출한다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 상태 변경이 불가능한 경우
   */
  public void closeSale(Long productId) {
    Product product = findProductById(productId);
    product.changeStatus(ProductStatus.CLOSED);
    log.info("상품 판매 종료. productId: {}", productId);
  }

  /**
   * 상품을 완료 상태로 변경한다.
   *
   * <p>CLOSED 상태에서 COMPLETED 상태로 변경한다.
   * 행사가 종료되면 스케줄러가 호출한다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 상태 변경이 불가능한 경우
   */
  public void completeProduct(Long productId) {
    Product product = findProductById(productId);
    product.changeStatus(ProductStatus.COMPLETED);
    log.info("상품 완료 처리. productId: {}", productId);
  }

  /**
   * 상품을 취소한다.
   *
   * <p>상품 취소 후 ReservationSeat, Reservation, Ticket 서비스로 취소 이벤트를 발행한다.
   *
   * @param productId 취소할 상품 ID
   * @param cancelledBy 취소 요청자 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 이미 취소된 상품인 경우
   * @throws ProductException 이벤트 발행 실패 시
   */
  public void cancelProduct(Long productId, String cancelledBy) {
    Product product = findProductById(productId);
    product.cancel(cancelledBy);

    eventPublisher.publishCancelled(product);
    log.info("상품 취소 완료. productId: {}, cancelledBy: {}", productId, cancelledBy);
  }

  // ========== 좌석 관련 (총합) ==========

  /**
   * 잔여 좌석을 차감한다 (총합).
   *
   * <p>예매 시 호출된다. 동시성 제어를 위해 비관적 락을 사용한다.
   *
   * @param productId 상품 ID
   * @param count 차감할 좌석 수
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 잔여 좌석이 부족한 경우
   */
  public void decreaseAvailableSeats(Long productId, int count) {
    Product product = findProductByIdForUpdate(productId);
    product.decreaseAvailableSeats(count);
    log.debug("잔여 좌석 차감 (총합). productId: {}, count: {}", productId, count);
  }

  /**
   * 잔여 좌석을 복구한다 (총합).
   *
   * <p>예매 취소 시 호출된다. 동시성 제어를 위해 비관적 락을 사용한다.
   *
   * @param productId 상품 ID
   * @param count 복구할 좌석 수
   * @throws ProductException 상품을 찾을 수 없는 경우
   */
  public void increaseAvailableSeats(Long productId, int count) {
    Product product = findProductByIdForUpdate(productId);
    product.increaseAvailableSeats(count);
    log.debug("잔여 좌석 복구 (총합). productId: {}, count: {}", productId, count);
  }

  // ========== 좌석 관련 (등급별) ==========

  /**
   * 등급별 잔여 좌석을 차감한다.
   *
   * <p>예매 시 호출된다. SeatGrade와 SeatSummary 모두 갱신된다.
   * 동시성 제어를 위해 비관적 락을 사용한다.
   *
   * @param productId 상품 ID
   * @param gradeName 등급명
   * @param count 차감할 좌석 수
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 해당 등급이 없는 경우
   * @throws ProductException 잔여 좌석이 부족한 경우
   */
  public void decreaseSeatGradeAvailable(Long productId, String gradeName, int count) {
    Product product = findProductByIdForUpdate(productId);
    product.decreaseSeatGradeAvailable(gradeName, count);
    log.debug("등급별 좌석 차감. productId: {}, grade: {}, count: {}", productId, gradeName, count);
  }

  /**
   * 등급별 잔여 좌석을 복구한다.
   *
   * <p>예매 취소 시 호출된다. SeatGrade와 SeatSummary 모두 갱신된다.
   * 동시성 제어를 위해 비관적 락을 사용한다.
   *
   * @param productId 상품 ID
   * @param gradeName 등급명
   * @param count 복구할 좌석 수
   * @throws ProductException 상품을 찾을 수 없는 경우
   * @throws ProductException 해당 등급이 없는 경우
   */
  public void increaseSeatGradeAvailable(Long productId, String gradeName, int count) {
    Product product = findProductByIdForUpdate(productId);
    product.increaseSeatGradeAvailable(gradeName, count);
    log.debug("등급별 좌석 복구. productId: {}, grade: {}, count: {}", productId, gradeName, count);
  }

  // ========== 통계 관련 ==========

  /**
   * 조회수를 1 증가한다.
   *
   * <p>상품 조회 시 비동기로 호출된다.
   *
   * @param productId 상품 ID
   */
  @Async
  public void incrementViewCount(Long productId) {
    productRepository.findById(productId)
        .ifPresent(Product::incrementViewCount);
  }

  /**
   * 조회수를 동기화한다.
   *
   * <p>Redis에서 배치로 동기화할 때 사용한다.
   *
   * @param productId 상품 ID
   * @param viewCount 동기화할 조회수
   * @throws ProductException 상품을 찾을 수 없는 경우
   */
  public void syncViewCount(Long productId, Long viewCount) {
    Product product = findProductById(productId);
    product.syncViewCount(viewCount);
  }

  /**
   * 예매 수를 증가한다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   */
  public void incrementReservationCount(Long productId) {
    Product product = findProductById(productId);
    product.incrementReservationCount();
  }

  /**
   * 예매 수를 감소한다.
   *
   * <p>예매 취소 시 호출된다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우
   */
  public void decrementReservationCount(Long productId) {
    Product product = findProductById(productId);
    product.decrementReservationCount();
  }

  // ========== VO 조립 (Create) ==========

  private Venue assembleVenue(ProductCreateCommand command) {
    return new Venue(
        command.getStageId(),
        command.getStageName(),
        command.getArtHallId(),
        command.getArtHallName(),
        command.getArtHallAddress());
  }

  private ProductContent assembleContent(ProductCreateCommand command) {
    if (!command.hasContent()) {
      return ProductContent.empty();
    }
    return new ProductContent(
        command.getDescription(),
        command.getPosterImageUrl(),
        command.getDetailImageUrls(),
        command.getCastInfo(),
        command.getNotice(),
        command.getOrganizer(),
        command.getAgency());
  }

  private AgeRestriction assembleAgeRestriction(ProductCreateCommand command) {
    if (!command.hasAgeRestriction()) {
      return AgeRestriction.defaultRestriction();
    }
    return new AgeRestriction(
        command.getAgeRating(),
        command.getRestrictionNotice());
  }

  private BookingPolicy assembleBookingPolicy(ProductCreateCommand command) {
    if (!command.hasBookingPolicy()) {
      return BookingPolicy.defaultPolicy();
    }
    return new BookingPolicy(
        command.getMaxTicketsPerPerson(),
        command.getIdVerificationRequired() != null ? command.getIdVerificationRequired() : false,
        command.getTransferable() != null ? command.getTransferable() : true);
  }

  private AdmissionPolicy assembleAdmissionPolicy(ProductCreateCommand command) {
    if (!command.hasAdmissionPolicy()) {
      return AdmissionPolicy.defaultPolicy();
    }
    return new AdmissionPolicy(
        command.getAdmissionMinutesBefore(),
        command.getLateEntryAllowed() != null ? command.getLateEntryAllowed() : true,
        command.getLateEntryNotice(),
        command.getHasIntermission() != null ? command.getHasIntermission() : false,
        command.getIntermissionMinutes(),
        command.getPhotographyAllowed() != null ? command.getPhotographyAllowed() : false,
        command.getFoodAllowed() != null ? command.getFoodAllowed() : false);
  }

  private RefundPolicy assembleRefundPolicy(ProductCreateCommand command) {
    if (!command.hasRefundPolicy()) {
      return RefundPolicy.defaultPolicy();
    }
    return new RefundPolicy(
        command.getCancellable(),
        command.getCancelDeadlineDays() != null ? command.getCancelDeadlineDays() : 1,
        command.getRefundPolicyText());
  }

  // ========== VO 조립 (Update) ==========

  private Venue assembleVenueFromUpdate(ProductUpdateCommand command) {
    return new Venue(
        command.getStageId(),
        command.getStageName(),
        command.getArtHallId(),
        command.getArtHallName(),
        command.getArtHallAddress());
  }

  private ProductContent assembleContentFromUpdate(ProductUpdateCommand command, Product product) {
    ProductContent current = product.getContent();
    return new ProductContent(
        command.getDescription() != null ? command.getDescription() :
            (current != null ? current.getDescription() : null),
        command.getPosterImageUrl() != null ? command.getPosterImageUrl() :
            (current != null ? current.getPosterImageUrl() : null),
        command.getDetailImageUrls() != null ? command.getDetailImageUrls() :
            (current != null ? current.getDetailImageUrls() : null),
        command.getCastInfo() != null ? command.getCastInfo() :
            (current != null ? current.getCastInfo() : null),
        command.getNotice() != null ? command.getNotice() :
            (current != null ? current.getNotice() : null),
        command.getOrganizer() != null ? command.getOrganizer() :
            (current != null ? current.getOrganizer() : null),
        command.getAgency() != null ? command.getAgency() :
            (current != null ? current.getAgency() : null));
  }

  private AgeRestriction assembleAgeRestrictionFromUpdate(ProductUpdateCommand command) {
    return new AgeRestriction(
        command.getAgeRating(),
        command.getRestrictionNotice());
  }

  private BookingPolicy assembleBookingPolicyFromUpdate(ProductUpdateCommand command) {
    return new BookingPolicy(
        command.getMaxTicketsPerPerson(),
        command.getIdVerificationRequired() != null ? command.getIdVerificationRequired() : false,
        command.getTransferable() != null ? command.getTransferable() : true);
  }

  private AdmissionPolicy assembleAdmissionPolicyFromUpdate(ProductUpdateCommand command) {
    return new AdmissionPolicy(
        command.getAdmissionMinutesBefore(),
        command.getLateEntryAllowed() != null ? command.getLateEntryAllowed() : true,
        command.getLateEntryNotice(),
        command.getHasIntermission() != null ? command.getHasIntermission() : false,
        command.getIntermissionMinutes(),
        command.getPhotographyAllowed() != null ? command.getPhotographyAllowed() : false,
        command.getFoodAllowed() != null ? command.getFoodAllowed() : false);
  }

  private RefundPolicy assembleRefundPolicyFromUpdate(ProductUpdateCommand command) {
    return new RefundPolicy(
        command.getCancellable(),
        command.getCancelDeadlineDays() != null ? command.getCancelDeadlineDays() : 1,
        command.getRefundPolicyText());
  }

  // ========== ReservationSeat 연동 ==========

  /**
   * ReservationSeat 서비스에 개별 좌석 생성을 요청한다.
   *
   * @param productId 상품 ID
   * @param seatCreateInfos 개별 좌석 정보 리스트
   */
  private void createReservationSeats(Long productId, List<SeatCreateInfo> seatCreateInfos) {
    List<SeatCreateRequest.SeatInfo> seatInfos = seatCreateInfos.stream()
        .map(info -> SeatCreateRequest.SeatInfo.builder()
            .seatNumber(info.seatNumber())
            .grade(info.grade())
            .price(info.price())
            .build())
        .collect(Collectors.toList());

    SeatCreateRequest request = SeatCreateRequest.builder()
        .productId(productId)
        .seatCreateInfos(seatInfos)
        .build();

    reservationSeatClient.createSeats(request);
    log.info("ReservationSeat 생성 요청 완료. productId: {}, count: {}", productId, seatInfos.size());
  }

  // ========== Private Methods ==========

  private Product findProductById(Long productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, productId));
  }

  private Product findProductByIdForUpdate(Long productId) {
    return productRepository
        .findByIdForUpdate(productId)
        .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, productId));
  }

  private void validateOwnership(Product product, String sellerId) {
    if (!product.isOwnedBy(sellerId)) {
      throw new ProductException(ProductErrorCode.PRODUCT_NOT_OWNED);
    }
  }
}