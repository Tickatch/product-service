package com.tickatch.product_service.product.domain.repository;

import com.tickatch.product_service.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {}
