package com.tickatch.product_service.product.domain.repository;

import com.tickatch.product_service.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상품 JPA 리포지토리.
 *
 * <p>Spring Data JPA 기본 CRUD 기능을 제공한다. {@link ProductRepositoryImpl}에서 내부적으로 사용된다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductRepositoryImpl
 */
public interface ProductJpaRepository extends JpaRepository<Product, Long> {}
