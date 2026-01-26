package me.joohyuk.ingestion.domain.exception;

import com.spartaecommerce.exception.DomainException;

public class IngestionDomainException extends DomainException {

  public IngestionDomainException(String message) {
    super(message);
  }

  public IngestionDomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
