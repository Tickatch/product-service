package com.tickatch.product_service.product.application.dto;

import com.tickatch.product_service.product.domain.vo.AgeRating;
import com.tickatch.product_service.product.domain.vo.ProductType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 수정 명령 객체.
 *
 * <p>상품 수정에 필요한 모든 정보를 담는다. null인 필드는 수정하지 않는다.
 * 단, 값객체는 세트 단위로 검증되어 일부 필드만 있으면 검증 실패.
 *
 * <p>DRAFT, REJECTED 상태에서만 수정 가능하다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public class ProductUpdateCommand {

  // ========== 필수 ==========

  /** 수정할 상품 ID */
  private final Long productId;

  /** 요청자 판매자 ID (소유권 검증용) */
  private final String sellerId;

  // ========== 기본 정보 (선택) ==========

  /** 상품명 */
  private final String name;

  /** 상품 타입 */
  private final ProductType productType;

  /** 상영 시간 (분) */
  private final Integer runningTime;

  // ========== 일정 (선택, 세트) ==========

  /** 행사 시작 일시 */
  private final LocalDateTime startAt;

  /** 행사 종료 일시 */
  private final LocalDateTime endAt;

  /** 예매 시작 일시 */
  private final LocalDateTime saleStartAt;

  /** 예매 종료 일시 */
  private final LocalDateTime saleEndAt;

  // ========== 장소 (선택, 세트) ==========

  /** 스테이지 ID */
  private final Long stageId;

  /** 스테이지명 */
  private final String stageName;

  /** 아트홀 ID */
  private final Long artHallId;

  /** 아트홀명 */
  private final String artHallName;

  /** 아트홀 주소 */
  private final String artHallAddress;

  // ========== 콘텐츠 (선택, 세트) ==========

  /** 상품 설명 */
  private final String description;

  /** 포스터 이미지 URL */
  private final String posterImageUrl;

  /** 상세 이미지 URL 배열 (JSON) */
  private final String detailImageUrls;

  /** 출연진/아티스트 정보 */
  private final String castInfo;

  /** 유의사항 */
  private final String notice;

  /** 주최사 */
  private final String organizer;

  /** 주관사/기획사 */
  private final String agency;

  // ========== 관람 제한 (선택, 세트) ==========

  /** 관람 등급 */
  private final AgeRating ageRating;

  /** 추가 제한사항 안내 */
  private final String restrictionNotice;

  // ========== 예매 정책 (선택, 세트) ==========

  /** 1인당 최대 예매 매수 */
  private final Integer maxTicketsPerPerson;

  /** 본인확인 필요 여부 */
  private final Boolean idVerificationRequired;

  /** 양도 가능 여부 */
  private final Boolean transferable;

  // ========== 입장 정책 (선택, 세트) ==========

  /** 입장 시작 시간 (공연 n분 전) */
  private final Integer admissionMinutesBefore;

  /** 지각 입장 가능 여부 */
  private final Boolean lateEntryAllowed;

  /** 지각 입장 안내 */
  private final String lateEntryNotice;

  /** 인터미션 유무 */
  private final Boolean hasIntermission;

  /** 인터미션 시간 (분) */
  private final Integer intermissionMinutes;

  /** 촬영 가능 여부 */
  private final Boolean photographyAllowed;

  /** 음식물 반입 가능 여부 */
  private final Boolean foodAllowed;

  // ========== 환불 정책 (선택, 세트) ==========

  /** 취소 가능 여부 */
  private final Boolean cancellable;

  /** 취소 마감일 (공연 n일 전) */
  private final Integer cancelDeadlineDays;

  /** 환불 정책 상세 안내 */
  private final String refundPolicyText;

  // ========== 좌석 등급 정보 (선택) ==========

  /** 좌석 등급 정보 리스트 (배열 순서대로 displayOrder) */
  private final List<SeatGradeInfo> seatGradeInfos;

  // ========== 개별 좌석 정보 (선택) ==========

  /** 개별 좌석 정보 리스트 (ReservationSeat 서비스 전달용) */
  private final List<SeatCreateInfo> seatCreateInfos;

  // ========== 검증 메서드 ==========

  /**
   * 필수 필드를 검증한다.
   *
   * @throws IllegalArgumentException 필수 필드가 없는 경우
   */
  public void validateRequired() {
    if (productId == null) {
      throw new IllegalArgumentException("productId는 필수입니다.");
    }
    if (sellerId == null || sellerId.isBlank()) {
      throw new IllegalArgumentException("sellerId는 필수입니다.");
    }
  }

  /**
   * 값객체 세트 검증을 수행한다.
   *
   * <p>값객체를 수정하려면 해당 세트의 모든 필수 필드가 있어야 한다.
   *
   * @throws IllegalArgumentException 값객체 필드가 일부만 있는 경우
   */
  public void validateValueObjectSets() {
    // 일정: 하나라도 있으면 4개 모두 필요
    if (hasSchedule()) {
      if (startAt == null || endAt == null || saleStartAt == null || saleEndAt == null) {
        throw new IllegalArgumentException("Schedule 수정 시 startAt, endAt, saleStartAt, saleEndAt 모두 필요합니다.");
      }
    }

    // 장소: 하나라도 있으면 5개 모두 필요
    if (hasVenue()) {
      if (stageId == null || stageName == null || artHallId == null
          || artHallName == null || artHallAddress == null) {
        throw new IllegalArgumentException("Venue 수정 시 stageId, stageName, artHallId, artHallName, artHallAddress 모두 필요합니다.");
      }
    }

    // 관람 제한: ageRating 필수, restrictionNotice 선택
    if (hasAgeRestriction() && ageRating == null) {
      throw new IllegalArgumentException("AgeRestriction 수정 시 ageRating은 필수입니다.");
    }

    // 예매 정책: maxTicketsPerPerson 필수
    if (hasBookingPolicy() && maxTicketsPerPerson == null) {
      throw new IllegalArgumentException("BookingPolicy 수정 시 maxTicketsPerPerson은 필수입니다.");
    }

    // 입장 정책: admissionMinutesBefore 필수
    if (hasAdmissionPolicy() && admissionMinutesBefore == null) {
      throw new IllegalArgumentException("AdmissionPolicy 수정 시 admissionMinutesBefore는 필수입니다.");
    }

    // 환불 정책: cancellable 필수
    if (hasRefundPolicy() && cancellable == null) {
      throw new IllegalArgumentException("RefundPolicy 수정 시 cancellable은 필수입니다.");
    }

    // 좌석 정보: seatGradeInfos와 seatCreateInfos는 함께 있어야 함
    if (hasSeatGradeInfos() != hasSeatCreateInfos()) {
      throw new IllegalArgumentException("좌석 수정 시 seatGradeInfos와 seatCreateInfos 모두 필요합니다.");
    }

    // 좌석 등급별 검증
    if (seatGradeInfos != null) {
      for (SeatGradeInfo info : seatGradeInfos) {
        info.validate();
      }
    }

    // 개별 좌석 검증
    if (seatCreateInfos != null) {
      for (SeatCreateInfo info : seatCreateInfos) {
        info.validate();
      }
    }
  }

  /**
   * 좌석 정보 정합성을 검증한다.
   *
   * <p>SeatGradeInfo의 totalSeats 합과 SeatCreateInfo 개수가 일치해야 한다.
   *
   * @throws IllegalArgumentException 정합성이 맞지 않는 경우
   */
  public void validateSeatConsistency() {
    if (seatGradeInfos == null || seatCreateInfos == null) {
      return;
    }

    // totalSeats 합 vs seatCreateInfos 개수
    int totalSeatsSum = seatGradeInfos.stream()
        .mapToInt(SeatGradeInfo::totalSeats)
        .sum();
    if (totalSeatsSum != seatCreateInfos.size()) {
      throw new IllegalArgumentException(
          String.format("좌석 수 불일치: SeatGradeInfo 총합=%d, SeatCreateInfo 개수=%d",
              totalSeatsSum, seatCreateInfos.size()));
    }

    // grade 존재 여부 검증
    List<String> validGrades = seatGradeInfos.stream()
        .map(SeatGradeInfo::gradeName)
        .toList();
    for (SeatCreateInfo info : seatCreateInfos) {
      if (!validGrades.contains(info.grade())) {
        throw new IllegalArgumentException(
            String.format("유효하지 않은 등급: %s (유효한 등급: %s)", info.grade(), validGrades));
      }
    }

    // grade별 가격 일치 검증
    for (SeatGradeInfo gradeInfo : seatGradeInfos) {
      List<SeatCreateInfo> seatsOfGrade = seatCreateInfos.stream()
          .filter(s -> s.grade().equals(gradeInfo.gradeName()))
          .toList();

      for (SeatCreateInfo seat : seatsOfGrade) {
        if (!seat.price().equals(gradeInfo.price())) {
          throw new IllegalArgumentException(
              String.format("가격 불일치: 등급 %s의 가격은 %d이어야 하지만 좌석 %s의 가격은 %d입니다.",
                  gradeInfo.gradeName(), gradeInfo.price(), seat.seatNumber(), seat.price()));
        }
      }

      // grade별 좌석 수 일치 검증
      if (seatsOfGrade.size() != gradeInfo.totalSeats()) {
        throw new IllegalArgumentException(
            String.format("좌석 수 불일치: 등급 %s의 totalSeats=%d이지만 실제 좌석 수=%d",
                gradeInfo.gradeName(), gradeInfo.totalSeats(), seatsOfGrade.size()));
      }
    }
  }

  // ========== 존재 여부 확인 ==========

  public boolean hasBasicInfo() {
    return name != null || productType != null || runningTime != null;
  }

  public boolean hasSchedule() {
    return startAt != null || endAt != null || saleStartAt != null || saleEndAt != null;
  }

  public boolean hasVenue() {
    return stageId != null || stageName != null || artHallId != null
        || artHallName != null || artHallAddress != null;
  }

  public boolean hasContent() {
    return description != null || posterImageUrl != null || detailImageUrls != null
        || castInfo != null || notice != null || organizer != null || agency != null;
  }

  public boolean hasAgeRestriction() {
    return ageRating != null || restrictionNotice != null;
  }

  public boolean hasBookingPolicy() {
    return maxTicketsPerPerson != null || idVerificationRequired != null || transferable != null;
  }

  public boolean hasAdmissionPolicy() {
    return admissionMinutesBefore != null || lateEntryAllowed != null || lateEntryNotice != null
        || hasIntermission != null || intermissionMinutes != null || photographyAllowed != null
        || foodAllowed != null;
  }

  public boolean hasRefundPolicy() {
    return cancellable != null || cancelDeadlineDays != null || refundPolicyText != null;
  }

  public boolean hasSeatGradeInfos() {
    return seatGradeInfos != null && !seatGradeInfos.isEmpty();
  }

  public boolean hasSeatCreateInfos() {
    return seatCreateInfos != null && !seatCreateInfos.isEmpty();
  }

  // ========== 중첩 Record (CreateCommand와 동일) ==========

  /**
   * 좌석 등급 정보.
   */
  public record SeatGradeInfo(
      String gradeName,
      Long price,
      Integer totalSeats
  ) {
    public void validate() {
      if (gradeName == null || gradeName.isBlank()) {
        throw new IllegalArgumentException("SeatGradeInfo.gradeName은 필수입니다.");
      }
      if (price == null || price < 0) {
        throw new IllegalArgumentException("SeatGradeInfo.price는 0 이상이어야 합니다.");
      }
      if (totalSeats == null || totalSeats <= 0) {
        throw new IllegalArgumentException("SeatGradeInfo.totalSeats는 1 이상이어야 합니다.");
      }
    }
  }

  /**
   * 개별 좌석 생성 정보.
   */
  public record SeatCreateInfo(
      String seatNumber,
      String grade,
      Long price
  ) {
    public void validate() {
      if (seatNumber == null || seatNumber.isBlank()) {
        throw new IllegalArgumentException("SeatCreateInfo.seatNumber는 필수입니다.");
      }
      if (grade == null || grade.isBlank()) {
        throw new IllegalArgumentException("SeatCreateInfo.grade는 필수입니다.");
      }
      if (price == null || price < 0) {
        throw new IllegalArgumentException("SeatCreateInfo.price는 0 이상이어야 합니다.");
      }
    }
  }
}