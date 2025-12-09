package com.tickatch.product_service.product.domain.repository.dto;

import com.tickatch.product_service.product.domain.Product;
import com.tickatch.product_service.product.domain.SeatGrade;
import com.tickatch.product_service.product.domain.vo.AgeRating;
import com.tickatch.product_service.product.domain.vo.ProductStatus;
import com.tickatch.product_service.product.domain.vo.ProductType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 응답 DTO.
 *
 * <p>상품 조회 시 반환되는 데이터를 담는다. 엔티티를 외부에 노출하지 않고 필요한 필드만 전달한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public final class ProductResponse {

  // ========== 기본 정보 ==========

  /** 상품 ID */
  private final Long id;

  /** 판매자 ID */
  private final String sellerId;

  /** 상품명 */
  private final String name;

  /** 상품 타입 */
  private final ProductType productType;

  /** 상영 시간 (분) */
  private final Integer runningTime;

  // ========== 일정 ==========

  /** 행사 시작 일시 */
  private final LocalDateTime startAt;

  /** 행사 종료 일시 */
  private final LocalDateTime endAt;

  /** 예매 시작 일시 */
  private final LocalDateTime saleStartAt;

  /** 예매 종료 일시 */
  private final LocalDateTime saleEndAt;

  // ========== 장소 ==========

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

  // ========== 콘텐츠 ==========

  /** 상세 설명 */
  private final String description;

  /** 메인 포스터 이미지 URL */
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

  // ========== 관람 제한 ==========

  /** 관람 등급 */
  private final AgeRating ageRating;

  /** 추가 제한사항 안내 */
  private final String restrictionNotice;

  // ========== 예매 정책 ==========

  /** 1인당 최대 예매 매수 */
  private final Integer maxTicketsPerPerson;

  /** 본인확인 필요 여부 */
  private final Boolean idVerificationRequired;

  /** 양도 가능 여부 */
  private final Boolean transferable;

  // ========== 입장 정책 ==========

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

  // ========== 환불 정책 ==========

  /** 취소 가능 여부 */
  private final Boolean cancellable;

  /** 취소 마감일 (공연 n일 전) */
  private final Integer cancelDeadlineDays;

  /** 환불 정책 상세 안내 */
  private final String refundPolicyText;

  // ========== 좌석 현황 ==========

  /** 총 좌석 수 */
  private final int totalSeats;

  /** 잔여 좌석 수 */
  private final int availableSeats;

  /** 매진 여부 */
  private final boolean soldOut;

  /** 등급별 좌석 정보 */
  private final List<SeatGradeResponse> seatGrades;

  // ========== 통계 ==========

  /** 조회수 */
  private final Long viewCount;

  /** 예매 수 */
  private final int reservationCount;

  // ========== 상태 ==========

  /** 구매 가능 여부 */
  private final boolean purchasable;

  /** 상품 상태 */
  private final ProductStatus status;

  /** 반려 사유 */
  private final String rejectionReason;

  // ========== 감사 정보 ==========

  /** 생성 일시 */
  private final LocalDateTime createdAt;

  /** 수정 일시 */
  private final LocalDateTime updatedAt;

  /**
   * 상품 엔티티를 응답 DTO로 변환한다.
   *
   * @param product 상품 엔티티
   * @return 상품 응답 DTO
   */
  public static ProductResponse from(Product product) {
    return ProductResponse.builder()
        // 기본 정보
        .id(product.getId())
        .sellerId(product.getSellerId())
        .name(product.getName())
        .productType(product.getProductType())
        .runningTime(product.getRunningTime())
        // 일정
        .startAt(product.getStartAt())
        .endAt(product.getEndAt())
        .saleStartAt(product.getSaleStartAt())
        .saleEndAt(product.getSaleEndAt())
        // 장소
        .stageId(product.getStageId())
        .stageName(product.getVenue().getStageName())
        .artHallId(product.getVenue().getArtHallId())
        .artHallName(product.getVenue().getArtHallName())
        .artHallAddress(product.getVenue().getArtHallAddress())
        // 콘텐츠
        .description(product.getContent() != null ? product.getContent().getDescription() : null)
        .posterImageUrl(
            product.getContent() != null ? product.getContent().getPosterImageUrl() : null)
        .detailImageUrls(
            product.getContent() != null ? product.getContent().getDetailImageUrls() : null)
        .castInfo(product.getContent() != null ? product.getContent().getCastInfo() : null)
        .notice(product.getContent() != null ? product.getContent().getNotice() : null)
        .organizer(product.getContent() != null ? product.getContent().getOrganizer() : null)
        .agency(product.getContent() != null ? product.getContent().getAgency() : null)
        // 관람 제한
        .ageRating(
            product.getAgeRestriction() != null ? product.getAgeRestriction().getAgeRating() : null)
        .restrictionNotice(
            product.getAgeRestriction() != null
                ? product.getAgeRestriction().getRestrictionNotice()
                : null)
        // 예매 정책
        .maxTicketsPerPerson(
            product.getBookingPolicy() != null
                ? product.getBookingPolicy().getMaxTicketsPerPerson()
                : null)
        .idVerificationRequired(
            product.getBookingPolicy() != null
                ? product.getBookingPolicy().getIdVerificationRequired()
                : null)
        .transferable(
            product.getBookingPolicy() != null
                ? product.getBookingPolicy().getTransferable()
                : null)
        // 입장 정책
        .admissionMinutesBefore(
            product.getAdmissionPolicy() != null
                ? product.getAdmissionPolicy().getAdmissionMinutesBefore()
                : null)
        .lateEntryAllowed(
            product.getAdmissionPolicy() != null
                ? product.getAdmissionPolicy().getLateEntryAllowed()
                : null)
        .lateEntryNotice(
            product.getAdmissionPolicy() != null
                ? product.getAdmissionPolicy().getLateEntryNotice()
                : null)
        .hasIntermission(
            product.getAdmissionPolicy() != null
                ? product.getAdmissionPolicy().getHasIntermission()
                : null)
        .intermissionMinutes(
            product.getAdmissionPolicy() != null
                ? product.getAdmissionPolicy().getIntermissionMinutes()
                : null)
        .photographyAllowed(
            product.getAdmissionPolicy() != null
                ? product.getAdmissionPolicy().getPhotographyAllowed()
                : null)
        .foodAllowed(
            product.getAdmissionPolicy() != null
                ? product.getAdmissionPolicy().getFoodAllowed()
                : null)
        // 환불 정책
        .cancellable(
            product.getRefundPolicy() != null ? product.getRefundPolicy().getCancellable() : null)
        .cancelDeadlineDays(
            product.getRefundPolicy() != null
                ? product.getRefundPolicy().getCancelDeadlineDays()
                : null)
        .refundPolicyText(
            product.getRefundPolicy() != null
                ? product.getRefundPolicy().getRefundPolicyText()
                : null)
        // 좌석 현황
        .totalSeats(product.getSeatSummary().getTotalSeats())
        .availableSeats(product.getSeatSummary().getAvailableSeats())
        .soldOut(product.isSoldOut())
        .seatGrades(product.getSeatGrades().stream().map(SeatGradeResponse::from).toList())
        // 통계
        .viewCount(product.getStats().getViewCount())
        .reservationCount(product.getStats().getReservationCount())
        // 상태
        .purchasable(product.canPurchase())
        .status(product.getStatus())
        .rejectionReason(product.getRejectionReason())
        // 감사 정보
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }

  /** 등급별 좌석 응답 DTO. */
  @Getter
  @Builder
  public static class SeatGradeResponse {
    private final Long id;
    private final String gradeName;
    private final Long price;
    private final Integer totalSeats;
    private final Integer availableSeats;
    private final Integer displayOrder;
    private final boolean soldOut;

    public static SeatGradeResponse from(SeatGrade seatGrade) {
      return SeatGradeResponse.builder()
          .id(seatGrade.getId())
          .gradeName(seatGrade.getGradeName())
          .price(seatGrade.getPrice())
          .totalSeats(seatGrade.getTotalSeats())
          .availableSeats(seatGrade.getAvailableSeats())
          .displayOrder(seatGrade.getDisplayOrder())
          .soldOut(seatGrade.isSoldOut())
          .build();
    }
  }
}
