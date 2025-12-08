package com.tickatch.product_service.product.application.dto;

import com.tickatch.product_service.product.domain.vo.AgeRating;
import com.tickatch.product_service.product.domain.vo.ProductType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 생성 명령 객체.
 *
 * <p>상품 생성에 필요한 모든 정보를 담는다. 서비스 레이어에서 이 정보를 바탕으로
 * 각 VO를 조립하여 도메인에 전달한다.
 *
 * <p>값객체는 세트 단위로 검증된다. 일부 필드만 있으면 검증 실패.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public class ProductCreateCommand {

  // ========== 기본 정보 (필수) ==========

  /** 판매자 ID */
  private final String sellerId;

  /** 상품명 */
  private final String name;

  /** 상품 타입 */
  private final ProductType productType;

  /** 상영 시간 (분) */
  private final Integer runningTime;

  // ========== 일정 (필수) ==========

  /** 행사 시작 일시 */
  private final LocalDateTime startAt;

  /** 행사 종료 일시 */
  private final LocalDateTime endAt;

  /** 예매 시작 일시 */
  private final LocalDateTime saleStartAt;

  /** 예매 종료 일시 */
  private final LocalDateTime saleEndAt;

  // ========== 장소 (필수) ==========

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

  // ========== 콘텐츠 (선택) ==========

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

  // ========== 관람 제한 (선택) ==========

  /** 관람 등급 */
  private final AgeRating ageRating;

  /** 추가 제한사항 안내 */
  private final String restrictionNotice;

  // ========== 예매 정책 (선택) ==========

  /** 1인당 최대 예매 매수 */
  private final Integer maxTicketsPerPerson;

  /** 본인확인 필요 여부 */
  private final Boolean idVerificationRequired;

  /** 양도 가능 여부 */
  private final Boolean transferable;

  // ========== 입장 정책 (선택) ==========

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

  // ========== 환불 정책 (선택) ==========

  /** 취소 가능 여부 */
  private final Boolean cancellable;

  /** 취소 마감일 (공연 n일 전) */
  private final Integer cancelDeadlineDays;

  /** 환불 정책 상세 안내 */
  private final String refundPolicyText;

  // ========== 좌석 등급 정보 (필수) ==========

  /** 좌석 등급 정보 리스트 (배열 순서대로 displayOrder) */
  private final List<SeatGradeInfo> seatGradeInfos;

  // ========== 개별 좌석 정보 (필수) ==========

  /** 개별 좌석 정보 리스트 (ReservationSeat 서비스 전달용) */
  private final List<SeatCreateInfo> seatCreateInfos;

  // ========== 검증 메서드 ==========

  /**
   * 필수 필드를 검증한다.
   *
   * @throws IllegalArgumentException 필수 필드가 없는 경우
   */
  public void validateRequired() {
    // 기본 정보
    validateNotNull(sellerId, "sellerId");
    validateNotBlank(name, "name");
    validateNotNull(productType, "productType");
    validateNotNull(runningTime, "runningTime");

    // 일정
    validateNotNull(startAt, "startAt");
    validateNotNull(endAt, "endAt");
    validateNotNull(saleStartAt, "saleStartAt");
    validateNotNull(saleEndAt, "saleEndAt");

    // 장소
    validateNotNull(stageId, "stageId");
    validateNotBlank(stageName, "stageName");
    validateNotNull(artHallId, "artHallId");
    validateNotBlank(artHallName, "artHallName");
    validateNotBlank(artHallAddress, "artHallAddress");

    // 좌석 정보
    validateNotEmpty(seatGradeInfos, "seatGradeInfos");
    validateNotEmpty(seatCreateInfos, "seatCreateInfos");
  }

  /**
   * 값객체 세트 검증을 수행한다.
   *
   * <p>값객체의 일부 필드만 있으면 검증 실패.
   *
   * @throws IllegalArgumentException 값객체 필드가 일부만 있는 경우
   */
  public void validateValueObjectSets() {
    // 관람 제한: ageRating 필수, restrictionNotice 선택
    if (hasAgeRestriction() && ageRating == null) {
      throw new IllegalArgumentException("AgeRestriction 설정 시 ageRating은 필수입니다.");
    }

    // 예매 정책: maxTicketsPerPerson 필수
    if (hasBookingPolicy() && maxTicketsPerPerson == null) {
      throw new IllegalArgumentException("BookingPolicy 설정 시 maxTicketsPerPerson은 필수입니다.");
    }

    // 입장 정책: admissionMinutesBefore 필수
    if (hasAdmissionPolicy() && admissionMinutesBefore == null) {
      throw new IllegalArgumentException("AdmissionPolicy 설정 시 admissionMinutesBefore는 필수입니다.");
    }

    // 환불 정책: cancellable 필수
    if (hasRefundPolicy() && cancellable == null) {
      throw new IllegalArgumentException("RefundPolicy 설정 시 cancellable은 필수입니다.");
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
   * <p>SeatCreateInfo의 grade가 SeatGradeInfo에 존재해야 한다.
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

  // ========== Private 검증 유틸 ==========

  private void validateNotNull(Object value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
    }
  }

  private void validateNotBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
    }
  }

  private void validateNotEmpty(List<?> list, String fieldName) {
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException(fieldName + "은(는) 최소 1개 이상이어야 합니다.");
    }
  }

  // ========== 중첩 Record ==========

  /**
   * 좌석 등급 정보.
   *
   * <p>Product의 SeatGrade 생성에 사용된다.
   * 배열 순서대로 displayOrder가 부여된다.
   *
   * @param gradeName 등급명 (예: "VIP", "R", "S")
   * @param price 가격
   * @param totalSeats 총 좌석수
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
   *
   * <p>ReservationSeat 서비스에 전달된다.
   *
   * @param seatNumber 좌석번호 (예: "A1", "B2")
   * @param grade 등급명 (예: "VIP", "R", "S")
   * @param price 가격
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