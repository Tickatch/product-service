# Product Error Messages 한글 매핑

## 조회 (404)

| 코드 | 메시지 |
|------|--------|
| `PRODUCT_NOT_FOUND` | 상품 {0}을(를) 찾을 수 없습니다. |

## 검증 (400)

| 코드 | 메시지 |
|------|--------|
| `INVALID_PRODUCT_NAME` | 상품명은 필수이며 50자 이하여야 합니다. |
| `INVALID_RUNNING_TIME` | 상연 시간은 0보다 커야 합니다. |
| `INVALID_PRODUCT_TYPE` | 상품 타입은 필수입니다. |
| `INVALID_SCHEDULE` | 일정이 유효하지 않습니다. 종료 일시는 시작 일시 이후여야 합니다. |
| `INVALID_STAGE_ID` | 스테이지 ID는 필수입니다. |
| `INVALID_PRODUCT_STATUS` | 상품 상태는 필수입니다. |
| `INVALID_SALE_SCHEDULE` | 판매 일정이 유효하지 않습니다. 예매 종료 일시는 시작 일시 이후여야 합니다. |
| `INVALID_VENUE` | 장소 정보가 유효하지 않습니다. 스테이지 ID, 스테이지명, 아트홀 ID, 아트홀명, 주소는 필수입니다. |
| `INVALID_SELLER_ID` | 판매자 ID는 필수입니다. |
| `INVALID_REJECTION_REASON` | 반려 사유는 필수입니다. |
| `INVALID_SEAT_COUNT` | 좌석 수가 유효하지 않습니다. |
| `SALE_MUST_START_BEFORE_EVENT` | 예매 시작일은 행사 시작일보다 이전이어야 합니다. |
| `SALE_MUST_END_BEFORE_EVENT` | 예매 종료일은 행사 시작일보다 이전이어야 합니다. |

## 비즈니스 규칙 (422)

| 코드 | 메시지 |
|------|--------|
| `STAGE_CHANGE_NOT_ALLOWED` | 행사 시작 후에는 스테이지를 변경할 수 없습니다. |
| `VENUE_CHANGE_NOT_ALLOWED` | 행사 시작 후에는 장소를 변경할 수 없습니다. |
| `PRODUCT_ALREADY_CANCELLED` | 이미 취소된 상품입니다. |
| `PRODUCT_STATUS_CHANGE_NOT_ALLOWED` | 현재 상태({0})에서는 {1} 상태로 변경할 수 없습니다. |
| `PRODUCT_NOT_PENDING` | 심사 대기 상태가 아닙니다. 승인/반려는 심사 대기 상태에서만 가능합니다. |
| `PRODUCT_NOT_REJECTED` | 반려 상태가 아닙니다. 재제출은 반려 상태에서만 가능합니다. |
| `PRODUCT_NOT_EDITABLE` | 수정 가능한 상태가 아닙니다. DRAFT 또는 REJECTED 상태에서만 수정 가능합니다. |
| `NOT_ENOUGH_SEATS` | 잔여 좌석이 부족합니다. |

## 권한 (403)

| 코드 | 메시지 |
|------|--------|
| `PRODUCT_NOT_OWNED` | 해당 상품에 대한 권한이 없습니다. |

## 이벤트 발행 (503)

| 코드 | 메시지 |
|------|--------|
| `EVENT_PUBLISH_FAILED` | 상품 취소 이벤트 발행에 실패했습니다. (상품 ID: {0}) |