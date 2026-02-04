package me.joohyuk.datarex.domain.exception;

public class DatarexDomainException extends RuntimeException {

  public DatarexDomainException(String message) {
    super(message);
  }

  public DatarexDomainException(String message, Throwable cause) {
    super(message, cause);
  }

  public DatarexDomainException(Throwable cause) {
    super(cause);
  }
}
