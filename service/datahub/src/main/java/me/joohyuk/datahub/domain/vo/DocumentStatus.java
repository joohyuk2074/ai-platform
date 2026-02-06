package me.joohyuk.datahub.domain.vo;

public enum DocumentStatus {
  UPLOADED,              // Extract 완료: 파일 저장 + DB 저장 완료
  TRANSFORM_REQUESTED,   // Transform+Load 요청: Kafka 이벤트 publish 완료
  TRANSFORMED,           // Transform+Load 완료: 결과 이벤트 수신 완료 (terminal)
  TRANSFORM_FAILED;      // Transform+Load 실패: 처리 실패 (terminal, 재시도 가능)
}
