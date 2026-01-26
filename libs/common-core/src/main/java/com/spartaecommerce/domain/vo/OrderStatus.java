package com.spartaecommerce.domain.vo;

public enum OrderStatus {
  // 1) Catalog(재고) 단계
  INVENTORY_RESERVING,     // 재고 예약 요청됨 (주문 생성 초기 상태)
  INVENTORY_RESERVED,      // 재고 예약 완료

  // 3) Point 사용 단계 (결제 전 - 결제 금액 차감용)
  POINT_USING,             // 포인트 사용 처리중
  POINT_USED,              // 포인트 사용 완료

  // 4) Payment 단계
  PAYMENT_REQUESTED,       // 결제 요청됨 (포인트 차감 후 금액)
  PAID,                    // 결제 완료(승인)

  // 5) Point 적립 단계 (결제 후 - 보상 포인트)
  POINT_EARNING,           // 포인트 적립 처리중
  POINT_EARNED,            // 포인트 적립 완료

  // 6) 최종 확정
  APPROVED,                // 주문 확정("처리 시작 가능")
  COMPLETED,               // 배송/완료까지 포함한다면 최종 완료

  // 7) 실패/보상(취소)
  CANCELING,               // 보상 트랜잭션 진행중
  CANCELED,                // 취소 확정

  // 8) 장애/예외 운영용
  FAILED                   // 복구 불가/수동 개입 필요 (Dead-letter 성격)
}