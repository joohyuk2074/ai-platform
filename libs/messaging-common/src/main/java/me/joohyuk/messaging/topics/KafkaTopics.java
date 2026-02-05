package me.joohyuk.messaging.topics;

public final class KafkaTopics {

  /**
   * Document 변환 요청 토픽 Producer: datahub Consumer: datarex
   */
  public static final String DOCUMENT_TRANSFORM_REQUESTED = "message.transform.requested";

  private KafkaTopics() {
    throw new AssertionError("Cannot instantiate constants class");
  }
}
