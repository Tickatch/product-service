package com.tickatch.product_service.product.infrastructure.scheduler;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 상태 자동 전이 스케줄러.
 *
 * <p>시간 조건에 따라 상품 상태를 자동으로 전이한다:
 *
 * <ul>
 *   <li>SCHEDULED → ON_SALE: 판매 시작 시간 도래 (매 분)
 *   <li>ON_SALE → CLOSED: 판매 종료 시간 도래 (매 분)
 *   <li>CLOSED → COMPLETED: 행사 종료 시간 도래 (매 시간)
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ProductStatusScheduler {

  private final ProductRepository productRepository;

  /**
   * SCHEDULED → ON_SALE 상태 전이.
   *
   * <p>판매 시작 시간이 도래한 SCHEDULED 상태 상품을 ON_SALE로 변경한다.
   * 매 분 실행된다.
   */
  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void transitionToOnSale() {
    LocalDateTime now = LocalDateTime.now();
    List<Product> products = productRepository.findByStatusAndSaleStartAtBefore(
        ProductStatus.SCHEDULED, now);

    for (Product product : products) {
      product.changeStatus(ProductStatus.ON_SALE);
    }
  }

  /**
   * ON_SALE → CLOSED 상태 전이.
   *
   * <p>판매 종료 시간이 도래한 ON_SALE 상태 상품을 CLOSED로 변경한다.
   * 매 분 실행된다.
   */
  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void transitionToClosed() {
    LocalDateTime now = LocalDateTime.now();
    List<Product> products = productRepository.findByStatusAndSaleEndAtBefore(
        ProductStatus.ON_SALE, now);

    for (Product product : products) {
      product.changeStatus(ProductStatus.CLOSED);
    }
  }

  /**
   * CLOSED → COMPLETED 상태 전이.
   *
   * <p>행사 종료 시간이 도래한 CLOSED 상태 상품을 COMPLETED로 변경한다.
   * 매 시간 실행된다.
   */
  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void transitionToCompleted() {
    LocalDateTime now = LocalDateTime.now();
    List<Product> products = productRepository.findByStatusAndEndAtBefore(
        ProductStatus.CLOSED, now);

    for (Product product : products) {
      product.changeStatus(ProductStatus.COMPLETED);
    }
  }
}