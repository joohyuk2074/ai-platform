package me.joohyuk.commonsaga;

public final class SagaConstants {

  private SagaConstants() {
    throw new AssertionError("Utility class cannot be instantiated");
  }

  public static final String DOCUMENT_TRANSFORM_SAGA_NAME = "TransformDocumentSaga";
  public static final String DOCUMENT_EMBED_SAGA_NAME = "EmbedDocumentSaga";

}
