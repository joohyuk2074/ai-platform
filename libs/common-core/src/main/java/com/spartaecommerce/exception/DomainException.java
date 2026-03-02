package com.spartaecommerce.exception;

public class DomainException extends RuntimeException {

  private final DomainErrorCode errorCode;

  public DomainException(DomainErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public DomainException(String message, DomainErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public DomainException(String message, DomainErrorCode errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public DomainException(DomainErrorCode errorCode, Throwable cause) {
    super(cause);
    this.errorCode = errorCode;
  }

  public DomainErrorCode getErrorCode() {
    return errorCode;
  }
}
