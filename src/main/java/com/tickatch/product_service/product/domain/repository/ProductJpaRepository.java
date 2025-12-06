package com.tickatch.product_service.product.domain.repository;

import com.tickatch.product_service.product.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 상품 JPA 리포지토리.
 *
 * <p>Spring Data JPA 기본 CRUD 기능을 제공한다. {@link ProductRepositoryImpl}에서 내부적으로 사용된다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductRepositoryImpl
 */
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

  /**
   * 비관적 쓰기 락을 걸고 상품을 조회한다.
   *
   * <p>좌석 차감 등 동시성 제어가 필요한 작업에서 사용한다. 트랜잭션 종료 시까지 다른 트랜잭션의 쓰기 락 요청이 대기한다.
   *
   * @param id 상품 ID
   * @return 조회된 상품 (없으면 empty)
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Product p WHERE p.id = :id")
  Optional<Product> findByIdForUpdate(@Param("id") Long id);
}