package me.joohyuk.messaging.topics;

public final class KafkaTopics {

  public static final String DOCUMENT_TRANSFORM_REQUESTED = "document.transform.requested";

  public static final String DOCUMENT_TRANSFORM_RESULT = "document.transform.result";

  private KafkaTopics() {
    throw new AssertionError("Cannot instantiate constants class");
  }
}
