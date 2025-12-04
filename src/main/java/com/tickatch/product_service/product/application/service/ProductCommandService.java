package com.tickatch.product_service.product.application.service;

import com.tickatch.product_service.product.application.messaging.ProductEventPublisher;
import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.vo.Schedule;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
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
   * @param name 상품명
   * @param productType 상품 타입
   * @param runningTime 상영 시간 (분)
   * @param startAt 시작 일시
   * @param endAt 종료 일시
   * @param stageId 스테이지 ID
   * @return 생성된 상품 ID
   */
  public Long createProduct(
      String name,
      ProductType productType,
      Integer runningTime,
      LocalDateTime startAt,
      LocalDateTime endAt,
      Long stageId) {
    Schedule schedule = new Schedule(startAt, endAt);
    Product product = Product.create(name, productType, runningTime, schedule, stageId);
    Product saved = productRepository.save(product);
    return saved.getId();
  }

  /**
   * 상품 정보를 수정한다.
   *
   * @param productId 수정할 상품 ID
   * @param name 상품명
   * @param productType 상품 타입
   * @param runningTime 상영 시간 (분)
   * @param startAt 시작 일시
   * @param endAt 종료 일시
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   */
  public void updateProduct(
      Long productId,
      String name,
      ProductType productType,
      Integer runningTime,
      LocalDateTime startAt,
      LocalDateTime endAt) {
    Schedule schedule = new Schedule(startAt, endAt);
    Product product = findProductById(productId);
    product.update(name, productType, runningTime, schedule);
  }

  /**
   * 상품의 스테이지를 변경한다.
   *
   * <p>예매 시작 전에만 스테이지를 변경할 수 있다.
   *
   * @param productId 상품 ID
   * @param stageId 변경할 스테이지 ID
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   * @throws ProductException 스테이지 변경이 불가능한 경우 ({@link ProductErrorCode#STAGE_CHANGE_NOT_ALLOWED})
   */
  public void changeStage(Long productId, Long stageId) {
    Product product = findProductById(productId);
    product.changeStage(stageId);
  }

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
}
