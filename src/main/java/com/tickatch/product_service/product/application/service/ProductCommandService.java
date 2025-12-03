package com.tickatch.product_service.product.application.service;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.vo.Schedule;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class ProductCommandService {

  private final ProductRepository productRepository;

  public Long createProduct(
      String name,
      ProductType productType,
      Integer runningTime,
      LocalDateTime startAt,
      LocalDateTime endAt,
      Long stageId
  ) {
    Schedule schedule = new Schedule(startAt, endAt);
    Product product = Product.create(name, productType, runningTime, schedule, stageId);
    Product saved = productRepository.save(product);
    return saved.getId();
  }

  public void updateProduct(
      Long productId,
      String name,
      ProductType productType,
      Integer runningTime,
      LocalDateTime startAt,
      LocalDateTime endAt
  ) {
    Schedule schedule = new Schedule(startAt, endAt);
    Product product = findProductById(productId);
    product.update(name, productType, runningTime, schedule);
  }

  public void changeStage(Long productId, Long stageId) {
    Product product = findProductById(productId);
    product.changeStage(stageId);
  }

  public void changeStatus(Long productId, ProductStatus newStatus) {
    Product product = findProductById(productId);
    product.changeStatus(newStatus);
  }

  public void cancelProduct(Long productId, String cancellBy) {
    Product product = findProductById(productId);
    product.cancel(cancellBy);
  }

  private Product findProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, productId));
  }
}
