package com.tickatch.product_service.product.domain;

import com.tickatch.product_service.product.domain.repository.dto.ProductSearchCondition;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

  Product save(Product product);

  Optional<Product> findById(Long id);

  Page<Product> findAllByCondition(ProductSearchCondition condition, Pageable pageable);
}
