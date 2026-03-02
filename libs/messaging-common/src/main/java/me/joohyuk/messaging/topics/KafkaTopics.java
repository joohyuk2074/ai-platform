package me.joohyuk.messaging.topics;

public final class KafkaTopics {

  public static final String DOCUMENT_TRANSFORM_REQUESTED = "document.transform.requested";

  public static final String DOCUMENT_TRANSFORM_RESULT = "document.transform.result";

  public static final String DOCUMENT_EMBED_REQUESTED = "document.embed.requested";

  public static final String DOCUMENT_TRANSFORM_DLQ = "datahub.transform.dlq";

  private KafkaTopics() {
    throw new AssertionError("Cannot instantiate constants class");
  }
}
