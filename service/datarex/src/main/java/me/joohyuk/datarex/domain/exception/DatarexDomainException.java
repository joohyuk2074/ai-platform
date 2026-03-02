package me.joohyuk.datarex.domain.exception;

import com.spartaecommerce.exception.DomainErrorCode;
import com.spartaecommerce.exception.DomainException;

public class DatarexDomainException extends DomainException {

  public DatarexDomainException(DomainErrorCode errorCode) {
    super(errorCode);
  }

  public DatarexDomainException(String message, DomainErrorCode errorCode) {
    super(message, errorCode);
  }

  public DatarexDomainException(String message, DomainErrorCode errorCode, Throwable cause) {
    super(message, errorCode, cause);
  }

  public DatarexDomainException(DomainErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
