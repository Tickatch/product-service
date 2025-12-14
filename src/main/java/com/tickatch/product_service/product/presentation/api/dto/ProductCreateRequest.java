package com.tickatch.product_service.product.presentation.api.dto;

import com.tickatch.product_service.product.application.dto.ProductCreateCommand;
import com.tickatch.product_service.product.application.dto.ProductCreateCommand.SeatCreateInfo;
import com.tickatch.product_service.product.application.dto.ProductCreateCommand.SeatGradeInfo;
import com.tickatch.product_service.product.domain.vo.AgeRating;
import com.tickatch.product_service.product.domain.vo.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 생성 요청 DTO.
 *
 * <p>새 상품 생성에 필요한 모든 정보를 담는다. 생성된 상품은 DRAFT 상태로 시작된다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Schema(description = "상품 생성 요청")
public record ProductCreateRequest(
    // ========== 기본 정보 (필수) ==========
    @Schema(description = "상품명", example = "레미제라블", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 50, message = "상품명은 50자 이하여야 합니다")
        String name,
    @Schema(description = "상품 타입", example = "MUSICAL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "상품 타입은 필수입니다")
        ProductType productType,
    @Schema(description = "상영 시간 (분)", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "상영 시간은 필수입니다")
        @Positive(message = "상영 시간은 양수여야 합니다")
        Integer runningTime,

    // ========== 일정 (필수) ==========
    @Schema(
            description = "행사 시작 일시",
            example = "2025-03-01T19:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "행사 시작 시간은 필수입니다")
        LocalDateTime startAt,
    @Schema(
            description = "행사 종료 일시",
            example = "2025-03-01T21:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "행사 종료 시간은 필수입니다")
        LocalDateTime endAt,
    @Schema(
            description = "예매 시작 일시",
            example = "2025-02-01T10:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "예매 시작 시간은 필수입니다")
        LocalDateTime saleStartAt,
    @Schema(
            description = "예매 종료 일시",
            example = "2025-02-28T23:59:59",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "예매 종료 시간은 필수입니다")
        LocalDateTime saleEndAt,

    // ========== 장소 (필수) ==========
    @Schema(description = "스테이지 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "스테이지 ID는 필수입니다")
        Long stageId,
    @Schema(description = "스테이지명", example = "올림픽홀", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "스테이지명은 필수입니다")
        String stageName,
    @Schema(description = "공연장 ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "공연장 ID는 필수입니다")
        Long artHallId,
    @Schema(description = "공연장명", example = "올림픽공원", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "공연장명은 필수입니다")
        String artHallName,
    @Schema(
            description = "공연장 주소",
            example = "서울시 송파구 올림픽로 424",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "공연장 주소는 필수입니다")
        String artHallAddress,

    // ========== 콘텐츠 (선택) ==========
    @Schema(description = "상세 설명", example = "세계적인 뮤지컬 레미제라블...")
        @Size(max = 5000, message = "상세 설명은 5000자 이하여야 합니다")
        String description,
    @Schema(description = "메인 포스터 이미지 URL", example = "https://example.com/poster.jpg")
        String posterImageUrl,
    @Schema(description = "상세 이미지 URL 배열 (JSON)", example = "[\"url1\", \"url2\"]")
        String detailImageUrls,
    @Schema(description = "출연진/아티스트 정보", example = "홍길동, 김철수") String castInfo,
    @Schema(description = "유의사항", example = "8세 이상 관람 가능") String notice,
    @Schema(description = "주최사", example = "OO엔터테인먼트") String organizer,
    @Schema(description = "주관사/기획사", example = "XX프로덕션") String agency,

    // ========== 관람 제한 (필수: ageRating) ==========
    @Schema(description = "관람 등급", example = "ALL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "관람 등급은 필수입니다")
        AgeRating ageRating,
    @Schema(description = "추가 제한사항 안내", example = "36개월 미만 입장 불가") String restrictionNotice,

    // ========== 예매 정책 (필수: maxTicketsPerPerson) ==========
    @Schema(
            description = "1인당 최대 예매 매수",
            example = "4",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "1인당 최대 예매 매수는 필수입니다")
        @Positive(message = "1인당 최대 예매 매수는 양수여야 합니다")
        Integer maxTicketsPerPerson,
    @Schema(description = "본인확인 필요 여부", example = "false") Boolean idVerificationRequired,
    @Schema(description = "양도 가능 여부", example = "true") Boolean transferable,

    // ========== 입장 정책 (필수: admissionMinutesBefore) ==========
    @Schema(
            description = "입장 시작 시간 (공연 n분 전)",
            example = "30",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "입장 시작 시간은 필수입니다")
        @PositiveOrZero(message = "입장 시작 시간은 0 이상이어야 합니다")
        Integer admissionMinutesBefore,
    @Schema(description = "지각 입장 가능 여부", example = "true") Boolean lateEntryAllowed,
    @Schema(description = "지각 입장 안내", example = "1막 종료 후 입장 가능") String lateEntryNotice,
    @Schema(description = "인터미션 유무", example = "true") Boolean hasIntermission,
    @Schema(description = "인터미션 시간 (분)", example = "15")
        @PositiveOrZero(message = "인터미션 시간은 0 이상이어야 합니다")
        Integer intermissionMinutes,
    @Schema(description = "촬영 가능 여부", example = "false") Boolean photographyAllowed,
    @Schema(description = "음식물 반입 가능 여부", example = "false") Boolean foodAllowed,

    // ========== 환불 정책 (필수: cancellable) ==========
    @Schema(description = "취소 가능 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "취소 가능 여부는 필수입니다")
        Boolean cancellable,
    @Schema(description = "취소 마감일 (공연 n일 전)", example = "7")
        @PositiveOrZero(message = "취소 마감일은 0 이상이어야 합니다")
        Integer cancelDeadlineDays,
    @Schema(description = "환불 정책 상세 안내", example = "관람일 7일 전까지 전액 환불 가능") String refundPolicyText,

    // ========== 좌석 등급 (필수) ==========
    @Schema(description = "좌석 등급 정보 리스트", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "좌석 등급 정보는 필수입니다")
        @Valid
        List<SeatGradeRequest> seatGradeInfos,

    // ========== 개별 좌석 (필수) ==========
    @Schema(description = "개별 좌석 정보 리스트", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "개별 좌석 정보는 필수입니다")
        @Valid
        List<SeatCreateRequest> seatCreateInfos) {

  /** Compact constructor로 방어적 복사 수행. List 필드를 불변 리스트로 변환하여 내부 표현 노출을 방지한다. */
  public ProductCreateRequest {
    seatGradeInfos = seatGradeInfos == null ? List.of() : List.copyOf(seatGradeInfos);
    seatCreateInfos = seatCreateInfos == null ? List.of() : List.copyOf(seatCreateInfos);
  }

  /**
   * 요청을 Command 객체로 변환한다.
   *
   * @param sellerId 판매자 ID (인증 정보에서 추출)
   * @return ProductCreateCommand
   */
  public ProductCreateCommand toCommand(String sellerId) {
    return ProductCreateCommand.builder()
        // 기본 정보
        .sellerId(sellerId)
        .name(name)
        .productType(productType)
        .runningTime(runningTime)
        // 일정
        .startAt(startAt)
        .endAt(endAt)
        .saleStartAt(saleStartAt)
        .saleEndAt(saleEndAt)
        // 장소
        .stageId(stageId)
        .stageName(stageName)
        .artHallId(artHallId)
        .artHallName(artHallName)
        .artHallAddress(artHallAddress)
        // 콘텐츠
        .description(description)
        .posterImageUrl(posterImageUrl)
        .detailImageUrls(detailImageUrls)
        .castInfo(castInfo)
        .notice(notice)
        .organizer(organizer)
        .agency(agency)
        // 관람 제한
        .ageRating(ageRating)
        .restrictionNotice(restrictionNotice)
        // 예매 정책
        .maxTicketsPerPerson(maxTicketsPerPerson)
        .idVerificationRequired(idVerificationRequired)
        .transferable(transferable)
        // 입장 정책
        .admissionMinutesBefore(admissionMinutesBefore)
        .lateEntryAllowed(lateEntryAllowed)
        .lateEntryNotice(lateEntryNotice)
        .hasIntermission(hasIntermission)
        .intermissionMinutes(intermissionMinutes)
        .photographyAllowed(photographyAllowed)
        .foodAllowed(foodAllowed)
        // 환불 정책
        .cancellable(cancellable)
        .cancelDeadlineDays(cancelDeadlineDays)
        .refundPolicyText(refundPolicyText)
        // 좌석 정보
        .seatGradeInfos(
            seatGradeInfos.stream()
                .map(sg -> new SeatGradeInfo(sg.gradeName(), sg.price(), sg.totalSeats()))
                .toList())
        .seatCreateInfos(
            seatCreateInfos.stream()
                .map(sc -> new SeatCreateInfo(sc.seatNumber(), sc.grade(), sc.price()))
                .toList())
        .build();
  }

  /** 좌석 등급 정보 요청. */
  @Schema(description = "좌석 등급 정보")
  public record SeatGradeRequest(
      @Schema(description = "등급명", example = "VIP", requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank(message = "등급명은 필수입니다")
          String gradeName,
      @Schema(description = "가격", example = "150000", requiredMode = Schema.RequiredMode.REQUIRED)
          @NotNull(message = "가격은 필수입니다")
          @PositiveOrZero(message = "가격은 0 이상이어야 합니다")
          Long price,
      @Schema(description = "총 좌석 수", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
          @NotNull(message = "총 좌석 수는 필수입니다")
          @Positive(message = "총 좌석 수는 양수여야 합니다")
          Integer totalSeats) {}

  /** 개별 좌석 정보 요청. */
  @Schema(description = "개별 좌석 정보")
  public record SeatCreateRequest(
      @Schema(description = "좌석번호", example = "A1", requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank(message = "좌석번호는 필수입니다")
          String seatNumber,
      @Schema(description = "등급명", example = "VIP", requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank(message = "등급명은 필수입니다")
          String grade,
      @Schema(description = "가격", example = "150000", requiredMode = Schema.RequiredMode.REQUIRED)
          @NotNull(message = "가격은 필수입니다")
          @PositiveOrZero(message = "가격은 0 이상이어야 합니다")
          Long price) {}
}
