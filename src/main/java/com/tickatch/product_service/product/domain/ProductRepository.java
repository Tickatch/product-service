package com.tickatch.product_service.product.domain;

import com.tickatch.product_service.product.domain.repository.dto.ProductSearchCondition;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 상품 리포지토리 인터페이스.
 *
 * <p>도메인 레이어에서 정의하고, 인프라스트럭처 레이어에서 구현한다. DIP(의존성 역전 원칙)를 적용하여 도메인 로직과 영속성 기술을 분리한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see com.tickatch.product_service.product.domain.repository.ProductRepositoryImpl
 */
public interface ProductRepository {

  /**
   * 상품을 저장한다.
   *
   * @param product 저장할 상품 엔티티
   * @return 저장된 상품 엔티티
   */
  Product save(Product product);

  /**
   * ID로 상품을 조회한다.
   *
   * @param id 상품 ID
   * @return 조회된 상품 (없으면 empty)
   */
  Optional<Product> findById(Long id);

  /**
   * 검색 조건에 맞는 상품 목록을 페이징하여 조회한다.
   *
   * <p>삭제된 상품(deletedAt != null)은 조회되지 않는다.
   *
   * @param condition 검색 조건
   * @param pageable 페이징 정보
   * @return 페이징된 상품 목록
   */
  Page<Product> findAllByCondition(ProductSearchCondition condition, Pageable pageable);
}
