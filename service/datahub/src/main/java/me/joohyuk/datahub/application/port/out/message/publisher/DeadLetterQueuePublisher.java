package me.joohyuk.datahub.application.port.out.message.publisher;

/**
 * Dead Letter Queue에 실패한 메시지를 발행하는 포트
 */
public interface DeadLetterQueuePublisher {

  /**
   * DLQ에 실패한 메시지를 발행합니다.
   *
   * @param topic          원본 토픽
   * @param key            메시지 키
   * @param payload        메시지 페이로드
   * @param errorCode      에러 코드
   * @param errorMessage   에러 메시지
   * @param correlationId  Saga correlation ID
   */
  void publish(
      String topic,
      String key,
      String payload,
      String errorCode,
      String errorMessage,
      String correlationId
  );
}
