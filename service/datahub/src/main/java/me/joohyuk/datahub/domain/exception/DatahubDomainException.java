package me.joohyuk.datahub.domain.exception;

import com.spartaecommerce.exception.DomainException;

public class DatahubDomainException extends DomainException {

  public DatahubDomainException(
      String message,
      DatahubDomainErrorCode errorCode
  ) {
    super(message, errorCode);
  }

  public DatahubDomainException(
      String message,
      DatahubDomainErrorCode errorCode,
      Throwable cause
  ) {
    super(message, errorCode, cause);
  }
}
