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

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

  private final ProductJpaRepository productJpaRepository;
  private final JPAQueryFactory queryFactory;

  @Override
  public Product save(Product product) {
    return productJpaRepository.save(product);
  }

  @Override
  public Optional<Product> findById(Long id) {
    return productJpaRepository.findById(id);
  }

  // QueryDsl 메서드 구현
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
                stageIdEq(condition.getStageId()))
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
                stageIdEq(condition.getStageId()));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }

  private BooleanExpression notDeleted() {
    return product.deletedAt.isNull();
  }

  private BooleanExpression nameContains(String name) {
    return StringUtils.hasText(name) ? product.name.contains(name) : null;
  }

  private BooleanExpression productTypeEq(ProductType productType) {
    return productType != null ? product.productType.eq(productType) : null;
  }

  private BooleanExpression statusEq(ProductStatus status) {
    return status != null ? product.status.eq(status) : null;
  }

  private BooleanExpression stageIdEq(Long stageId) {
    return stageId != null ? product.stageId.eq(stageId) : null;
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    sort.forEach(
        order -> {
          Order direction = order.isAscending() ? Order.ASC : Order.DESC;
          String property = order.getProperty();

          OrderSpecifier<?> orderSpecifier =
              switch (property) {
                case "name" -> new OrderSpecifier<>(direction, product.name);
                case "createAt" -> new OrderSpecifier<>(direction, product.createdAt);
                case "updateAt" -> new OrderSpecifier<>(direction, product.updatedAt);
                case "status" -> new OrderSpecifier<>(direction, product.status);
                case "productType" -> new OrderSpecifier<>(direction, product.productType);
                default -> new OrderSpecifier<>(direction, product.createdAt);
              };
          orderSpecifiers.add(orderSpecifier);
        });

    if (orderSpecifiers.isEmpty()) {
      orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, product.createdAt));
    }

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }
}
