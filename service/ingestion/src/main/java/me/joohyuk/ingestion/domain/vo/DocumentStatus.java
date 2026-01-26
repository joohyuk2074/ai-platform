package me.joohyuk.ingestion.domain.vo;

public enum DocumentStatus {
  UPLOADED,

  VALIDATING,
  VALIDATED,
  VALIDATION_FAILED,

  CHUNKED,

  PASSAGES_GENERATED,

  EMBEDDED,

  FAILED;

  public boolean isProcessable() {
    return this == UPLOADED || this == VALIDATED || this == CHUNKED;
  }

  public boolean isFinal() {
    return this == EMBEDDED || this == FAILED;
  }
}
