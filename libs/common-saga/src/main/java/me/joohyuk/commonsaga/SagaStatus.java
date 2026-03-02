package me.joohyuk.commonsaga;

public enum SagaStatus {
  STARTED,
  FAILED,
  SUCCEEDED,
  PROCESSING,
  COMPENSATING,
  COMPENSATED
}
