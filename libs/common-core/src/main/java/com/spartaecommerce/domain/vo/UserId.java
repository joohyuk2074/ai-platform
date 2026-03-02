package com.spartaecommerce.domain.vo;

public final class UserId extends BaseId<Long> {

  public UserId(Long value) {
    super(value);
  }

  public static UserId of(Long value) {
    return new UserId(value);
  }
}
