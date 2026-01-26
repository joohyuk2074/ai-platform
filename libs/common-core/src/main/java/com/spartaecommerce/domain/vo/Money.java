package com.spartaecommerce.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(
    BigDecimal amount
) {

  public static final Money ZERO = new Money(BigDecimal.ZERO);

  private static final int SCALE = 2;

  public Money {
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }

    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }

    amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
  }

  public static Money from(BigDecimal amount) {
    return new Money(amount);
  }

  public static Money zero() {
    return new Money(BigDecimal.ZERO);
  }

  public Money add(Money other) {
    return new Money(this.amount.add(other.amount));
  }

  public Money subtract(Money other) {
    return new Money(this.amount.subtract(other.amount));
  }

  public Money multiply(int quantity) {
    return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)));
  }

  public boolean isGreaterThanZero() {
    return this.amount.compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean isGreaterThan(Money other) {
    return this.amount.compareTo(other.amount) > 0;
  }

  public boolean isGreaterThanEqual(Money other) {
    return this.amount.compareTo(other.amount) >= 0;
  }

  public boolean isLessThan(Money other) {
    return this.amount.compareTo(other.amount) < 0;
  }

  public boolean isLessThanEqual(Money other) {
    return this.amount.compareTo(other.amount) <= 0;
  }

  public boolean isZero() {
    return this.amount.compareTo(BigDecimal.ZERO) == 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Money money)) {
      return false;
    }
    return amount.compareTo(money.amount) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount);
  }
}
