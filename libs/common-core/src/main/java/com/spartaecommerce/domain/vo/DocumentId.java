package com.spartaecommerce.domain.vo;

public final class DocumentId extends BaseId<Long> {

  public DocumentId(Long value) {
    super(value);
  }

  public static DocumentId from(String value) {
    return new DocumentId(Long.getLong(value));
  }

  public static DocumentId from(Long value) {
    return new DocumentId(value);
  }
}
