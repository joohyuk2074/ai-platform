package com.spartaecommerce.outbox;

/**
 * 아웃박스 메시지의 생명주기 상태입니다.
 *
 * <ul>
 *   <li>{@link #PENDING} — 아직 Kafka로 전송되지 않음. 스케줄러가 주기적으로 폴링하여 발행 시도합니다.</li>
 *   <li>{@link #SENT} — Kafka로 성공적으로 전송됨 (terminal).</li>
 *   <li>{@link #FAILED} — 최대 재시도 횟수를 초과하여 전송 실패 (terminal). 수동 조사 또는 DLQ
 *       처리가 필요합니다.</li>
 * </ul>
 */
public enum OutboxStatus {
  PENDING,
  SENT,
  FAILED
}
