package me.joohyuk.datahub.domain.exception;

import com.spartaecommerce.exception.DomainException;

public class DatahubDomainException extends DomainException {

  public DatahubDomainException(String message) {
    super(message);
  }

  public DatahubDomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
