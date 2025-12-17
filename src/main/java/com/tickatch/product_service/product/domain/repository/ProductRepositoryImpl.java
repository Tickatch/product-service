package com.tickatch.product_service.product.domain.repository;

import static com.tickatch.product_service.product.domain.QProduct.product;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.ProductRepository;
import com.tickatch.product_service.product.domain.repository.dto.ProductSearchCondition;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 상품 리포지토리 구현체.
 *
 * <p>JPA와 QueryDSL을 사용하여 상품 데이터를 조회/저장한다. 동적 쿼리를 통해 다양한 검색 조건을 지원한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see ProductRepository
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

  private final ProductJpaRepository productJpaRepository;
  private final JPAQueryFactory queryFactory;

  /** {@inheritDoc} */
  @Override
  public Product save(Product product) {
    return productJpaRepository.save(product);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Product> findById(Long id) {
    return productJpaRepository.findById(id);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Product> findByIdForUpdate(Long id) {
    return productJpaRepository.findByIdForUpdate(id);
  }

  /**
   * {@inheritDoc}
   *
   * <p>QueryDSL을 사용하여 동적 검색 조건을 적용한다. 삭제된 상품(deletedAt != null)은 조회되지 않는다.
   */
  @Override
  public Page<Product> findAllByCondition(ProductSearchCondition condition, Pageable pageable) {

    List<Product> content =
        queryFactory
            .selectFrom(product)
            .where(
                notDeleted(),
                nameContains(condition.getName()),
                productTypeEq(condition.getProductType()),
                statusEq(condition.getStatus()),
                stageIdEq(condition.getStageId()),
                sellerIdEq(condition.getSellerId()))
            .orderBy(getOrderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    JPAQuery<Long> countQuery =
        queryFactory
            .select(product.count())
            .from(product)
            .where(
                notDeleted(),
                nameContains(condition.getName()),
                productTypeEq(condition.getProductType()),
                statusEq(condition.getStatus()),
                stageIdEq(condition.getStageId()),
                sellerIdEq(condition.getSellerId()));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }

  /**
   * 삭제되지 않은 상품만 조회하는 조건.
   *
   * @return soft delete 되지 않은 상품 조건
   */
  private BooleanExpression notDeleted() {
    return product.deletedAt.isNull();
  }

  /**
   * 상품명 부분 일치 검색 조건.
   *
   * @param name 검색할 상품명
   * @return 상품명 포함 조건 (null이면 조건 미적용)
   */
  private BooleanExpression nameContains(String name) {
    return StringUtils.hasText(name) ? product.name.contains(name) : null;
  }

  /**
   * 상품 타입 일치 검색 조건.
   *
   * @param productType 검색할 상품 타입
   * @return 상품 타입 일치 조건 (null이면 조건 미적용)
   */
  private BooleanExpression productTypeEq(ProductType productType) {
    return productType != null ? product.productType.eq(productType) : null;
  }

  /**
   * 상품 상태 일치 검색 조건.
   *
   * @param status 검색할 상품 상태
   * @return 상품 상태 일치 조건 (null이면 조건 미적용)
   */
  private BooleanExpression statusEq(ProductStatus status) {
    return status != null ? product.status.eq(status) : null;
  }

  /**
   * 스테이지 ID 일치 검색 조건.
   *
   * @param stageId 검색할 스테이지 ID
   * @return 스테이지 ID 일치 조건 (null이면 조건 미적용)
   */
  private BooleanExpression stageIdEq(Long stageId) {
    return stageId != null ? product.venue.stageId.eq(stageId) : null;
  }

  /**
   * 판매자 ID 일치 검색 조건.
   *
   * @param sellerId 검색할 판매자 ID
   * @return 판매자 ID 일치 조건 (null이면 조건 미적용)
   */
  private BooleanExpression sellerIdEq(String sellerId) {
    return StringUtils.hasText(sellerId) ? product.sellerId.eq(sellerId) : null;
  }

  /**
   * 정렬 조건을 OrderSpecifier 배열로 변환한다.
   *
   * @param sort Spring Data Sort 객체
   * @return QueryDSL OrderSpecifier 배열
   */
  private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    sort.forEach(
        order -> {
          Order direction = order.isAscending() ? Order.ASC : Order.DESC;
          String property = order.getProperty();

          OrderSpecifier<?> orderSpecifier =
              switch (property) {
                case "name" -> new OrderSpecifier<>(direction, product.name);
                case "createdAt" -> new OrderSpecifier<>(direction, product.createdAt);
                case "updatedAt" -> new OrderSpecifier<>(direction, product.updatedAt);
                case "status" -> new OrderSpecifier<>(direction, product.status);
                case "productType" -> new OrderSpecifier<>(direction, product.productType);
                case "viewCount" -> new OrderSpecifier<>(direction, product.stats.viewCount);
                default -> new OrderSpecifier<>(direction, product.createdAt);
              };
          orderSpecifiers.add(orderSpecifier);
        });

    if (orderSpecifiers.isEmpty()) {
      orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, product.createdAt));
    }

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }

  /** {@inheritDoc} */
  @Override
  public List<Product> findByStatusAndSaleStartAtBefore(ProductStatus status, LocalDateTime time) {
    return productJpaRepository.findByStatusAndSaleScheduleSaleStartAtBefore(status, time);
  }

  /** {@inheritDoc} */
  @Override
  public List<Product> findByStatusAndSaleEndAtBefore(ProductStatus status, LocalDateTime time) {
    return productJpaRepository.findByStatusAndSaleScheduleSaleEndAtBefore(status, time);
  }

  /** {@inheritDoc} */
  @Override
  public List<Product> findByStatusAndEndAtBefore(ProductStatus status, LocalDateTime time) {
    return productJpaRepository.findByStatusAndScheduleEndAtBefore(status, time);
  }

  @Override
  public void flush() {
    productJpaRepository.flush();
  }
}
