package me.joohyuk.datahub.domain.exception;

import com.spartaecommerce.exception.DomainException;

public class DatahubDomainException extends DomainException {

  public DatahubDomainException(
      String message,
      DatahubErrorCode errorCode
  ) {
    super(message, errorCode);
  }

  public DatahubDomainException(
      String message,
      DatahubErrorCode errorCode,
      Throwable cause
  ) {
    super(message, errorCode, cause);
  }
}
