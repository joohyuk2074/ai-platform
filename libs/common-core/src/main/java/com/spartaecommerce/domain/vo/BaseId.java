package com.spartaecommerce.domain.vo;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public abstract class BaseId<T> {

  private final T value;

  protected BaseId(T value) {
    this.value = value;
  }

  @JsonValue
  public T getValue() {
    return value;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof BaseId<?> baseId)) {
      return false;
    }
    return Objects.equals(value, baseId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }
}
