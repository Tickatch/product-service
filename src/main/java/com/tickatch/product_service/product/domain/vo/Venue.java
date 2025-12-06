package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Venue {

  @Column(name = "stage_id", nullable = false)
  private Long stageId;

  @Column(name = "stage_name", nullable = false)
  private String stageName;

  @Column(name = "art_hall_id", nullable = false)
  private Long artHallId;

  @Column(name = "art_hall_name", nullable = false)
  private String artHallName;

  @Column(name = "art_hall_address", nullable = false)
  private String artHallAddress;

  public Venue(
      Long stageId, String stageName, Long artHallId, String artHallName, String artHallAddress) {
    validate(stageId, stageName, artHallId, artHallName, artHallAddress);
    this.stageId = stageId;
    this.stageName = stageName;
    this.artHallId = artHallId;
    this.artHallName = artHallName;
    this.artHallAddress = artHallAddress;
  }

  private static void validate(
      Long stageId, String stageName, Long artHallId, String artHallName, String artHallAddress) {
    requireNonNull(stageId);
    requireNonNull(artHallId);
    requireNonBlank(stageName);
    requireNonBlank(artHallName);
    requireNonBlank(artHallAddress);
  }

  private static void requireNonNull(Object value) {
    if (Objects.isNull(value)) {
      throw new ProductException(ProductErrorCode.INVALID_VENUE);
    }
  }

  private static void requireNonBlank(String value) {
    if (Objects.isNull(value) || value.isBlank()) {
      throw new ProductException(ProductErrorCode.INVALID_VENUE);
    }
  }
}
