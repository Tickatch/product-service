package com.tickatch.product_service.product.infrastructure.scheduler;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 상태 전이 헬퍼.
 *
 * <p>스케줄러에서 호출되며, 개별 상품의 상태 전이를 독립된 트랜잭션으로 처리한다. 한 상품의 실패가 다른 상품에 영향을 주지 않도록 트랜잭션을 분리한다.
 *
 * <p>각 상품은 새 트랜잭션에서 다시 조회하여 영속성 컨텍스트에 포함시킨 후 상태를 변경한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStatusTransitionHelper {

  private final ProductRepository productRepository;

  /**
   * 개별 상품의 상태를 변경한다.
   *
   * <p>REQUIRES_NEW propagation을 사용하여 각 상품별로 독립된 트랜잭션을 생성한다. 새 트랜잭션에서 상품을 다시 조회하여 영속 상태로 만든 후 상태를
   * 변경한다. 실패 시 해당 상품만 롤백되고 다른 상품 처리는 계속된다.
   *
   * @param productId 상태를 변경할 상품 ID
   * @param targetStatus 변경할 상태
   * @return 성공 여부
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean changeStatusSafely(Long productId, ProductStatus targetStatus) {
    try {
      Product product =
          productRepository
              .findById(productId)
              .orElseThrow(
                  () -> new IllegalStateException("상품을 찾을 수 없습니다. productId: " + productId));

      ProductStatus previousStatus = product.getStatus();
      product.changeStatus(targetStatus);

      log.info(
          "상품 상태 전이 성공. productId: {}, status: {} → {}", productId, previousStatus, targetStatus);
      return true;
    } catch (Exception e) {
      log.error(
          "상품 상태 전이 실패. productId: {}, targetStatus: {}, error: {}",
          productId,
          targetStatus,
          e.getMessage());
      return false;
    }
  }
}
