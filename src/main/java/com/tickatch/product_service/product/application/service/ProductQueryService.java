package com.tickatch.product_service.product.application.service;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import com.tickatch.product_service.product.domain.repository.dto.ProductResponse;
import com.tickatch.product_service.product.domain.repository.dto.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 조회 서비스.
 *
 * <p>상품 조회와 관련된 비즈니스 로직을 처리한다. 읽기 전용 트랜잭션으로 동작하여 성능을 최적화한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductCommandService
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductQueryService {

  private final ProductRepository productRepository;

  /**
   * 상품 단건을 조회한다.
   *
   * @param productId 조회할 상품 ID
   * @return 상품 응답 DTO
   * @throws ProductException 상품을 찾을 수 없는 경우 ({@link ProductErrorCode#PRODUCT_NOT_FOUND})
   */
  public ProductResponse getProduct(Long productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, productId));
    return ProductResponse.from(product);
  }

  /**
   * 검색 조건에 맞는 상품 목록을 페이징하여 조회한다.
   *
   * @param condition 검색 조건 (상품명, 타입, 상태, 스테이지 ID)
   * @param pageable 페이징 정보
   * @return 페이징된 상품 응답 목록
   */
  public Page<ProductResponse> getProducts(ProductSearchCondition condition, Pageable pageable) {
    Page<Product> products = productRepository.findAllByCondition(condition, pageable);
    return products.map(ProductResponse::from);
  }
}
