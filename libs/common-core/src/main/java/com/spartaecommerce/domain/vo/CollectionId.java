package com.spartaecommerce.domain.vo;

public final class CollectionId extends BaseId<Long> {

  public CollectionId(Long value) {
    super(value);
  }

  public static CollectionId of(String value) {
    return new CollectionId(Long.parseLong(value));
  }

  public static CollectionId of(Long value) {
    return new CollectionId(value);
  }
}
