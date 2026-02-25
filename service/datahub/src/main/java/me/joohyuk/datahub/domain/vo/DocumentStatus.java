package me.joohyuk.datahub.domain.vo;

public enum DocumentStatus {
  UPLOADED,                // Extract 완료: 파일 저장 + DB 저장 완료 (Saga 시작 전 초기 상태)

  TRANSFORM_REQUESTED,     // Transform+Load 요청: Kafka 이벤트 publish 완료 (Saga STARTED)
  TRANSFORMING,            // Transform+Load 처리 중: 실제 변환 작업 진행 중 (Saga PROCESSING)
  TRANSFORMED,             // Transform+Load 완료: 결과 이벤트 수신 완료 (Saga SUCCEEDED, terminal)
  TRANSFORM_FAILED,        // Transform+Load 실패: 처리 실패 (Saga FAILED)

  EMBED_REQUESTED,         // Embed 요청: Kafka 이벤트 publish 완료 (Saga STARTED)
  EMBEDDING,               // Embed 처리 중: 실제 임베딩 작업 진행 중 (Saga PROCESSING)
  EMBEDDED,                // Embed 완료: 결과 이벤트 수신 완료 (Saga SUCCEEDED, terminal)
  EMBED_FAILED            // Embed 실패: 처리 실패 (Saga FAILED)
}
