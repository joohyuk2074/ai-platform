package me.joohyuk.datahub.domain.vo;

public enum DocumentStatus {
  UPLOADED,              // 파일 저장 + DB 저장 완료
  PASSAGE_REQUESTED,     // Kafka 이벤트 publish 완료
  PASSAGE_CREATED,       // 결과 이벤트 수신 완료 (terminal)
  PASSAGE_FAILED;        // 처리 실패 (terminal, 재시도 가능)
}
