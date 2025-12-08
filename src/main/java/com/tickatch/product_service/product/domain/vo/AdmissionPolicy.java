package com.tickatch.product_service.product.domain.vo;

import com.tickatch.product_service.product.domain.exception.ProductErrorCode;
import com.tickatch.product_service.product.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 입장 정책 Value Object.
 *
 * <p>공연장 입장 및 관람 관련 정책을 관리한다. 불변 객체로 설계되어 값의 일관성을 보장한다.
 *
 * <p>필드 제약:
 *
 * <ul>
 *   <li>admissionMinutesBefore: 기본값 30분
 *   <li>lateEntryAllowed: 기본값 false
 *   <li>lateEntryNotice: 최대 200자
 *   <li>hasIntermission: 기본값 false
 *   <li>intermissionMinutes: hasIntermission이 true일 때만 의미 있음
 *   <li>photographyAllowed: 기본값 false
 *   <li>foodAllowed: 기본값 false
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class AdmissionPolicy {

  private static final int DEFAULT_ADMISSION_MINUTES = 30;
  private static final int LATE_ENTRY_NOTICE_MAX_LENGTH = 200;

  /** 입장 시작 시간 (공연 n분 전) */
  @Column(name = "admission_minutes_before")
  private Integer admissionMinutesBefore;

  /** 지각 입장 가능 여부 */
  @Column(name = "late_entry_allowed")
  private Boolean lateEntryAllowed;

  /** 지각 입장 안내 */
  @Column(name = "late_entry_notice", length = LATE_ENTRY_NOTICE_MAX_LENGTH)
  private String lateEntryNotice;

  /** 인터미션 유무 */
  @Column(name = "has_intermission")
  private Boolean hasIntermission;

  /** 인터미션 시간 (분) */
  @Column(name = "intermission_minutes")
  private Integer intermissionMinutes;

  /** 촬영 가능 여부 */
  @Column(name = "photography_allowed")
  private Boolean photographyAllowed;

  /** 음식물 반입 가능 여부 */
  @Column(name = "food_allowed")
  private Boolean foodAllowed;

  /**
   * 입장 정책을 생성한다.
   *
   * @param admissionMinutesBefore 입장 시작 시간 (null이면 30분)
   * @param lateEntryAllowed 지각 입장 가능 여부 (null이면 false)
   * @param lateEntryNotice 지각 입장 안내
   * @param hasIntermission 인터미션 유무 (null이면 false)
   * @param intermissionMinutes 인터미션 시간 (분)
   * @param photographyAllowed 촬영 가능 여부 (null이면 false)
   * @param foodAllowed 음식물 반입 가능 여부 (null이면 false)
   * @throws ProductException 유효성 검증 실패 시 ({@link ProductErrorCode#INVALID_ADMISSION_POLICY})
   */
  public AdmissionPolicy(
      Integer admissionMinutesBefore,
      Boolean lateEntryAllowed,
      String lateEntryNotice,
      Boolean hasIntermission,
      Integer intermissionMinutes,
      Boolean photographyAllowed,
      Boolean foodAllowed) {
    validate(admissionMinutesBefore, lateEntryNotice, hasIntermission, intermissionMinutes);

    this.admissionMinutesBefore =
        admissionMinutesBefore != null ? admissionMinutesBefore : DEFAULT_ADMISSION_MINUTES;
    this.lateEntryAllowed = lateEntryAllowed != null ? lateEntryAllowed : false;
    this.lateEntryNotice = lateEntryNotice;
    this.hasIntermission = hasIntermission != null ? hasIntermission : false;
    this.intermissionMinutes = intermissionMinutes;
    this.photographyAllowed = photographyAllowed != null ? photographyAllowed : false;
    this.foodAllowed = foodAllowed != null ? foodAllowed : false;
  }

  /**
   * 기본 입장 정책을 생성한다.
   *
   * <p>30분 전 입장, 지각 입장 불가, 촬영/음식물 불가로 설정된다.
   *
   * @return 기본 AdmissionPolicy
   */
  public static AdmissionPolicy defaultPolicy() {
    return new AdmissionPolicy(
        DEFAULT_ADMISSION_MINUTES, false, null, false, null, false, false);
  }

  /**
   * 지각 입장이 가능한지 확인한다.
   *
   * @return 지각 입장 가능하면 true
   */
  public boolean allowsLateEntry() {
    return lateEntryAllowed;
  }

  /**
   * 인터미션이 있는지 확인한다.
   *
   * @return 인터미션이 있으면 true
   */
  public boolean hasIntermission() {
    return hasIntermission;
  }

  /**
   * 촬영이 가능한지 확인한다.
   *
   * @return 촬영 가능하면 true
   */
  public boolean allowsPhotography() {
    return photographyAllowed;
  }

  /**
   * 음식물 반입이 가능한지 확인한다.
   *
   * @return 음식물 반입 가능하면 true
   */
  public boolean allowsFood() {
    return foodAllowed;
  }

  private static void validate(
      Integer admissionMinutesBefore,
      String lateEntryNotice,
      Boolean hasIntermission,
      Integer intermissionMinutes) {

    if (admissionMinutesBefore != null && admissionMinutesBefore < 0) {
      throw new ProductException(ProductErrorCode.INVALID_ADMISSION_POLICY);
    }

    if (lateEntryNotice != null && lateEntryNotice.length() > LATE_ENTRY_NOTICE_MAX_LENGTH) {
      throw new ProductException(ProductErrorCode.INVALID_ADMISSION_POLICY);
    }

    if (Boolean.TRUE.equals(hasIntermission)
        && (intermissionMinutes == null || intermissionMinutes <= 0)) {
      throw new ProductException(ProductErrorCode.INVALID_ADMISSION_POLICY);
    }
  }
}