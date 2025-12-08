package com.tickatch.product_service.product.application.service;

import com.tickatch.product_service.product.application.messaging.ProductEventPublisher;
import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.SaleSchedule;
import com.tickatch.product_service.product.domain.vo.Schedule;
import com.tickatch.product_service.product.domain.vo.Venue;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 명령 서비스.
 *
 * <p>상품의 생성, 수정, 삭제 등 상태 변경과 관련된 비즈니스 로직을 처리한다. 상품 취소 시 관련 서비스로 이벤트를 발행한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductQueryService
 * @see ProductEventPublisher
 */
@Transactional
@Service
@RequiredArgsConstructor
public class ProductCommandService {

  private final ProductRepository productRepository;
  private final ProductEventPublisher eventPublisher;

  /**
   * 상품을 생성한다.
   *
   * <p>새 상품은 DRAFT 상태로 생성된다.
   *
   * @param sellerId 판매자 ID
   * @param name 상품명
   * @param productType 상품 타입
   * @param runningTime 상영 시간 (분)
   * @param startAt 행사 시작 일시
   * @param endAt 행사 종료 일시
   * @param saleStartAt 예매 시작 일시
   * @param saleEndAt 예매 종료 일시
   * @param stageId 스테이지 ID
   * @param stageName 스테이지명
   * @param artHallId 아트홀 ID
   * @param artHallName 아트홀명
   * @param artHallAddress 아트홀 주소
   * @return 생성된 상품 ID
   */
  public Long createProduct(
      String sellerId,
      String name,
      ProductType productType,
      Integer runningTime,
      LocalDateTime startAt,
      LocalDateTime endAt,
      LocalDateTime saleStartAt,
      LocalDateTime saleEndAt,
      Long stageId,
      String stageName,
      Long artHallId,
      String artHallName,
      String artHallAddress) {
    Schedule schedule = new Schedule(startAt, endAt);
    SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);
    Venue venue = new Venue(stageId, stageName, artHallId, artHallName, artHallAddress);

    Product product =
        Product.create(sellerId, name, productType, runningTime, schedule, saleSchedule, venue);
    Product saved = productRepository.save(product);
    return saved.getId();
  }

  /**
   * 상품 정보를 수정한다.
   *
   * <p>DRAFT 또는 REJECTED 상태에서만 수정 가능하다.
   *
   * @param productId 수정할 상품 ID
   * @param sellerId 요청한 판매자 ID
   * @param name 상품명
   * @param productType 상품 타입
   * @param runningTime 상영 시간 (분)
   * @param startAt 행사 시작 일시
   * @param endAt 행사 종료 일시
   * @param saleStartAt 예매 시작 일시
   * @param saleEndAt 예매 종료 일시
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 소유자가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_OWNED})
   * @throws ProductException 수정 불가능한 상태인 경우 ({@link ProductErrorCode#PRODUCT_NOT_EDITABLE})
   */
  public void updateProduct(
      Long productId,
      String sellerId,
      String name,
      ProductType productType,
      Integer runningTime,
      LocalDateTime startAt,
      LocalDateTime endAt,
      LocalDateTime saleStartAt,
      LocalDateTime saleEndAt) {
    Product product = findProductById(productId);
    validateOwnership(product, sellerId);

    Schedule schedule = new Schedule(startAt, endAt);
    SaleSchedule saleSchedule = new SaleSchedule(saleStartAt, saleEndAt);
    product.update(name, productType, runningTime, schedule, saleSchedule);
  }

  /**
   * 상품의 장소를 변경한다.
   *
   * <p>행사 시작 전에만 장소를 변경할 수 있다.
   *
   * @param productId 상품 ID
   * @param sellerId 요청한 판매자 ID
   * @param stageId 스테이지 ID
   * @param stageName 스테이지명
   * @param artHallId 아트홀 ID
   * @param artHallName 아트홀명
   * @param artHallAddress 아트홀 주소
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 소유자가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_OWNED})
   * @throws ProductException 장소 변경이 불가능한 경우 ({@link ProductErrorCode#VENUE_CHANGE_NOT_ALLOWED})
   */
  public void changeVenue(
      Long productId,
      String sellerId,
      Long stageId,
      String stageName,
      Long artHallId,
      String artHallName,
      String artHallAddress) {
    Product product = findProductById(productId);
    validateOwnership(product, sellerId);

    Venue venue = new Venue(stageId, stageName, artHallId, artHallName, artHallAddress);
    product.changeVenue(venue);
  }

  // ========== 심사 관련 ==========

  /**
   * 상품을 심사 요청한다.
   *
   * <p>DRAFT 상태에서 PENDING 상태로 변경한다.
   *
   * @param productId 상품 ID
   * @param sellerId 요청한 판매자 ID
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 소유자가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_OWNED})
   * @throws ProductException 상태 변경이 불가능한 경우 ({@link
   *     ProductErrorCode#PRODUCT_STATUS_CHANGE_NOT_ALLOWED})
   */
  public void submitForApproval(Long productId, String sellerId) {
    Product product = findProductById(productId);
    validateOwnership(product, sellerId);

    product.changeStatus(ProductStatus.PENDING);
  }

  /**
   * 상품을 승인한다.
   *
   * <p>PENDING 상태에서 APPROVED 상태로 변경한다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException PENDING 상태가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_PENDING})
   */
  public void approveProduct(Long productId) {
    Product product = findProductById(productId);
    product.approve();
  }

  /**
   * 상품을 반려한다.
   *
   * <p>PENDING 상태에서 REJECTED 상태로 변경한다.
   *
   * @param productId 상품 ID
   * @param reason 반려 사유
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException PENDING 상태가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_PENDING})
   * @throws ProductException 반려 사유가 없는 경우 ({@link ProductErrorCode#INVALID_REJECTION_REASON})
   */
  public void rejectProduct(Long productId, String reason) {
    Product product = findProductById(productId);
    product.reject(reason);
  }

  /**
   * 반려된 상품을 재제출한다.
   *
   * <p>REJECTED 상태에서 DRAFT 상태로 변경한다.
   *
   * @param productId 상품 ID
   * @param sellerId 요청한 판매자 ID
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 소유자가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_OWNED})
   * @throws ProductException REJECTED 상태가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_REJECTED})
   */
  public void resubmitProduct(Long productId, String sellerId) {
    Product product = findProductById(productId);
    validateOwnership(product, sellerId);

    product.resubmit();
  }

  // ========== 상태 변경 ==========

  /**
   * 상품의 상태를 변경한다.
   *
   * <p>상태 전이 규칙에 따라 유효한 상태로만 변경할 수 있다.
   *
   * @param productId 상품 ID
   * @param newStatus 변경할 상태
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 상태 변경이 불가능한 경우 ({@link
   *     ProductErrorCode#PRODUCT_STATUS_CHANGE_NOT_ALLOWED})
   */
  public void changeStatus(Long productId, ProductStatus newStatus) {
    Product product = findProductById(productId);
    product.changeStatus(newStatus);
  }

  /**
   * 상품을 취소한다.
   *
   * <p>상품 취소 후 ReservationSeat, Reservation, Ticket 서비스로 취소 이벤트를 발행한다.
   *
   * @param productId 취소할 상품 ID
   * @param cancelledBy 취소 요청자 ID
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 이미 취소된 상품인 경우 ({@link ProductErrorCode#PRODUCT_ALREADY_CANCELLED})
   * @throws ProductException 이벤트 발행 실패 시 ({@link ProductErrorCode#EVENT_PUBLISH_FAILED})
   */
  public void cancelProduct(Long productId, String cancelledBy) {
    Product product = findProductById(productId);
    product.cancel(cancelledBy);

    eventPublisher.publishCancelled(product);
  }

  // ========== 좌석 관련 ==========

  /**
   * 좌석 현황을 초기화한다.
   *
   * @param productId 상품 ID
   * @param totalSeats 총 좌석 수
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   */
  public void initializeSeatSummary(Long productId, int totalSeats) {
    Product product = findProductByIdForUpdate(productId);
    product.initializeSeatSummary(totalSeats);
  }

  /**
   * 잔여 좌석을 차감한다.
   *
   * <p>예매 시 호출된다. 동시성 제어를 위해 비관적 락을 사용한다.
   *
   * @param productId 상품 ID
   * @param count 차감할 좌석 수
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 잔여 좌석이 부족한 경우 ({@link ProductErrorCode#NOT_ENOUGH_SEATS})
   */
  public void decreaseAvailableSeats(Long productId, int count) {
    Product product = findProductByIdForUpdate(productId);
    product.decreaseAvailableSeats(count);
  }

  /**
   * 잔여 좌석을 복구한다.
   *
   * <p>예매 취소 시 호출된다. 동시성 제어를 위해 비관적 락을 사용한다.
   *
   * @param productId 상품 ID
   * @param count 복구할 좌석 수
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   */
  public void increaseAvailableSeats(Long productId, int count) {
    Product product = findProductByIdForUpdate(productId);
    product.increaseAvailableSeats(count);
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
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   */
  public void syncViewCount(Long productId, Long viewCount) {
    Product product = findProductById(productId);
    product.syncViewCount(viewCount);
  }

  /**
   * 예매 수를 증가한다.
   *
   * @param productId 상품 ID
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
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
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   */
  public void decrementReservationCount(Long productId) {
    Product product = findProductById(productId);
    product.decrementReservationCount();
  }

  // ========== Private Methods ==========

  /**
   * 상품 ID로 상품을 조회한다.
   *
   * @param productId 상품 ID
   * @return 조회된 상품 엔티티
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   */
  private Product findProductById(Long productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, productId));
  }

  /**
   * 비관적 락을 걸고 상품을 조회한다.
   *
   * <p>좌석 차감/복구 등 동시성 제어가 필요한 작업에서 사용한다.
   *
   * @param productId 상품 ID
   * @return 조회된 상품 엔티티
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   */
  private Product findProductByIdForUpdate(Long productId) {
    return productRepository
        .findByIdForUpdate(productId)
        .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, productId));
  }

  /**
   * 상품 소유권을 검증한다.
   *
   * @param product 상품
   * @param sellerId 판매자 ID
   * @throws ProductException 소유자가 아닌 경우 ({@link ProductErrorCode#PRODUCT_NOT_OWNED})
   */
  private void validateOwnership(Product product, String sellerId) {
    if (!product.isOwnedBy(sellerId)) {
      throw new ProductException(ProductErrorCode.PRODUCT_NOT_OWNED);
    }
  }
}
