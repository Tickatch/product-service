package com.tickatch.product_service.product.infrastructure.scheduler;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
 * <p>각 상품의 상태 전이는 독립된 트랜잭션으로 처리되어, 한 상품의 실패가 다른 상품에 영향을 주지 않는다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStatusScheduler {

  private final ProductRepository productRepository;
  private final ProductStatusTransitionHelper transitionHelper;

  /**
   * SCHEDULED → ON_SALE 상태 전이.
   *
   * <p>판매 시작 시간이 도래한 SCHEDULED 상태 상품을 ON_SALE로 변경한다. 매 분 실행된다.
   */
  @Scheduled(cron = "0 * * * * *")
  public void transitionToOnSale() {
    LocalDateTime now = LocalDateTime.now();
    List<Product> products =
        productRepository.findByStatusAndSaleStartAtBefore(ProductStatus.SCHEDULED, now);

    if (products.isEmpty()) {
      return;
    }

    log.info("SCHEDULED → ON_SALE 전이 시작. 대상 상품 수: {}", products.size());

    int successCount = 0;
    int failCount = 0;

    for (Product product : products) {
      boolean success = transitionHelper.changeStatusSafely(product.getId(), ProductStatus.ON_SALE);
      if (success) {
        successCount++;
      } else {
        failCount++;
      }
    }

    log.info("SCHEDULED → ON_SALE 전이 완료. 성공: {}, 실패: {}", successCount, failCount);
  }

  /**
   * ON_SALE → CLOSED 상태 전이.
   *
   * <p>판매 종료 시간이 도래한 ON_SALE 상태 상품을 CLOSED로 변경한다. 매 분 실행된다.
   */
  @Scheduled(cron = "0 * * * * *")
  public void transitionToClosed() {
    LocalDateTime now = LocalDateTime.now();
    List<Product> products =
        productRepository.findByStatusAndSaleEndAtBefore(ProductStatus.ON_SALE, now);

    if (products.isEmpty()) {
      return;
    }

    log.info("ON_SALE → CLOSED 전이 시작. 대상 상품 수: {}", products.size());

    int successCount = 0;
    int failCount = 0;

    for (Product product : products) {
      boolean success = transitionHelper.changeStatusSafely(product.getId(), ProductStatus.CLOSED);
      if (success) {
        successCount++;
      } else {
        failCount++;
      }
    }

    log.info("ON_SALE → CLOSED 전이 완료. 성공: {}, 실패: {}", successCount, failCount);
  }

  /**
   * CLOSED → COMPLETED 상태 전이.
   *
   * <p>행사 종료 시간이 도래한 CLOSED 상태 상품을 COMPLETED로 변경한다. 매 시간 실행된다.
   */
  @Scheduled(cron = "0 0 * * * *")
  public void transitionToCompleted() {
    LocalDateTime now = LocalDateTime.now();
    List<Product> products =
        productRepository.findByStatusAndEndAtBefore(ProductStatus.CLOSED, now);

    if (products.isEmpty()) {
      return;
    }

    log.info("CLOSED → COMPLETED 전이 시작. 대상 상품 수: {}", products.size());

    int successCount = 0;
    int failCount = 0;

    for (Product product : products) {
      boolean success =
          transitionHelper.changeStatusSafely(product.getId(), ProductStatus.COMPLETED);
      if (success) {
        successCount++;
      } else {
        failCount++;
      }
    }

    log.info("CLOSED → COMPLETED 전이 완료. 성공: {}, 실패: {}", successCount, failCount);
  }
}
